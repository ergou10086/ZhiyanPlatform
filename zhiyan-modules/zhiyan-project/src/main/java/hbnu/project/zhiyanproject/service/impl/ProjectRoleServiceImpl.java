package hbnu.project.zhiyanproject.service.impl;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonbasic.utils.id.SnowflakeIdUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ProjectRoleServiceImpl {
    // ========== 项目角色专用方法 ==========

    @Override
    @Transactional
    public R<RoleDTO> createProjectRole(ProjectRole projectRole, String customRoleName, Long projectId) {
        try {
            String finalRoleName = StringUtils.hasText(customRoleName) ? customRoleName : projectRole.getRoleName();

            // 检查角色名称是否已存在
            if (roleRepository.existsByName(finalRoleName)) {
                return R.fail("角色名称已存在: " + finalRoleName);
            }

            // 创建项目角色
            Role role = Role.builder()
                    .id(SnowflakeIdUtil.nextId())
                    .name(finalRoleName)
                    .description(projectRole.getDescription())
                    .build();

            role = roleRepository.save(role);

            // 应用项目角色权限
            int assignedCount = assignProjectRolePermissions(role, projectRole);

            RoleDTO roleDTO = roleMapper.toDTO(role);

            log.info("成功创建项目角色: {} -> {}, 项目ID: {}, 分配权限: {}",
                    projectRole.getRoleName(), finalRoleName, projectId, assignedCount);

            return R.ok(roleDTO, String.format("成功创建项目角色 '%s' 并分配 %d 个权限",
                    finalRoleName, assignedCount));
        } catch (Exception e) {
            log.error("创建项目角色失败: projectRole={}, customRoleName={}, projectId={}",
                    projectRole, customRoleName, projectId, e);
            return R.fail("项目角色创建失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public R<Integer> applyProjectRole(Long roleId, ProjectRole projectRole, boolean resetMode, Long projectId) {
        try {
            Role role = findById(roleId);
            if (role == null) {
                return R.fail("角色不存在");
            }

            int assignedCount;
            if (resetMode) {
                // 重置模式：清空现有权限后重新分配
                clearRolePermissions(roleId);
                assignedCount = assignProjectRolePermissions(role, projectRole);
            } else {
                // 增量模式：在现有权限基础上添加
                assignedCount = assignProjectRolePermissions(role, projectRole);
            }

            // 清理相关缓存
            clearRolePermissionsCache(roleId);
            clearAllUserPermissionsCache();

            String mode = resetMode ? "重置" : "增量";
            return R.ok(assignedCount, String.format("成功以%s模式为角色 '%s' 应用项目角色 '%s'，分配 %d 个权限",
                    mode, role.getName(), projectRole.getRoleName(), assignedCount));
        } catch (Exception e) {
            log.error("应用项目角色失败: roleId={}, projectRole={}, resetMode={}, projectId={}",
                    roleId, projectRole, resetMode, projectId, e);
            return R.fail("项目角色应用失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public R<Void> assignProjectRoleToUser(Long userId, Long projectId, ProjectRole projectRole) {
        try {
            // 验证用户是否存在
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return R.fail("用户不存在: " + userId);
            }

            // 查找或创建项目角色
            Role projectRoleEntity = findOrCreateProjectRole(projectRole, projectId);
            if (projectRoleEntity == null) {
                return R.fail("项目角色创建失败");
            }

            // 检查用户是否已有该角色
            UserRole existingUserRole = userRoleRepository.findByUserIdAndRoleId(userId, projectRoleEntity.getId()).orElse(null);
            if (existingUserRole != null) {
                return R.ok(null, "用户已拥有该角色");
            }

            // 创建用户角色关联
            UserRole userRole = UserRole.builder()
                    .id(SnowflakeIdUtil.nextId())
                    .user(user)
                    .role(projectRoleEntity)
                    .build();

            userRoleRepository.save(userRole);

            // 清理相关缓存
            clearUserRolesCache(userId);
            clearUserPermissionsCache(userId);
            clearUserProjectRolesCache(userId, projectId);

            log.info("为用户[{}]在项目[{}]中分配角色[{}]成功", userId, projectId, projectRole.getRoleName());
            return R.ok(null, "项目角色分配成功");
        } catch (Exception e) {
            log.error("为用户分配项目角色失败: userId={}, projectId={}, projectRole={}",
                    userId, projectId, projectRole, e);
            return R.fail("项目角色分配失败");
        }
    }

    @Override
    @Transactional
    public R<Void> removeUserFromProject(Long userId, Long projectId) {
        try {
            // 查找用户在项目中的所有角色
            List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
            List<Long> projectRoleIds = userRoles.stream()
                    .filter(ur -> isProjectRole(ur.getRole()))
                    .map(ur -> ur.getRole().getId())
                    .collect(Collectors.toList());

            if (projectRoleIds.isEmpty()) {
                return R.ok(null, "用户在该项目中无角色");
            }

            // 删除用户项目角色关联
            int deletedCount = 0;
            for (Long roleId : projectRoleIds) {
                deletedCount += userRoleRepository.deleteByUserIdAndRoleId(userId, roleId);
            }

            // 清理相关缓存
            clearUserRolesCache(userId);
            clearUserPermissionsCache(userId);
            clearUserProjectRolesCache(userId, projectId);

            log.info("移除用户[{}]在项目[{}]中的角色成功，删除了{}条记录", userId, projectId, deletedCount);
            return R.ok(null, "用户项目角色移除成功");
        } catch (Exception e) {
            log.error("移除用户项目角色失败: userId={}, projectId={}", userId, projectId, e);
            return R.fail("用户项目角色移除失败");
        }
    }

    @Override
    public R<Set<String>> getUserProjectRoles(Long userId, Long projectId) {
        try {
            if (userId == null || projectId == null) {
                return R.fail("用户ID和项目ID不能为空");
            }

            // 先从缓存获取
            String cacheKey = USER_PROJECT_ROLES_CACHE_PREFIX + userId + ":" + projectId;
            Set<String> userProjectRoles = redisService.getCacheObject(cacheKey);

            if (userProjectRoles == null) {
                // 缓存未命中，从数据库查询
                List<Role> roles = roleRepository.findAllByUserId(userId);
                userProjectRoles = roles.stream()
                        .filter(this::isProjectRole)
                        .map(Role::getName)
                        .collect(Collectors.toSet());

                // 缓存用户项目角色
                cacheUserProjectRoles(userId, projectId, userProjectRoles);
            }

            log.debug("获取用户[{}]在项目[{}]中的角色列表，共{}个角色", userId, projectId, userProjectRoles.size());
            return R.ok(userProjectRoles);
        } catch (Exception e) {
            log.error("获取用户项目角色失败: userId={}, projectId={}", userId, projectId, e);
            return R.fail("获取用户项目角色失败");
        }
    }

    // ========== 角色类型查询方法 ==========

    @Override
    public R<Page<RoleDTO>> getRolesByType(String roleType, Pageable pageable) {
        try {
            // 这里需要根据角色类型进行查询，可能需要扩展Repository
            // 暂时返回所有角色，后续可以根据实际需求实现
            Page<Role> rolePage = roleRepository.findAll(pageable);
            List<RoleDTO> roleDTOs = roleMapper.toDTOList(rolePage.getContent());

            Page<RoleDTO> result = new PageImpl<>(roleDTOs, pageable, rolePage.getTotalElements());

            log.debug("获取{}角色列表，页码: {}, 大小: {}, 总数: {}",
                    roleType, pageable.getPageNumber(), pageable.getPageSize(), rolePage.getTotalElements());
            return R.ok(result);
        } catch (Exception e) {
            log.error("获取{}角色列表失败", roleType, e);
            return R.fail("获取角色列表失败");
        }
    }

    @Override
    public R<Page<RoleDTO>> getProjectRoles(Long projectId, Pageable pageable) {
        try {
            // 这里需要根据项目ID查询项目角色，可能需要扩展Repository
            // 暂时返回所有角色，后续可以根据实际需求实现
            Page<Role> rolePage = roleRepository.findAll(pageable);
            List<RoleDTO> roleDTOs = roleMapper.toDTOList(rolePage.getContent());

            Page<RoleDTO> result = new PageImpl<>(roleDTOs, pageable, rolePage.getTotalElements());

            log.debug("获取项目[{}]角色列表，页码: {}, 大小: {}, 总数: {}",
                    projectId, pageable.getPageNumber(), pageable.getPageSize(), rolePage.getTotalElements());
            return R.ok(result);
        } catch (Exception e) {
            log.error("获取项目角色列表失败: projectId={}", projectId, e);
            return R.fail("获取项目角色列表失败");
        }
    }

    @Override
    public R<Page<RoleDTO>> getSystemRoles(Pageable pageable) {
        try {
            // 这里需要查询系统角色，可能需要扩展Repository
            // 暂时返回所有角色，后续可以根据实际需求实现
            Page<Role> rolePage = roleRepository.findAll(pageable);
            List<RoleDTO> roleDTOs = roleMapper.toDTOList(rolePage.getContent());

            Page<RoleDTO> result = new PageImpl<>(roleDTOs, pageable, rolePage.getTotalElements());

            log.debug("获取系统角色列表，页码: {}, 大小: {}, 总数: {}",
                    pageable.getPageNumber(), pageable.getPageSize(), rolePage.getTotalElements());
            return R.ok(result);
        } catch (Exception e) {
            log.error("获取系统角色列表失败", e);
            return R.fail("获取系统角色列表失败");
        }
    }

    /**
     * 移除角色的权限模块
     */
    @Override
    @Transactional
    public R<Integer> removePermissionModule(Long roleId, PermissionModule permissionModule) {
        try {
            Role role = findById(roleId);
            if (role == null) {
                return R.fail("角色不存在");
            }

            int removedCount = permissionAssignmentUtil.removePermissionModule(role, permissionModule);

            // 清理相关缓存
            clearRolePermissionsCache(roleId);
            clearAllUserPermissionsCache();

            return R.ok(removedCount, String.format("成功从角色 '%s' 移除权限模块 '%s'，共移除 %d 个权限",
                    role.getName(), permissionModule.getModuleName(), removedCount));
        } catch (Exception e) {
            log.error("移除角色权限模块失败: roleId={}, module={}", roleId, permissionModule, e);
            return R.fail("权限模块移除失败: " + e.getMessage());
        }
    }


    /**
     * 获取角色权限统计信息
     */
    @Override
    public R<PermissionAssignmentUtil.PermissionStatistics> getRolePermissionStatistics(Long roleId) {
        try {
            Role role = findById(roleId);
            if (role == null) {
                return R.fail("角色不存在");
            }

            PermissionAssignmentUtil.PermissionStatistics statistics =
                    permissionAssignmentUtil.getPermissionStatistics(role);

            return R.ok(statistics, "成功获取角色权限统计信息");
        } catch (Exception e) {
            log.error("获取角色权限统计失败: roleId={}", roleId, e);
            return R.fail("获取权限统计失败: " + e.getMessage());
        }
    }


    /**
     * 初始化系统权限数据
     */
    @Override
    @Transactional
    public R<Void> initializeSystemPermissions() {
        try {
            permissionAssignmentUtil.initializeSystemPermissions();
            return R.ok(null, "系统权限初始化完成");
        } catch (Exception e) {
            log.error("初始化系统权限失败", e);
            return R.fail("系统权限初始化失败: " + e.getMessage());
        }
    }

    /**
     * 清空角色权限
     */
    private void clearRolePermissions(Long roleId) {
        try {
            rolePermissionRepository.deleteByRoleId(roleId);
        } catch (Exception e) {
            log.warn("清空角色权限失败: roleId={}", roleId, e);
        }
    }

    /**
     * 分配系统角色权限
     */
    private int assignSystemRolePermissions(Role role, SysRole sysRole) {
        try {
            // 获取系统角色的权限列表
            List<SystemPermission> permissions = sysRole.getPermissions();
            if (permissions == null || permissions.isEmpty()) {
                return 0;
            }

            // 查找权限实体
            List<Permission> permissionEntities = permissionRepository.findByNameIn(
                    permissions.stream()
                            .map(SystemPermission::getPermission)
                            .collect(Collectors.toList())
            );

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
            log.error("分配系统角色权限失败: roleId={}, sysRole={}", role.getId(), sysRole, e);
            return 0;
        }
    }

    /**
     * 分配项目角色权限
     */
    private int assignProjectRolePermissions(Role role, ProjectRole projectRole) {
        try {
            // 获取项目角色的权限列表
            List<SystemPermission> permissions = projectRole.getPermissions();
            if (permissions == null || permissions.isEmpty()) {
                return 0;
            }

            // 查找权限实体
            List<Permission> permissionEntities = permissionRepository.findByNameIn(
                    permissions.stream()
                            .map(SystemPermission::getPermission)
                            .collect(Collectors.toList())
            );

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
            log.error("分配项目角色权限失败: roleId={}, projectRole={}", role.getId(), projectRole, e);
            return 0;
        }
    }

    /**
     * 查找或创建项目角色
     */
    private Role findOrCreateProjectRole(ProjectRole projectRole, Long projectId) {
        try {
            // 构建项目角色名称（包含项目ID）
            String projectRoleName = projectRole.getRoleName() + "_" + projectId;

            // 查找是否已存在
            Role existingRole = roleRepository.findByName(projectRoleName).orElse(null);
            if (existingRole != null) {
                return existingRole;
            }

            // 创建新的项目角色
            Role role = Role.builder()
                    .id(SnowflakeIdUtil.nextId())
                    .name(projectRoleName)
                    .description(projectRole.getDescription())
                    .roleType("PROJECT")
                    .projectId(projectId)
                    .build();

            role = roleRepository.save(role);

            // 分配权限
            assignProjectRolePermissions(role, projectRole);

            return role;
        } catch (Exception e) {
            log.error("查找或创建项目角色失败: projectRole={}, projectId={}", projectRole, projectId, e);
            return null;
        }
    }

    /**
     * 判断是否为项目角色
     */
    private boolean isProjectRole(Role role) {
        return role != null && "PROJECT".equals(role.getRoleType());  // 改为字符串比较
    }

    /**
     * 缓存用户项目角色
     */
    private void cacheUserProjectRoles(Long userId, Long projectId, Set<String> roles) {
        try {
            String cacheKey = USER_PROJECT_ROLES_CACHE_PREFIX + userId + ":" + projectId;
            redisService.setCacheObject(cacheKey, roles, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("缓存用户项目角色失败: userId={}, projectId={}", userId, projectId, e);
        }
    }

    /**
     * 清理用户项目角色缓存
     */
    private void clearUserProjectRolesCache(Long userId, Long projectId) {
        try {
            String cacheKey = USER_PROJECT_ROLES_CACHE_PREFIX + userId + ":" + projectId;
            redisService.deleteObject(cacheKey);
        } catch (Exception e) {
            log.warn("清理用户项目角色缓存失败: userId={}, projectId={}", userId, projectId, e);
        }
    }
}
