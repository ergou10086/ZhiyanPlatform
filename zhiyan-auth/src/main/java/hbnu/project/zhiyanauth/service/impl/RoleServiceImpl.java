package hbnu.project.zhiyanauth.service.impl;

import hbnu.project.zhiyanauth.mapper.RoleMapper;
import hbnu.project.zhiyanauth.model.dto.RoleDTO;
import hbnu.project.zhiyanauth.model.entity.*;
import hbnu.project.zhiyanauth.model.enums.*;
import hbnu.project.zhiyanauth.repository.*;
import hbnu.project.zhiyanauth.service.RoleService;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonbasic.utils.id.SnowflakeIdUtil;
import hbnu.project.zhiyancommonredis.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 角色服务实现类
 * 提供角色管理、角色分配等核心功能
 * 
 * 实现说明：
 * 1. 专注于系统角色管理（DEVELOPER、USER、GUEST）
 * 2. 使用 Redis 缓存提升查询性能
 * 3. 事务保证数据一致性
 * 4. 详细的日志记录便于问题排查
 *
 * @author ErgouTree
 * @version 2.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;
    private final RedisService redisService;

    // 缓存相关常量
    private static final String USER_ROLES_CACHE_PREFIX = "user:roles:";
    private static final String ROLE_CACHE_PREFIX = "role:";
    private static final String ROLE_PERMISSIONS_CACHE_PREFIX = "role:permissions:";
    private static final long CACHE_EXPIRE_TIME = 1800L; // 30分钟

    // ==================== 角色信息查询 ====================

    @Override
    @Transactional(readOnly = true)
    public R<Page<RoleDTO>> getAllRoles(Pageable pageable) {
        try {
            log.debug("查询所有角色 - 页码: {}, 每页数量: {}", 
                    pageable.getPageNumber(), pageable.getPageSize());

            Page<Role> rolePage = roleRepository.findAll(pageable);
            List<RoleDTO> roleDTOs = roleMapper.toDTOList(rolePage.getContent());

            Page<RoleDTO> result = new PageImpl<>(roleDTOs, pageable, rolePage.getTotalElements());

            log.debug("查询角色成功 - 总数: {}, 当前页数量: {}", 
                    rolePage.getTotalElements(), roleDTOs.size());
            return R.ok(result);
        } catch (Exception e) {
            log.error("查询角色列表失败", e);
            return R.fail("查询角色列表失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public R<RoleDTO> getRoleById(Long roleId) {
        try {
            log.debug("查询角色详情 - roleId: {}", roleId);

            if (roleId == null) {
                return R.fail("角色ID不能为空");
            }

            // 先从缓存获取
            String cacheKey = ROLE_CACHE_PREFIX + roleId;
            Role cachedRole = redisService.getCacheObject(cacheKey);

            if (cachedRole != null) {
                RoleDTO roleDTO = roleMapper.toDTO(cachedRole);
                return R.ok(roleDTO);
            }

            // 缓存未命中，从数据库查询
            Optional<Role> optionalRole = roleRepository.findById(roleId);
            if (optionalRole.isEmpty()) {
                log.warn("角色不存在 - roleId: {}", roleId);
                return R.fail("角色不存在");
            }

            Role role = optionalRole.get();
            
            // 缓存角色信息
            redisService.setCacheObject(cacheKey, role, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);

            RoleDTO roleDTO = roleMapper.toDTO(role);
            return R.ok(roleDTO);
        } catch (Exception e) {
            log.error("查询角色详情失败 - roleId: {}", roleId, e);
            return R.fail("查询角色详情失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public R<RoleDTO> getRoleByName(String roleName) {
        try {
            log.debug("根据名称查询角色 - roleName: {}", roleName);

            if (!StringUtils.hasText(roleName)) {
                return R.fail("角色名称不能为空");
            }

            Optional<Role> optionalRole = roleRepository.findByName(roleName);
            if (optionalRole.isEmpty()) {
                log.warn("角色不存在 - roleName: {}", roleName);
                return R.fail("角色不存在");
            }

            Role role = optionalRole.get();
            RoleDTO roleDTO = roleMapper.toDTO(role);

            return R.ok(roleDTO);
        } catch (Exception e) {
            log.error("根据名称查询角色失败 - roleName: {}", roleName, e);
            return R.fail("查询角色失败");
        }
    }

    // ==================== 角色管理（CRUD） ====================

    @Override
    @Transactional
    public R<RoleDTO> createRole(RoleDTO roleDTO) {
        try {
            log.info("创建角色 - name: {}", roleDTO.getName());

            if (roleDTO == null || !StringUtils.hasText(roleDTO.getName())) {
                return R.fail("角色信息不完整");
            }

            // 检查角色名称是否已存在
            if (roleRepository.existsByName(roleDTO.getName())) {
                return R.fail("角色名称已存在: " + roleDTO.getName());
            }

            // 转换为实体
            Role role = roleMapper.fromDTO(roleDTO);

            // 设置必需字段
            if (role.getId() == null) {
                role.setId(SnowflakeIdUtil.nextId());
            }
            if (role.getRoleType() == null) {
                role.setRoleType("SYSTEM");
            }
            if (role.getIsSystemDefault() == null) {
                role.setIsSystemDefault(false);
            }

            Role savedRole = roleRepository.save(role);

            // 清理缓存
            clearRoleCache(savedRole.getId());

            RoleDTO result = roleMapper.toDTO(savedRole);

            log.info("创建角色成功 - roleId: {}, name: {}", savedRole.getId(), savedRole.getName());
            return R.ok(result, "角色创建成功");
        } catch (Exception e) {
            log.error("创建角色失败 - roleDTO: {}", roleDTO, e);
            return R.fail("创建角色失败");
        }
    }

    @Override
    @Transactional
    public R<RoleDTO> updateRole(Long roleId, RoleDTO roleDTO) {
        try {
            log.info("更新角色 - roleId: {}, name: {}", roleId, roleDTO.getName());

            if (roleId == null || roleDTO == null) {
                return R.fail("角色ID和角色信息不能为空");
            }

            // 查询角色是否存在
            Optional<Role> optionalRole = roleRepository.findById(roleId);
            if (optionalRole.isEmpty()) {
                return R.fail("角色不存在");
            }

            Role existingRole = optionalRole.get();

            // 如果是系统默认角色，不允许修改名称
            if (Boolean.TRUE.equals(existingRole.getIsSystemDefault()) && 
                !existingRole.getName().equals(roleDTO.getName())) {
                return R.fail("系统默认角色不允许修改名称");
            }

            // 如果修改了名称，检查新名称是否已存在
            if (StringUtils.hasText(roleDTO.getName()) &&
                    !roleDTO.getName().equals(existingRole.getName())) {
                if (roleRepository.existsByName(roleDTO.getName())) {
                    return R.fail("角色名称已存在: " + roleDTO.getName());
                }
            }

            // 更新角色信息
            roleMapper.updateRole(existingRole, roleDTO);
            Role updatedRole = roleRepository.save(existingRole);

            // 清理缓存
            clearRoleCache(roleId);
            clearAllUserRolesCache();

            RoleDTO result = roleMapper.toDTO(updatedRole);

            log.info("更新角色成功 - roleId: {}, name: {}", roleId, updatedRole.getName());
            return R.ok(result, "角色更新成功");
        } catch (Exception e) {
            log.error("更新角色失败 - roleId: {}, roleDTO: {}", roleId, roleDTO, e);
            return R.fail("更新角色失败");
        }
    }

    @Override
    @Transactional
    public R<Void> deleteRole(Long roleId) {
        try {
            log.info("删除角色 - roleId: {}", roleId);

            if (roleId == null) {
                return R.fail("角色ID不能为空");
            }

            // 查询角色是否存在
            Optional<Role> optionalRole = roleRepository.findById(roleId);
            if (optionalRole.isEmpty()) {
                return R.fail("角色不存在");
            }

            Role role = optionalRole.get();

            // 不允许删除系统默认角色
            if (Boolean.TRUE.equals(role.getIsSystemDefault())) {
                return R.fail("不允许删除系统默认角色");
            }

            // 检查是否有用户关联了该角色
            List<UserRole> userRoles = userRoleRepository.findByRoleId(roleId);
            if (!userRoles.isEmpty()) {
                return R.fail("无法删除角色，该角色已被 " + userRoles.size() + " 个用户使用");
            }

            // 删除角色
            roleRepository.delete(role);

            // 清理缓存
            clearRoleCache(roleId);
            clearRolePermissionsCache(roleId);

            log.info("删除角色成功 - roleId: {}, name: {}", roleId, role.getName());
            return R.ok(null, "角色删除成功");
        } catch (Exception e) {
            log.error("删除角色失败 - roleId: {}", roleId, e);
            return R.fail("删除角色失败");
        }
    }

    // ==================== 用户角色管理 ====================

    @Override
    @Transactional(readOnly = true)
    public R<Set<String>> getUserRoles(Long userId) {
        try {
            log.debug("获取用户角色 - userId: {}", userId);

            if (userId == null) {
                return R.fail("用户ID不能为空");
            }

            // 先从缓存获取
            String cacheKey = USER_ROLES_CACHE_PREFIX + userId;
            Set<String> cachedRoles = redisService.getCacheObject(cacheKey);

            if (cachedRoles != null) {
                return R.ok(cachedRoles);
            }

            // 缓存未命中，从数据库查询
            List<Role> roles = roleRepository.findAllByUserId(userId);
            Set<String> roleNames = roles.stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());

            // 缓存用户角色
            redisService.setCacheObject(cacheKey, roleNames, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);

            log.debug("获取用户角色成功 - userId: {}, 角色数: {}", userId, roleNames.size());
            return R.ok(roleNames);
        } catch (Exception e) {
            log.error("获取用户角色失败 - userId: {}", userId, e);
            return R.fail("获取用户角色失败");
        }
    }

    @Override
    @Transactional
    public R<Void> assignRolesToUser(Long userId, List<Long> roleIds) {
        try {
            log.info("为用户分配角色 - userId: {}, roleIds: {}", userId, roleIds);

            if (userId == null || roleIds == null || roleIds.isEmpty()) {
                return R.fail("用户ID和角色ID列表不能为空");
            }

            // 验证用户是否存在
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                return R.fail("用户不存在");
            }

            User user = optionalUser.get();

            // 验证角色是否存在
            List<Role> roles = roleRepository.findAllById(roleIds);
            if (roles.size() != roleIds.size()) {
                return R.fail("部分角色不存在");
            }

            // 查询用户已有的角色
            List<UserRole> existingUserRoles = userRoleRepository.findByUserId(userId);
            Set<Long> existingRoleIds = existingUserRoles.stream()
                    .map(ur -> ur.getRole().getId())
                    .collect(Collectors.toSet());

            // 过滤掉已存在的角色
            List<Long> newRoleIds = roleIds.stream()
                    .filter(roleId -> !existingRoleIds.contains(roleId))
                    .collect(Collectors.toList());

            if (newRoleIds.isEmpty()) {
                return R.ok(null, "用户已拥有所有指定角色");
            }

            // 创建新的用户角色关联
            List<UserRole> newUserRoles = newRoleIds.stream()
                    .map(roleId -> {
                        Role role = roles.stream()
                                .filter(r -> r.getId().equals(roleId))
                                .findFirst()
                                .orElse(null);

                        return UserRole.builder()
                                .id(SnowflakeIdUtil.nextId())
                                .user(user)
                                .role(role)
                                .build();
                    })
                    .collect(Collectors.toList());

            userRoleRepository.saveAll(newUserRoles);

            // 清理缓存
            clearUserRolesCache(userId);
            clearUserPermissionsCache(userId);

            log.info("为用户分配角色成功 - userId: {}, 新增角色数: {}", userId, newUserRoles.size());
            return R.ok(null, "角色分配成功");
        } catch (Exception e) {
            log.error("为用户分配角色失败 - userId: {}, roleIds: {}", userId, roleIds, e);
            return R.fail("角色分配失败");
        }
    }

    @Override
    @Transactional
    public R<Void> removeRolesFromUser(Long userId, List<Long> roleIds) {
        try {
            log.info("移除用户角色 - userId: {}, roleIds: {}", userId, roleIds);

            if (userId == null || roleIds == null || roleIds.isEmpty()) {
                return R.fail("用户ID和角色ID列表不能为空");
            }

            // 删除用户角色关联
            int deletedCount = 0;
            for (Long roleId : roleIds) {
                deletedCount += userRoleRepository.deleteByUserIdAndRoleId(userId, roleId);
            }

            // 清理缓存
            clearUserRolesCache(userId);
            clearUserPermissionsCache(userId);

            log.info("移除用户角色成功 - userId: {}, 删除数量: {}", userId, deletedCount);
            return R.ok(null, "角色移除成功");
        } catch (Exception e) {
            log.error("移除用户角色失败 - userId: {}, roleIds: {}", userId, roleIds, e);
            return R.fail("角色移除失败");
        }
    }

    @Override
    @Transactional
    public R<Void> assignSystemRoleToUser(Long userId, SysRole sysRole) {
        try {
            log.info("为用户分配系统角色 - userId: {}, sysRole: {}", userId, sysRole.getRoleName());

            if (userId == null || sysRole == null) {
                return R.fail("用户ID和系统角色不能为空");
            }

            // 验证用户是否存在
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                return R.fail("用户不存在");
            }

            User user = optionalUser.get();

            // 查找或创建系统角色
            Role systemRole = findOrCreateSystemRole(sysRole);
            if (systemRole == null) {
                return R.fail("系统角色创建失败");
            }

            // 检查用户是否已有该角色
            Optional<UserRole> existingUserRole = userRoleRepository.findByUserIdAndRoleId(userId, systemRole.getId());
            if (existingUserRole.isPresent()) {
                return R.ok(null, "用户已拥有该角色");
            }

            // 创建用户角色关联
            UserRole userRole = UserRole.builder()
                    .id(SnowflakeIdUtil.nextId())
                    .user(user)
                    .role(systemRole)
                    .build();

            userRoleRepository.save(userRole);

            // 清理缓存
            clearUserRolesCache(userId);
            clearUserPermissionsCache(userId);

            log.info("为用户分配系统角色成功 - userId: {}, sysRole: {}", userId, sysRole.getRoleName());
            return R.ok(null, "系统角色分配成功");
        } catch (Exception e) {
            log.error("为用户分配系统角色失败 - userId: {}, sysRole: {}", userId, sysRole, e);
            return R.fail("系统角色分配失败");
        }
    }

    // ==================== 角色权限管理 ====================

    @Override
    @Transactional
    public R<Void> assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        try {
            log.info("为角色分配权限 - roleId: {}, permissionIds: {}", roleId, permissionIds);

            if (roleId == null || permissionIds == null || permissionIds.isEmpty()) {
                return R.fail("角色ID和权限ID列表不能为空");
            }

            // 验证角色是否存在
            Optional<Role> optionalRole = roleRepository.findById(roleId);
            if (optionalRole.isEmpty()) {
                return R.fail("角色不存在");
            }

            Role role = optionalRole.get();

            // 验证权限是否存在
            List<Permission> permissions = permissionRepository.findAllById(permissionIds);
            if (permissions.size() != permissionIds.size()) {
                return R.fail("部分权限不存在");
            }

            // 查询角色已有的权限
            List<RolePermission> existingRolePermissions = rolePermissionRepository.findByRoleId(roleId);
            Set<Long> existingPermissionIds = existingRolePermissions.stream()
                    .map(rp -> rp.getPermission().getId())
                    .collect(Collectors.toSet());

            // 过滤掉已存在的权限
            List<Long> newPermissionIds = permissionIds.stream()
                    .filter(permissionId -> !existingPermissionIds.contains(permissionId))
                    .collect(Collectors.toList());

            if (newPermissionIds.isEmpty()) {
                return R.ok(null, "角色已拥有所有指定权限");
            }

            // 创建新的角色权限关联
            List<RolePermission> newRolePermissions = newPermissionIds.stream()
                    .map(permissionId -> {
                        Permission permission = permissions.stream()
                                .filter(p -> p.getId().equals(permissionId))
                                .findFirst()
                                .orElse(null);

                        return RolePermission.builder()
                                .id(SnowflakeIdUtil.nextId())
                                .role(role)
                                .permission(permission)
                                .build();
                    })
                    .collect(Collectors.toList());

            rolePermissionRepository.saveAll(newRolePermissions);

            // 清理缓存
            clearRolePermissionsCache(roleId);
            clearAllUserPermissionsCache();

            log.info("为角色分配权限成功 - roleId: {}, 新增权限数: {}", roleId, newRolePermissions.size());
            return R.ok(null, "权限分配成功");
        } catch (Exception e) {
            log.error("为角色分配权限失败 - roleId: {}, permissionIds: {}", roleId, permissionIds, e);
            return R.fail("权限分配失败");
        }
    }

    @Override
    @Transactional
    public R<Void> removePermissionsFromRole(Long roleId, List<Long> permissionIds) {
        try {
            log.info("移除角色权限 - roleId: {}, permissionIds: {}", roleId, permissionIds);

            if (roleId == null || permissionIds == null || permissionIds.isEmpty()) {
                return R.fail("角色ID和权限ID列表不能为空");
            }

            // 删除角色权限关联
            int deletedCount = 0;
            for (Long permissionId : permissionIds) {
                deletedCount += rolePermissionRepository.deleteByRoleIdAndPermissionId(roleId, permissionId);
            }

            // 清理缓存
            clearRolePermissionsCache(roleId);
            clearAllUserPermissionsCache();

            log.info("移除角色权限成功 - roleId: {}, 删除数量: {}", roleId, deletedCount);
            return R.ok(null, "权限移除成功");
        } catch (Exception e) {
            log.error("移除角色权限失败 - roleId: {}, permissionIds: {}", roleId, permissionIds, e);
            return R.fail("权限移除失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public R<Set<String>> getRolePermissions(Long roleId) {
        try {
            log.debug("获取角色权限 - roleId: {}", roleId);

            if (roleId == null) {
                return R.fail("角色ID不能为空");
            }

            // 先从缓存获取
            String cacheKey = ROLE_PERMISSIONS_CACHE_PREFIX + roleId;
            Set<String> cachedPermissions = redisService.getCacheObject(cacheKey);

            if (cachedPermissions != null) {
                return R.ok(cachedPermissions);
            }

            // 缓存未命中，从数据库查询
            List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleId(roleId);
            Set<String> permissionNames = rolePermissions.stream()
                    .map(rp -> rp.getPermission().getName())
                    .collect(Collectors.toSet());

            // 缓存角色权限
            redisService.setCacheObject(cacheKey, permissionNames, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);

            log.debug("获取角色权限成功 - roleId: {}, 权限数: {}", roleId, permissionNames.size());
            return R.ok(permissionNames);
        } catch (Exception e) {
            log.error("获取角色权限失败 - roleId: {}", roleId, e);
            return R.fail("获取角色权限失败");
        }
    }

    // ==================== 角色用户查询 ====================

    @Override
    @Transactional(readOnly = true)
    public R<Page<Long>> getRoleUsers(Long roleId, Pageable pageable) {
        try {
            log.debug("获取角色用户 - roleId: {}", roleId);

            if (roleId == null) {
                return R.fail("角色ID不能为空");
            }

            // 查询拥有该角色的用户
            List<UserRole> userRoles = userRoleRepository.findByRoleId(roleId);
            List<Long> userIds = userRoles.stream()
                    .map(ur -> ur.getUser().getId())
                    .collect(Collectors.toList());

            // 分页处理
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), userIds.size());
            List<Long> pagedUserIds = userIds.subList(start, end);

            Page<Long> result = new PageImpl<>(pagedUserIds, pageable, userIds.size());

            log.debug("获取角色用户成功 - roleId: {}, 总数: {}", roleId, userIds.size());
            return R.ok(result);
        } catch (Exception e) {
            log.error("获取角色用户失败 - roleId: {}", roleId, e);
            return R.fail("获取角色用户失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public R<Long> countRoleUsers(Long roleId) {
        try {
            log.debug("统计角色用户数 - roleId: {}", roleId);

            if (roleId == null) {
                return R.fail("角色ID不能为空");
            }

            List<UserRole> userRoles = userRoleRepository.findByRoleId(roleId);
            long count = userRoles.size();

            log.debug("统计角色用户数成功 - roleId: {}, count: {}", roleId, count);
            return R.ok(count);
        } catch (Exception e) {
            log.error("统计角色用户数失败 - roleId: {}", roleId, e);
            return R.fail("统计角色用户数失败");
        }
    }

    // ==================== 系统初始化 ====================

    @Override
    @Transactional
    public R<Void> initializeSystemRoles() {
        try {
            log.info("初始化系统角色");

            // 创建三个系统角色
            for (SysRole sysRole : SysRole.values()) {
                Optional<Role> existingRole = roleRepository.findByName(sysRole.getRoleName());
                
                if (existingRole.isEmpty()) {
                    // 创建角色
                    Role role = Role.builder()
                            .id(SnowflakeIdUtil.nextId())
                            .name(sysRole.getRoleName())
                            .description(sysRole.getDescription())
                            .roleType("SYSTEM")
                            .isSystemDefault(true)
                            .build();

                    role = roleRepository.save(role);

                    // 分配权限
                    assignSystemRolePermissions(role, sysRole);

                    log.info("创建系统角色成功 - {}", sysRole.getRoleName());
                } else {
                    log.info("系统角色已存在 - {}", sysRole.getRoleName());
                }
            }

            log.info("系统角色初始化完成");
            return R.ok(null, "系统角色初始化完成");
        } catch (Exception e) {
            log.error("初始化系统角色失败", e);
            return R.fail("系统角色初始化失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public R<Boolean> isSystemRolesInitialized() {
        try {
            // 检查三个系统角色是否都存在
            for (SysRole sysRole : SysRole.values()) {
                if (roleRepository.findByName(sysRole.getRoleName()).isEmpty()) {
                    return R.ok(false);
                }
            }

            return R.ok(true);
        } catch (Exception e) {
            log.error("检查系统角色初始化状态失败", e);
            return R.fail("检查系统角色初始化状态失败");
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 查找或创建系统角色
     */
    private Role findOrCreateSystemRole(SysRole sysRole) {
        try {
            Optional<Role> optionalRole = roleRepository.findByName(sysRole.getRoleName());
            
            if (optionalRole.isPresent()) {
                return optionalRole.get();
            }

            // 创建新的系统角色
            Role role = Role.builder()
                    .id(SnowflakeIdUtil.nextId())
                    .name(sysRole.getRoleName())
                    .description(sysRole.getDescription())
                    .roleType("SYSTEM")
                    .isSystemDefault(true)
                    .build();

            role = roleRepository.save(role);

            // 分配权限
            assignSystemRolePermissions(role, sysRole);

            return role;
        } catch (Exception e) {
            log.error("查找或创建系统角色失败 - sysRole: {}", sysRole, e);
            return null;
        }
    }

    /**
     * 为系统角色分配权限
     */
    private int assignSystemRolePermissions(Role role, SysRole sysRole) {
        try {
            List<SystemPermission> permissions = sysRole.getPermissions();
            if (permissions == null || permissions.isEmpty()) {
                return 0;
            }

            // 查找权限实体
            List<String> permissionNames = permissions.stream()
                    .map(SystemPermission::getPermission)
                    .collect(Collectors.toList());

            List<Permission> permissionEntities = permissionRepository.findByNameIn(permissionNames);

            // 创建角色权限关联
            List<RolePermission> rolePermissions = permissionEntities.stream()
                    .map(permission -> RolePermission.builder()
                            .id(SnowflakeIdUtil.nextId())
                            .role(role)
                            .permission(permission)
                            .build())
                    .collect(Collectors.toList());

            rolePermissionRepository.saveAll(rolePermissions);
            
            return rolePermissions.size();
        } catch (Exception e) {
            log.error("分配系统角色权限失败 - roleId: {}, sysRole: {}", role.getId(), sysRole, e);
            return 0;
        }
    }

    /**
     * 清理角色缓存
     */
    private void clearRoleCache(Long roleId) {
        try {
            String cacheKey = ROLE_CACHE_PREFIX + roleId;
            redisService.deleteObject(cacheKey);
        } catch (Exception e) {
            log.warn("清理角色缓存失败 - roleId: {}", roleId, e);
        }
    }

    /**
     * 清理用户角色缓存
     */
    private void clearUserRolesCache(Long userId) {
        try {
            String cacheKey = USER_ROLES_CACHE_PREFIX + userId;
            redisService.deleteObject(cacheKey);
        } catch (Exception e) {
            log.warn("清理用户角色缓存失败 - userId: {}", userId, e);
        }
    }

    /**
     * 清理所有用户角色缓存
     */
    private void clearAllUserRolesCache() {
        try {
            Collection<String> keys = redisService.keys(USER_ROLES_CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisService.deleteObject(keys);
            }
        } catch (Exception e) {
            log.warn("清理所有用户角色缓存失败", e);
        }
    }

    /**
     * 清理角色权限缓存
     */
    private void clearRolePermissionsCache(Long roleId) {
        try {
            String cacheKey = ROLE_PERMISSIONS_CACHE_PREFIX + roleId;
            redisService.deleteObject(cacheKey);
        } catch (Exception e) {
            log.warn("清理角色权限缓存失败 - roleId: {}", roleId, e);
        }
    }

    /**
     * 清理用户权限缓存
     */
    private void clearUserPermissionsCache(Long userId) {
        try {
            String cacheKey = "user:permissions:" + userId;
            redisService.deleteObject(cacheKey);
        } catch (Exception e) {
            log.warn("清理用户权限缓存失败 - userId: {}", userId, e);
        }
    }

    /**
     * 清理所有用户权限缓存
     */
    private void clearAllUserPermissionsCache() {
        try {
            Collection<String> keys = redisService.keys("user:permissions:*");
            if (keys != null && !keys.isEmpty()) {
                redisService.deleteObject(keys);
            }
        } catch (Exception e) {
            log.warn("清理所有用户权限缓存失败", e);
        }
    }
}
