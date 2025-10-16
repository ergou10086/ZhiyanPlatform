package hbnu.project.zhiyanproject.model.enums;

/**
 * 项目成员角色枚举
 *
 * @author Tokito
 */
public enum ProjectMemberRole {

    /**
     * 项目负责人
     */
    LEADER("负责人", "项目负责人，拥有项目管理权限"),

    /**
     * 项目维护者
     */
    MAINTAINER("维护者", "项目维护者，拥有部分管理权限"),

    /**
     * 普通成员
     */
    MEMBER("普通成员", "项目普通成员，基础参与权限");

    private final String roleName;
    private final String description;

    ProjectMemberRole(String roleName, String description) {
        this.roleName = roleName;
        this.description = description;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getDescription() {
        return description;
    }
}