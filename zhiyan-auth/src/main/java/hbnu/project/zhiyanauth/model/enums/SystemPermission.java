package hbnu.project.zhiyanauth.model.enums;

/**
 * 系统权限枚举
 * 单条权限在这里管理，方便后期随时管理
 * 权限命名规范：模块:资源:操作（模块小写，资源和操作大写）
 * 例如：system:USER:LIST 表示系统模块的用户资源的列表查看操作
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
    
    // ============ 用户管理权限 ============
    /**
     * 查看用户列表
     */
    SYSTEM_USER_LIST("system:user:list", "查看用户列表"),

    /**
     * 查看用户详情
     */
    SYSTEM_USER_VIEW("system:user:view", "查看用户详情"),

    /**
     * 创建用户
     */
    SYSTEM_USER_CREATE("system:user:create", "创建用户"),

    /**
     * 更新用户信息
     */
    SYSTEM_USER_UPDATE("system:user:update", "更新用户信息"),

    /**
     * 删除用户
     */
    SYSTEM_USER_DELETE("system:user:delete", "删除用户"),

    /**
     * 锁定/解锁用户
     */
    SYSTEM_USER_LOCK("system:user:lock", "锁定或解锁用户账户"),

    // ============ 角色管理权限 ============
    /**
     * 查看角色列表
     */
    SYSTEM_ROLE_LIST("system:role:list", "查看角色列表"),

    /**
     * 查看角色详情
     */
    SYSTEM_ROLE_VIEW("system:role:view", "查看角色详情"),

    /**
     * 创建角色
     */
    SYSTEM_ROLE_CREATE("system:role:create", "创建角色"),

    /**
     * 更新角色
     */
    SYSTEM_ROLE_UPDATE("system:role:update", "更新角色"),

    /**
     * 删除角色
     */
    SYSTEM_ROLE_DELETE("system:role:delete", "删除角色"),

    /**
     * 分配角色
     */
    SYSTEM_ROLE_ASSIGN("system:role:assign", "为用户分配角色"),

    // ============ 权限管理 ============
    /**
     * 查看权限列表
     */
    SYSTEM_PERMISSION_LIST("system:permission:list", "查看权限列表"),

    /**
     * 分配权限
     */
    SYSTEM_PERMISSION_ASSIGN("system:permission:assign", "为角色分配权限"),

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
     * 用户管理权限 - 仅系统管理员拥有（综合权限）
     */
    USER_ADMIN("user:admin", "管理系统用户（综合权限）"),
    
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
