package hbnu.project.zhiyanauth.model.enums;

import java.util.List;

/**
 * 角色模板接口
 * 统一系统角色和项目角色的接口
 *
 * @author Tokito
 */

public interface RoleTemplate {

    /**
     * 获取角色名称
     */
    String getRoleName();

    /**
     * 获取角色描述
     */
    String getDescription();

    /**
     * 获取角色权限列表
     */
    List<SystemPermission> getPermissions();

    /**
     * 获取角色类型字符串
     */
    String getRoleType();


    /**
     * 获取角色代码
     */
    String getCode();

    List<PermissionModule> getPermissionMoules();
}

