package hbnu.project.zhiyanauth.utils;

import hbnu.project.zhiyanauth.model.entity.Permission;
import hbnu.project.zhiyanauth.model.entity.Role;
import hbnu.project.zhiyanauth.model.entity.RolePermission;
import hbnu.project.zhiyanauth.model.enums.PermissionModule;
import hbnu.project.zhiyanauth.model.enums.SystemPermission;
import hbnu.project.zhiyanauth.repository.PermissionRepository;
import hbnu.project.zhiyanauth.repository.RolePermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限分配工具类
 * 提供批量权限分配的便捷方法
 *
 * @author ErgouTree
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionAssignmentUtil {

    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    /**
     * 为角色分配权限模块
     *
     * @param role             角色
     * @param permissionModule 权限模块
     * @return 分配的权限数量
     */
    @Transactional
    public int assignPermissionModule(Role role, PermissionModule permissionModule) {
        log.info("为角色 {} 分配权限模块: {}", role.getName(), permissionModule.getModuleName());

        // 获取权限模块包含的所有权限字符串
        List<String> permissionStrings = permissionModule.getPermissionStrings();

        // 从数据库查找对应的权限实体
        List<Permission> permissions = permissionRepository.findByNameIn(permissionStrings);

        if (permissions.size() != permissionStrings.size()) {
            log.warn("权限模块 {} 中有部分权限在数据库中不存在。期望: {}, 实际找到: {}",
                    permissionModule.getModuleName(), permissionStrings.size(), permissions.size());
        }

        // 获取角色已有的权限
        Set<Long> existingPermissionIds = rolePermissionRepository.findByRole(role)
                .stream()
                .map(rp -> rp.getPermission().getId())
                .collect(Collectors.toSet());

        // 过滤出需要新增的权限
        List<Permission> newPermissions = permissions.stream()
                .filter(permission -> !existingPermissionIds.contains(permission.getId()))
                .toList();

        // 批量创建角色权限关联
        List<RolePermission> rolePermissions = newPermissions.stream()
                .map(permission -> RolePermission.builder()
                        .role(role)
                        .permission(permission)
                        .build())
                .collect(Collectors.toList());

        rolePermissionRepository.saveAll(rolePermissions);

        log.info("成功为角色 {} 分配 {} 个新权限", role.getName(), rolePermissions.size());
        return rolePermissions.size();
    }


    /**
     * 为角色分配多个权限模块
     *
     * @param role              角色
     * @param permissionModules 权限模块列表
     * @return 分配的权限数量
     */
    @Transactional
    public int assignPermissionModules(Role role, List<PermissionModule> permissionModules) {
        log.info("为角色 {} 分配 {} 个权限模块", role.getName(), permissionModules.size());

        int totalAssigned = 0;
        for (PermissionModule module : permissionModules) {
            totalAssigned += assignPermissionModule(role, module);
        }

        log.info("为角色 {} 总共分配了 {} 个权限", role.getName(), totalAssigned);
        return totalAssigned;
    }


    /**
     * 根据角色模板为角色分配权限
     *
     * @param role         角色
     * @param roleTemplate 角色模板
     * @return 分配的权限数量
     */
    @Transactional
    public int assignRoleTemplate(Role role, RoleTemplate roleTemplate) {
        log.info("为角色 {} 应用角色模板: {}", role.getName(), roleTemplate.getRoleName());

        return assignPermissionModules(role, roleTemplate.getPermissionModules());
    }


    /**
     * 移除角色的权限模块
     *
     * @param role             角色
     * @param permissionModule 权限模块
     * @return 移除的权限数量
     */
    @Transactional
    public int removePermissionModule(Role role, PermissionModule permissionModule) {
        log.info("从角色 {} 移除权限模块: {}", role.getName(), permissionModule.getModuleName());

        // 获取权限模块包含的所有权限字符串
        List<String> permissionStrings = permissionModule.getPermissionStrings();

        // 查找并删除对应的角色权限关联
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleAndPermissionNameIn(role, permissionStrings);
        rolePermissionRepository.deleteAll(rolePermissions);

        log.info("成功从角色 {} 移除 {} 个权限", role.getName(), rolePermissions.size());
        return rolePermissions.size();
    }


    /**
     * 重置角色权限（清空后重新分配）
     *
     * @param role         角色
     * @param roleTemplate 角色模板
     * @return 分配的权限数量
     */
    @Transactional
    public int resetRolePermissions(Role role, RoleTemplate roleTemplate) {
        log.info("重置角色 {} 的权限，应用模板: {}", role.getName(), roleTemplate.getRoleName());

        // 清空角色所有权限
        List<RolePermission> existingPermissions = rolePermissionRepository.findByRole(role);
        rolePermissionRepository.deleteAll(existingPermissions);
        log.info("清空角色 {} 的 {} 个现有权限", role.getName(), existingPermissions.size());

        // 重新分配权限
        return assignRoleTemplate(role, roleTemplate);
    }


    /**
     * 获取角色权限统计信息
     *
     * @param role 角色
     * @return 权限统计信息
     */
    public PermissionStatistics getPermissionStatistics(Role role) {
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRole(role);

        // 按权限模块分组统计
        Map<String, List<String>> modulePermissions = rolePermissions.stream()
                .map(rp -> rp.getPermission().getName())
                .collect(Collectors.groupingBy(this::getPermissionModule));

        return PermissionStatistics.builder()
                .roleName(role.getName())
                .totalPermissions(rolePermissions.size())
                .modulePermissions(modulePermissions)
                .build();
    }


    /**
     * 根据权限字符串推断权限模块
     */
    private String getPermissionModule(String permission) {
        if (permission.startsWith("user:")) return "用户管理";
        if (permission.startsWith("role:")) return "角色管理";
        if (permission.startsWith("permission:")) return "权限管理";
        if (permission.startsWith("project:")) return "项目管理";
        if (permission.startsWith("task:")) return "任务管理";
        if (permission.startsWith("artifact:")) return "成果管理";
        if (permission.startsWith("wiki:")) return "Wiki管理";
        if (permission.startsWith("system:")) return "系统管理";
        return "其他";
    }


    /**
     * 权限统计信息
     */
    @lombok.Data
    @lombok.Builder
    public static class PermissionStatistics {
        private String roleName;
        private int totalPermissions;
        private Map<String, List<String>> modulePermissions;
    }


    /**
     * 检查权限是否存在于数据库中
     *
     * @param systemPermissions 系统权限列表
     * @return 存在的权限列表
     */
    public List<Permission> validatePermissions(List<SystemPermission> systemPermissions) {
        List<String> permissionStrings = systemPermissions.stream()
                .map(SystemPermission::getPermission)
                .collect(Collectors.toList());

        List<Permission> existingPermissions = permissionRepository.findByNameIn(permissionStrings);

        if (existingPermissions.size() != systemPermissions.size()) {
            List<String> existingNames = existingPermissions.stream()
                    .map(Permission::getName)
                    .toList();

            List<String> missingPermissions = permissionStrings.stream()
                    .filter(name -> !existingNames.contains(name))
                    .collect(Collectors.toList());

            log.warn("以下权限在数据库中不存在: {}", missingPermissions);
        }

        return existingPermissions;
    }


    /**
     * 初始化系统权限到数据库
     * 确保所有SystemPermission枚举中的权限都存在于数据库中
     */
    @Transactional
    public void initializeSystemPermissions() {
        log.info("开始初始化系统权限到数据库");

        for (SystemPermission systemPermission : SystemPermission.values()) {
            Optional<Permission> existingPermissionOpt = permissionRepository.findByName(systemPermission.getPermission());
            if (existingPermissionOpt.isEmpty()) {
                Permission permission = Permission.builder()
                        .name(systemPermission.getPermission())
                        .description(systemPermission.getDescription())
                        .build();
                permissionRepository.save(permission);
                log.info("创建权限: {} - {}", systemPermission.getPermission(), systemPermission.getDescription());
            }
        }

        log.info("系统权限初始化完成");
    }
}
