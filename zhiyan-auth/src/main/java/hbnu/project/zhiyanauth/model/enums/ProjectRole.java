package hbnu.project.zhiyanauth.model.enums;

import java.util.Arrays;
import java.util.List;

/**
 * 项目角色枚举
 * 定义用户在特定项目中的角色和权限，只有创建者和负责人，创建者可以删除项目和创建任务，但是任何注册的用户都能创建项目
 *
 * @author ErgouTree
 */
public enum ProjectRole implements RoleTemplate {

    /**
     * 项目创建者/拥有者 - 拥有项目的全部权限
     */
    OWNER("项目创建者", "项目拥有者，拥有项目的全部权限", Arrays.asList(
            SystemPermission.PROJECT_MANAGE,
            SystemPermission.PROJECT_DELETE,
            SystemPermission.KNOWLEDGE_MANAGE
    )),

    /**
     * 项目负责人/成员 - 拥有项目管理权限，但不能删除项目
     */
    MEMBER("项目成员", "项目团队成员，可以管理项目但不能删除", Arrays.asList(
            SystemPermission.PROJECT_MANAGE,
            SystemPermission.KNOWLEDGE_MANAGE
    ));

    private final String roleName;
    private final String description;
    private final List<SystemPermission> permissions;

    ProjectRole(String roleName, String description, List<SystemPermission> permissions) {
        this.roleName = roleName;
        this.description = description;
        this.permissions = permissions;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getDescription() {
        return description;
    }

    public List<SystemPermission> getPermissions() {
        return permissions;
    }

    /**
     * 获取角色类型
     */
    @Override
    public String getRoleType() {
        return "PROJECT";
    }    public String getCode() { return this.name();
    }

    @Override
    public List<PermissionModule> getPermissionMoules() {
        return List.of();
    }

    /**
     * 检查是否拥有指定权限
     */
    public boolean hasPermission(SystemPermission permission) {
        return permissions.contains(permission);
    }

    /**
     * 检查是否拥有指定权限（字符串形式）
     */
    public boolean hasPermission(String permissionCode) {
        return permissions.stream()
                .anyMatch(p -> p.getPermission().equals(permissionCode));
    }
}
