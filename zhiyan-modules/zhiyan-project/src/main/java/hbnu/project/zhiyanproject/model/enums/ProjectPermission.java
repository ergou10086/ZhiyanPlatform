package hbnu.project.zhiyanproject.model.enums;

/**
 * 系统权限枚举
 * 单条权限在这里管理，方便后期随时管理
 * 权限命名规范：模块:资源:操作（模块小写，资源和操作大写）
 * 例如：system:USER:LIST 表示系统模块的用户资源的列表查看操作
 *
 * @author ErgouTree
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

    /**
     * 任务管理权限 - 项目成员拥有
     */
    TASK_MANAGE("task:manage", "管理项目任务"),

    /**
     * 任务创建权限 - 项目成员拥有
     */
    TASK_CREATE("task:create", "创建项目任务"),

    /**
     * 任务分配权限 - 项目负责人拥有
     */
    TASK_ASSIGN("task:assign", "分配项目任务"),

    /**
     * 成员管理权限 - 项目创建者和负责人拥有
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
