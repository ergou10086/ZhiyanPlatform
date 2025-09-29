package hbnu.project.zhiyanauth.model.enums;

/**
 * 系统角色枚举
 * 简化的角色体系，主要分为系统级角色和项目级角色
 * 系统级的角色只能被指定，除开发者之外，只有登录的普通用户和未登录的用户这两种情况
 * 每个登录在平台上的用户，在不同项目中权限是不一样的
 * 所以一开始，用户注册了，只分配普通用户，开发者我们再改表
 * 创建了项目就分配 项目创建者，加入了项目就分配 项目负责人
 *                                              ———— ErgouTree
 *
 * @author ErgouTree
 */
public enum SysRole {
    
    /**
     * 开发者
     */
    DEVELOPER("开发者", "拥有系统所有权限"),
    
    /**
     * 普通用户 - 可以创建项目，拥有基础功能权限
     */
    USER("普通用户", "可以创建项目，管理个人信息，参与项目团队"),
    
    /**
     * 访客用户 - 受限的只读权限
     */
    GUEST("访客用户", "受限的访问权限，无法创建项目");

    private final String roleName;
    private final String description;

    SysRole(String roleName, String description) {
        this.roleName = roleName;
        this.description = description;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 获取角色代码（用于权限判断）
     */
    public String getCode() {
        return this.name();
    }
}
