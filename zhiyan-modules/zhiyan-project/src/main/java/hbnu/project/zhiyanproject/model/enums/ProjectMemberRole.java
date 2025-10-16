package hbnu.project.zhiyanproject.model.enums;

import java.util.Arrays;
import java.util.List;

/**
 * 项目成员角色枚举
 * 定义项目成员在项目中的具体角色和权限
 *
 * @author Tokito
 */
public enum ProjectMemberRole {

    /**
     * 项目创建者/拥有者 - 拥有项目的全部权限
     */
    OWNER("项目创建者", "项目拥有者，拥有项目的全部权限", Arrays.asList(
            ProjectPermission.PROJECT_MANAGE,
            ProjectPermission.PROJECT_DELETE,
            ProjectPermission.KNOWLEDGE_MANAGE,
            ProjectPermission.TASK_MANAGE,
            ProjectPermission.TASK_CREATE,
            ProjectPermission.MEMBER_MANAGE
    )),

    /**
     * 项目成员 - 基础参与权限
     */
    MEMBER("项目成员", "项目普通成员，基础参与权限", Arrays.asList(
            ProjectPermission.KNOWLEDGE_MANAGE,
            ProjectPermission.TASK_MANAGE,
            ProjectPermission.TASK_CREATE
    ));

    private final String roleName;
    private final String description;
    private final List<ProjectPermission> permissions;

    ProjectMemberRole(String roleName, String description, List<ProjectPermission> permissions) {
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

    public List<ProjectPermission> getPermissions() {
        return permissions;
    }

    /**
     * 检查是否拥有指定权限
     */
    public boolean hasPermission(ProjectPermission permission) {
        return permissions.contains(permission);
    }

    /**
     * 检查是否拥有指定权限（字符串形式）
     */
    public boolean hasPermission(String permissionCode) {
        return permissions.stream()
                .anyMatch(p -> p.getPermission().equals(permissionCode));
    }

    /**
     * 获取角色代码
     */
    public String getCode() {
        return this.name();
    }
}