package hbnu.project.zhiyanauth.model.enums;

import org.apache.catalina.Role;

import java.util.Arrays;
import java.util.List;

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
public enum SysRole implements RoleTemplate {



    /**
     * 开发者 - 拥有系统所有权限
     */
    DEVELOPER("开发者", "拥有系统所有权限，包括用户、角色、权限的完全控制", Arrays.asList(
            // 基础权限
            SystemPermission.PROFILE_MANAGE,
            SystemPermission.PROJECT_CREATE,
            // 用户管理权限
            SystemPermission.SYSTEM_USER_LIST,
            SystemPermission.SYSTEM_USER_VIEW,
            SystemPermission.SYSTEM_USER_CREATE,
            SystemPermission.SYSTEM_USER_UPDATE,
            SystemPermission.SYSTEM_USER_DELETE,
            SystemPermission.SYSTEM_USER_LOCK,
            // 角色管理权限
            SystemPermission.SYSTEM_ROLE_LIST,
            SystemPermission.SYSTEM_ROLE_VIEW,
            SystemPermission.SYSTEM_ROLE_CREATE,
            SystemPermission.SYSTEM_ROLE_UPDATE,
            SystemPermission.SYSTEM_ROLE_DELETE,
            SystemPermission.SYSTEM_ROLE_ASSIGN,
            // 权限管理
            SystemPermission.SYSTEM_PERMISSION_LIST,
            SystemPermission.SYSTEM_PERMISSION_ASSIGN,
            // 项目权限
            SystemPermission.PROJECT_MANAGE,
            SystemPermission.PROJECT_DELETE,
            SystemPermission.KNOWLEDGE_MANAGE,
            // 系统管理
            SystemPermission.USER_ADMIN,
            SystemPermission.SYSTEM_ADMIN
    )),

    /**
     * 普通用户 - 可以创建项目，拥有基础功能权限
     */
    USER("普通用户", "可以创建项目，管理个人信息，参与项目团队", Arrays.asList(
            SystemPermission.PROFILE_MANAGE,
            SystemPermission.PROJECT_CREATE
    )),

    /**
     * 访客用户 - 受限的只读权限
     */
    GUEST("访客用户", "受限的访问权限，无法创建项目", Arrays.asList(
            SystemPermission.PROFILE_MANAGE
    ));

    private final String roleName;
    private final String description;
    private final List<SystemPermission> permissions;

    SysRole(String roleName, String description, List<SystemPermission> permissions) {
        this.roleName = roleName;
        this.description = description;
        this.permissions = permissions;
    }

    @Override
    public String getRoleName() {
        return roleName;
    }
    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public List<SystemPermission> getPermissions() {
        return permissions;
    }

    @Override
    public String getRoleType() {
        return "SYSTEM";
    }


    /**
     * 获取角色代码（用于权限判断）
     */
    public String getCode() {
        return this.name();
    }

    @Override
    public List<PermissionModule> getPermissionMoules() {
        return List.of();
    }


    /**
     * 获取角色类型
     */

}
