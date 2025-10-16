package hbnu.project.zhiyanproject.model.enums;

/**
 * 项目权限枚举
 * 定义项目模块中的所有权限
 *
 * @author Tokito
 */
public enum ProjectPermission {
    
    // ============ 基础权限 ============
    /**
     * 个人信息管理 - 所有注册用户都拥有
     */
    PROFILE_MANAGE("profile:manage", "管理个人信息"),
    
    /**
     * 项目创建权限 - 普通用户及以上拥有
     */
    PROJECT_CREATE("project:create", "创建新项目"),
    
    // ============ 项目级权限 ============
    /**
     * 项目管理权限 - 项目创建者和成员拥有
     */
    PROJECT_MANAGE("project:manage", "管理项目基本信息"),
    
    /**
     * 项目删除权限 - 仅项目创建者拥有
     */
    PROJECT_DELETE("project:delete", "删除项目"),
    
    /**
     * 知识库管理权限 - 项目团队所有成员拥有
     */
    KNOWLEDGE_MANAGE("knowledge:manage", "管理项目知识库"),
    
    /**
     * 任务管理权限 - 项目成员拥有
     */
    TASK_MANAGE("task:manage", "管理项目任务"),
    
    /**
     * 任务创建权限 - 项目成员拥有
     */
    TASK_CREATE("task:create", "创建项目任务"),

    /**
     * 成员管理权限 - 项目拥有者拥有
     */
    MEMBER_MANAGE("member:manage", "管理项目成员");

    private final String permission;
    private final String description;

    ProjectPermission(String permission, String description) {
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