package hbnu.project.zhiyanauth.service.impl;

import hbnu.project.zhiyanauth.mapper.PermissionMapper;
import hbnu.project.zhiyanauth.model.dto.PermissionDTO;
import hbnu.project.zhiyanauth.model.entity.Permission;
import hbnu.project.zhiyanauth.model.entity.RolePermission;
import hbnu.project.zhiyanauth.model.enums.SystemPermission;
import hbnu.project.zhiyanauth.repository.PermissionRepository;
import hbnu.project.zhiyanauth.repository.RolePermissionRepository;
import hbnu.project.zhiyanauth.service.PermissionService;
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
 * 权限服务实现类
 * 提供权限管理、权限校验等核心功能
 * 
 * 实现说明：
 * 1. 专注于系统权限管理和校验
 * 2. 使用 Redis 缓存提升查询性能
 * 3. 事务保证数据一致性
 * 4. 详细的日志记录便于问题排查
 * 5. 为API网关和其他微服务提供权限校验接口
 *
 * @author ErgouTree
 * @version 2.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionMapper permissionMapper;
    private final RedisService redisService;

    // 缓存相关常量
    private static final String USER_PERMISSIONS_CACHE_PREFIX = "user:permissions:";
    private static final String PERMISSION_CACHE_PREFIX = "permission:";
    private static final long CACHE_EXPIRE_TIME = 1800L; // 30分钟

    // ==================== 权限校验接口（内部调用） ====================

    @Override
    @Transactional(readOnly = true)
    public R<Boolean> hasPermission(Long userId, String permission) {
        try {
            log.debug("检查用户权限 - userId: {}, permission: {}", userId, permission);

            if (userId == null || !StringUtils.hasText(permission)) {
                return R.ok(false);
            }

            // 先从缓存获取用户权限
            Set<String> userPermissions = getUserPermissionsFromCache(userId);
            
            if (userPermissions == null) {
                // 缓存未命中，从数据库查询
                List<Permission> permissions = permissionRepository.findAllByUserId(userId);
                userPermissions = permissions.stream()
                        .map(Permission::getName)
                        .collect(Collectors.toSet());

                // 缓存用户权限
                cacheUserPermissions(userId, userPermissions);
            }

            boolean hasPermission = userPermissions.contains(permission);
            log.debug("用户[{}]权限[{}]检查结果: {}", userId, permission, hasPermission);

            return R.ok(hasPermission);
        } catch (Exception e) {
            log.error("检查用户权限失败 - userId: {}, permission: {}", userId, permission, e);
            return R.fail("权限检查失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public R<Boolean> hasAnyPermission(Long userId, List<String> permissions) {
        try {
            log.debug("检查用户是否拥有任一权限 - userId: {}, permissions: {}", userId, permissions);

            if (userId == null || permissions == null || permissions.isEmpty()) {
                return R.ok(false);
            }

            // 获取用户所有权限
            R<Set<String>> userPermissionsResult = getUserPermissions(userId);
            if (!R.isSuccess(userPermissionsResult)) {
                return R.fail("获取用户权限失败");
            }

            Set<String> userPermissions = userPermissionsResult.getData();

            // 检查是否拥有任一权限
            boolean hasAny = permissions.stream()
                    .anyMatch(userPermissions::contains);

            log.debug("用户[{}]是否拥有任一权限: {}", userId, hasAny);
            return R.ok(hasAny);
        } catch (Exception e) {
            log.error("检查用户任一权限失败 - userId: {}, permissions: {}", userId, permissions, e);
            return R.fail("权限检查失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public R<Boolean> hasAllPermissions(Long userId, List<String> permissions) {
        try {
            log.debug("检查用户是否拥有所有权限 - userId: {}, permissions: {}", userId, permissions);

            if (userId == null || permissions == null || permissions.isEmpty()) {
                return R.ok(false);
            }

            // 获取用户所有权限
            R<Set<String>> userPermissionsResult = getUserPermissions(userId);
            if (!R.isSuccess(userPermissionsResult)) {
                return R.fail("获取用户权限失败");
            }

            Set<String> userPermissions = userPermissionsResult.getData();

            // 检查是否拥有所有权限
            boolean hasAll = userPermissions.containsAll(permissions);

            log.debug("用户[{}]是否拥有所有权限: {}", userId, hasAll);
            return R.ok(hasAll);
        } catch (Exception e) {
            log.error("检查用户所有权限失败 - userId: {}, permissions: {}", userId, permissions, e);
            return R.fail("权限检查失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public R<Set<String>> getUserPermissions(Long userId) {
        try {
            log.debug("获取用户权限 - userId: {}", userId);

            if (userId == null) {
                return R.fail("用户ID不能为空");
            }

            // 先从缓存获取
            Set<String> userPermissions = getUserPermissionsFromCache(userId);
            
            if (userPermissions == null) {
                // 缓存未命中，从数据库查询
                List<Permission> permissions = permissionRepository.findAllByUserId(userId);
                userPermissions = permissions.stream()
                        .map(Permission::getName)
                        .collect(Collectors.toSet());

                // 缓存用户权限
                cacheUserPermissions(userId, userPermissions);
            }

            log.debug("获取用户权限成功 - userId: {}, 权限数: {}", userId, userPermissions.size());
            return R.ok(userPermissions);
        } catch (Exception e) {
            log.error("获取用户权限失败 - userId: {}", userId, e);
            return R.fail("获取用户权限失败");
        }
    }

    // ==================== 权限信息查询 ====================

    @Override
    @Transactional(readOnly = true)
    public R<Page<PermissionDTO>> getAllPermissions(Pageable pageable) {
        try {
            log.debug("查询所有权限 - 页码: {}, 每页数量: {}", 
                    pageable.getPageNumber(), pageable.getPageSize());

            Page<Permission> permissionPage = permissionRepository.findAll(pageable);
            List<PermissionDTO> permissionDTOs = permissionMapper.toDTOList(permissionPage.getContent());

            Page<PermissionDTO> result = new PageImpl<>(permissionDTOs, pageable, permissionPage.getTotalElements());

            log.debug("查询权限成功 - 总数: {}, 当前页数量: {}", 
                    permissionPage.getTotalElements(), permissionDTOs.size());
            return R.ok(result);
        } catch (Exception e) {
            log.error("查询权限列表失败", e);
            return R.fail("查询权限列表失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public R<PermissionDTO> getPermissionById(Long permissionId) {
        try {
            log.debug("查询权限详情 - permissionId: {}", permissionId);

            if (permissionId == null) {
                return R.fail("权限ID不能为空");
            }

            // 先从缓存获取
            String cacheKey = PERMISSION_CACHE_PREFIX + permissionId;
            Permission cachedPermission = redisService.getCacheObject(cacheKey);

            if (cachedPermission != null) {
                PermissionDTO permissionDTO = permissionMapper.toDTO(cachedPermission);
                return R.ok(permissionDTO);
            }

            // 缓存未命中，从数据库查询
            Optional<Permission> optionalPermission = permissionRepository.findById(permissionId);
            if (optionalPermission.isEmpty()) {
                log.warn("权限不存在 - permissionId: {}", permissionId);
                return R.fail("权限不存在");
            }

            Permission permission = optionalPermission.get();
            
            // 缓存权限信息
            redisService.setCacheObject(cacheKey, permission, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);

            PermissionDTO permissionDTO = permissionMapper.toDTO(permission);
            return R.ok(permissionDTO);
        } catch (Exception e) {
            log.error("查询权限详情失败 - permissionId: {}", permissionId, e);
            return R.fail("查询权限详情失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public R<PermissionDTO> getPermissionByName(String permissionName) {
        try {
            log.debug("根据名称查询权限 - permissionName: {}", permissionName);

            if (!StringUtils.hasText(permissionName)) {
                return R.fail("权限名称不能为空");
            }

            Optional<Permission> optionalPermission = permissionRepository.findByName(permissionName);
            if (optionalPermission.isEmpty()) {
                log.warn("权限不存在 - permissionName: {}", permissionName);
                return R.fail("权限不存在");
            }

            Permission permission = optionalPermission.get();
            PermissionDTO permissionDTO = permissionMapper.toDTO(permission);

            return R.ok(permissionDTO);
        } catch (Exception e) {
            log.error("根据名称查询权限失败 - permissionName: {}", permissionName, e);
            return R.fail("查询权限失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Permission findById(Long permissionId) {
        if (permissionId == null) {
            return null;
        }

        try {
            // 先从缓存查找
            String cacheKey = PERMISSION_CACHE_PREFIX + permissionId;
            Permission cachedPermission = redisService.getCacheObject(cacheKey);

            if (cachedPermission != null) {
                return cachedPermission;
            }

            // 缓存未命中，从数据库查询
            Optional<Permission> optionalPermission = permissionRepository.findById(permissionId);
            
            if (optionalPermission.isPresent()) {
                Permission permission = optionalPermission.get();
                // 查询到权限实体时，存入缓存
                redisService.setCacheObject(cacheKey, permission, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
                return permission;
            }

            return null;
        } catch (Exception e) {
            log.error("根据ID查找权限失败 - permissionId: {}", permissionId, e);
            return permissionRepository.findById(permissionId).orElse(null);
        }
    }

    // ==================== 权限管理（CRUD） ====================

    @Override
    @Transactional
    public R<PermissionDTO> createPermission(PermissionDTO permissionDTO) {
        try {
            log.info("创建权限 - name: {}", permissionDTO.getName());

            if (permissionDTO == null || !StringUtils.hasText(permissionDTO.getName())) {
                return R.fail("权限信息不完整");
            }

            // 检查权限名称是否已存在
            if (permissionRepository.existsByName(permissionDTO.getName())) {
                return R.fail("权限名称已存在: " + permissionDTO.getName());
            }

            // 转换为实体
            Permission permission = permissionMapper.fromDTO(permissionDTO);

            // 设置ID
            if (permission.getId() == null) {
                permission.setId(SnowflakeIdUtil.nextId());
            }

            Permission savedPermission = permissionRepository.save(permission);

            // 清理缓存
            clearPermissionCache(savedPermission.getId());

            PermissionDTO result = permissionMapper.toDTO(savedPermission);

            log.info("创建权限成功 - permissionId: {}, name: {}", 
                    savedPermission.getId(), savedPermission.getName());
            return R.ok(result, "权限创建成功");
        } catch (Exception e) {
            log.error("创建权限失败 - permissionDTO: {}", permissionDTO, e);
            return R.fail("创建权限失败");
        }
    }

    @Override
    @Transactional
    public R<PermissionDTO> updatePermission(Long permissionId, PermissionDTO permissionDTO) {
        try {
            log.info("更新权限 - permissionId: {}, name: {}", permissionId, permissionDTO.getName());

            if (permissionId == null || permissionDTO == null) {
                return R.fail("权限ID和权限信息不能为空");
            }

            // 查询权限是否存在
            Optional<Permission> optionalPermission = permissionRepository.findById(permissionId);
            if (optionalPermission.isEmpty()) {
                return R.fail("权限不存在");
            }

            Permission existingPermission = optionalPermission.get();

            // 如果修改了名称，检查新名称是否已存在
            if (StringUtils.hasText(permissionDTO.getName()) &&
                    !permissionDTO.getName().equals(existingPermission.getName())) {
                if (permissionRepository.existsByName(permissionDTO.getName())) {
                    return R.fail("权限名称已存在: " + permissionDTO.getName());
                }
            }

            // 更新权限信息
            permissionMapper.updatePermission(existingPermission, permissionDTO);
            Permission updatedPermission = permissionRepository.save(existingPermission);

            // 清理缓存
            clearPermissionCache(permissionId);
            clearAllUserPermissionsCache();

            PermissionDTO result = permissionMapper.toDTO(updatedPermission);

            log.info("更新权限成功 - permissionId: {}, name: {}", 
                    permissionId, updatedPermission.getName());
            return R.ok(result, "权限更新成功");
        } catch (Exception e) {
            log.error("更新权限失败 - permissionId: {}, permissionDTO: {}", permissionId, permissionDTO, e);
            return R.fail("更新权限失败");
        }
    }

    @Override
    @Transactional
    public R<Void> deletePermission(Long permissionId) {
        try {
            log.info("删除权限 - permissionId: {}", permissionId);

            if (permissionId == null) {
                return R.fail("权限ID不能为空");
            }

            // 查询权限是否存在
            Optional<Permission> optionalPermission = permissionRepository.findById(permissionId);
            if (optionalPermission.isEmpty()) {
                return R.fail("权限不存在");
            }

            Permission permission = optionalPermission.get();

            // 检查是否有角色关联了该权限
            List<RolePermission> rolePermissions = rolePermissionRepository.findByPermissionId(permissionId);
            if (!rolePermissions.isEmpty()) {
                return R.fail("无法删除权限，该权限已被 " + rolePermissions.size() + " 个角色使用");
            }

            // 删除权限
            permissionRepository.delete(permission);

            // 清理缓存
            clearPermissionCache(permissionId);
            clearAllUserPermissionsCache();

            log.info("删除权限成功 - permissionId: {}, name: {}", permissionId, permission.getName());
            return R.ok(null, "权限删除成功");
        } catch (Exception e) {
            log.error("删除权限失败 - permissionId: {}", permissionId, e);
            return R.fail("删除权限失败");
        }
    }

    @Override
    @Transactional
    public R<List<PermissionDTO>> batchCreatePermissions(List<PermissionDTO> permissionDTOs) {
        try {
            log.info("批量创建权限 - 数量: {}", permissionDTOs.size());

            if (permissionDTOs == null || permissionDTOs.isEmpty()) {
                return R.fail("权限列表不能为空");
            }

            List<PermissionDTO> createdPermissions = new ArrayList<>();
            List<String> failedPermissions = new ArrayList<>();

            for (PermissionDTO permissionDTO : permissionDTOs) {
                try {
                    // 检查权限名称是否已存在
                    if (permissionRepository.existsByName(permissionDTO.getName())) {
                        failedPermissions.add(permissionDTO.getName() + " (已存在)");
                        continue;
                    }

                    // 转换为实体
                    Permission permission = permissionMapper.fromDTO(permissionDTO);
                    
                    if (permission.getId() == null) {
                        permission.setId(SnowflakeIdUtil.nextId());
                    }

                    Permission savedPermission = permissionRepository.save(permission);
                    createdPermissions.add(permissionMapper.toDTO(savedPermission));

                    // 清理缓存
                    clearPermissionCache(savedPermission.getId());
                } catch (Exception e) {
                    log.warn("创建权限失败 - name: {}", permissionDTO.getName(), e);
                    failedPermissions.add(permissionDTO.getName() + " (创建失败)");
                }
            }

            // 清理所有用户权限缓存
            clearAllUserPermissionsCache();

            String message = String.format("批量创建权限完成 - 成功: %d, 失败: %d", 
                    createdPermissions.size(), failedPermissions.size());
            
            if (!failedPermissions.isEmpty()) {
                message += ", 失败列表: " + String.join(", ", failedPermissions);
            }

            log.info(message);
            return R.ok(createdPermissions, message);
        } catch (Exception e) {
            log.error("批量创建权限失败", e);
            return R.fail("批量创建权限失败");
        }
    }

    // ==================== 权限角色查询 ====================

    @Override
    @Transactional(readOnly = true)
    public R<Page<Long>> getPermissionRoles(Long permissionId, Pageable pageable) {
        try {
            log.debug("获取权限角色 - permissionId: {}", permissionId);

        if (permissionId == null) {
                return R.fail("权限ID不能为空");
            }

            // 查询拥有该权限的角色
            List<RolePermission> rolePermissions = rolePermissionRepository.findByPermissionId(permissionId);
            List<Long> roleIds = rolePermissions.stream()
                    .map(rp -> rp.getRole().getId())
                    .collect(Collectors.toList());

            // 分页处理
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), roleIds.size());
            List<Long> pagedRoleIds = roleIds.subList(start, end);

            Page<Long> result = new PageImpl<>(pagedRoleIds, pageable, roleIds.size());

            log.debug("获取权限角色成功 - permissionId: {}, 总数: {}", permissionId, roleIds.size());
            return R.ok(result);
        } catch (Exception e) {
            log.error("获取权限角色失败 - permissionId: {}", permissionId, e);
            return R.fail("获取权限角色失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public R<Long> countPermissionRoles(Long permissionId) {
        try {
            log.debug("统计权限角色数 - permissionId: {}", permissionId);

            if (permissionId == null) {
                return R.fail("权限ID不能为空");
            }

            List<RolePermission> rolePermissions = rolePermissionRepository.findByPermissionId(permissionId);
            long count = rolePermissions.size();

            log.debug("统计权限角色数成功 - permissionId: {}, count: {}", permissionId, count);
            return R.ok(count);
        } catch (Exception e) {
            log.error("统计权限角色数失败 - permissionId: {}", permissionId, e);
            return R.fail("统计权限角色数失败");
        }
    }

    // ==================== 系统初始化 ====================

    @Override
    @Transactional
    public R<Void> initializeSystemPermissions() {
        try {
            log.info("初始化系统权限");

            int createdCount = 0;
            int existingCount = 0;

            // 创建所有系统权限
            for (SystemPermission systemPermission : SystemPermission.values()) {
                Optional<Permission> existingPermission = 
                        permissionRepository.findByName(systemPermission.getPermission());
                
                if (existingPermission.isEmpty()) {
                    // 创建权限
                    Permission permission = Permission.builder()
                            .id(SnowflakeIdUtil.nextId())
                            .name(systemPermission.getPermission())
                            .description(systemPermission.getDescription())
                            .build();

                    permissionRepository.save(permission);
                    createdCount++;

                    log.info("创建系统权限成功 - {}", systemPermission.getPermission());
                } else {
                    existingCount++;
                    log.debug("系统权限已存在 - {}", systemPermission.getPermission());
                }
            }

            String message = String.format("系统权限初始化完成 - 新创建: %d, 已存在: %d", 
                    createdCount, existingCount);
            
            log.info(message);
            return R.ok(null, message);
        } catch (Exception e) {
            log.error("初始化系统权限失败", e);
            return R.fail("系统权限初始化失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public R<Boolean> isSystemPermissionsInitialized() {
        try {
            // 检查是否所有系统权限都已存在
            long systemPermissionCount = SystemPermission.values().length;
            long existingPermissionCount = permissionRepository.count();

            // 简单判断：如果数据库中的权限数量大于等于系统权限数量，则认为已初始化
            boolean isInitialized = existingPermissionCount >= systemPermissionCount;

            log.debug("检查系统权限初始化状态 - 系统权限数: {}, 已存在: {}, 已初始化: {}", 
                    systemPermissionCount, existingPermissionCount, isInitialized);

            return R.ok(isInitialized);
        } catch (Exception e) {
            log.error("检查系统权限初始化状态失败", e);
            return R.fail("检查系统权限初始化状态失败");
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 从缓存中获取用户权限
     */
    private Set<String> getUserPermissionsFromCache(Long userId) {
        try {
            String cacheKey = USER_PERMISSIONS_CACHE_PREFIX + userId;
            return redisService.getCacheObject(cacheKey);
        } catch (Exception e) {
            log.warn("从缓存获取用户权限失败 - userId: {}", userId, e);
            return null;
        }
    }

    /**
     * 缓存用户权限
     */
    private void cacheUserPermissions(Long userId, Set<String> permissions) {
        try {
            String cacheKey = USER_PERMISSIONS_CACHE_PREFIX + userId;
            redisService.setCacheObject(cacheKey, permissions, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("缓存用户权限失败 - userId: {}", userId, e);
        }
    }

    /**
     * 清理权限缓存
     */
    private void clearPermissionCache(Long permissionId) {
        try {
            String cacheKey = PERMISSION_CACHE_PREFIX + permissionId;
            redisService.deleteObject(cacheKey);
        } catch (Exception e) {
            log.warn("清理权限缓存失败 - permissionId: {}", permissionId, e);
        }
    }

    /**
     * 清理所有用户权限缓存
     */
    private void clearAllUserPermissionsCache() {
        try {
            Collection<String> keys = redisService.keys(USER_PERMISSIONS_CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisService.deleteObject(keys);
            }
        } catch (Exception e) {
            log.warn("清理所有用户权限缓存失败", e);
        }
    }
}
