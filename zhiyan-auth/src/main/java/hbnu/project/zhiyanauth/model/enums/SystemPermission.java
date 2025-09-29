package hbnu.project.zhiyanauth.model.enums;

/**
 * 系统权限枚举
 * 单条权限在这里管理，方便后期随时管理
 *
 * @author ErgouTree
 */
public enum SystemPermission {
    
    // ============ 基础权限 ============
    /**
     * 个人信息管理 - 所有注册用户都拥有
     */
    PROFILE_MANAGE("profile:manage", "管理个人信息"),
    
    /**
     * 项目创建权限 - 普通用户及以上拥有
     */
    PROJECT_CREATE("project:create", "创建新项目"),
    
    // ============ 项目级权限（基于项目成员身份动态分配） ============
    /**
     * 项目管理权限 - 项目创建者和负责人拥有
     */
    PROJECT_MANAGE("project:manage", "管理项目基本信息、任务、成员"),
    
    /**
     * 项目删除权限 - 仅项目创建者拥有
     */
    PROJECT_DELETE("project:delete", "删除项目"),
    
    /**
     * 知识库管理权限 - 项目团队所有成员拥有
     */
    KNOWLEDGE_MANAGE("knowledge:manage", "管理项目知识库"),
    
    // ============ 系统管理权限 ============
    /**
     * 用户管理权限 - 仅系统管理员拥有
     */
    USER_ADMIN("user:admin", "管理系统用户"),
    
    /**
     * 系统配置权限 - 仅系统管理员拥有
     */
    SYSTEM_ADMIN("system:admin", "系统配置和监控");

    private final String permission;
    private final String description;

    SystemPermission(String permission, String description) {
        this.permission = permission;
        this.description = description;
    }

    public String getPermission() {
        return permission;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 获取权限代码（用于权限判断）
     */
    public String getCode() {
        return this.permission;
    }
}
