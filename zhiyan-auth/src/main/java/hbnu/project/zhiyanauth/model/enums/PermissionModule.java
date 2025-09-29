package hbnu.project.zhiyanauth.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限组枚举类
 * 代表一组的权限，按照需求打包给角色使用
 *
 * @author ErgouTree
 */
public enum PermissionModule {
    
    /**
     * 基础用户权限模块
     */
    BASIC_USER("基础用户权限", "普通用户的基础权限", Arrays.asList(
            SystemPermission.PROFILE_MANAGE,
            SystemPermission.PROJECT_CREATE
    )),
    
    /**
     * 项目管理者权限模块
     */
    PROJECT_MANAGER("项目管理权限", "项目管理相关权限", Arrays.asList(
            SystemPermission.PROJECT_MANAGE,
            SystemPermission.KNOWLEDGE_MANAGE
    )),
    
    /**
     * 项目拥有者权限模块
     */
    PROJECT_OWNER("项目拥有者权限", "项目拥有者的完整权限", Arrays.asList(
            SystemPermission.PROJECT_MANAGE,
            SystemPermission.PROJECT_DELETE,
            SystemPermission.KNOWLEDGE_MANAGE
    )),
    
    /**
     * 系统管理员权限模块
     */
    SYSTEM_ADMIN("系统管理员权限", "系统管理员的完整权限", Arrays.asList(
            SystemPermission.PROFILE_MANAGE,
            SystemPermission.PROJECT_CREATE,
            SystemPermission.PROJECT_MANAGE,
            SystemPermission.PROJECT_DELETE,
            SystemPermission.KNOWLEDGE_MANAGE,
            SystemPermission.USER_ADMIN,
            SystemPermission.SYSTEM_ADMIN
    ));

    private final String moduleName;
    private final String description;
    private final List<SystemPermission> permissions;

    PermissionModule(String moduleName, String description, List<SystemPermission> permissions) {
        this.moduleName = moduleName;
        this.description = description;
        this.permissions = permissions;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getDescription() {
        return description;
    }

    public List<SystemPermission> getPermissions() {
        return permissions;
    }

    /**
     * 获取权限字符串列表
     */
    public List<String> getPermissionStrings() {
        return permissions.stream()
                .map(SystemPermission::getPermission)
                .collect(Collectors.toList());
    }

    /**
     * 获取权限数量
     */
    public int getPermissionCount() {
        return permissions.size();
    }

    /**
     * 检查是否包含指定权限
     */
    public boolean containsPermission(SystemPermission permission) {
        return permissions.contains(permission);
    }

    /**
     * 检查是否包含指定权限字符串
     */
    public boolean containsPermission(String permissionString) {
        return permissions.stream()
                .anyMatch(p -> p.getPermission().equals(permissionString));
    }
}
