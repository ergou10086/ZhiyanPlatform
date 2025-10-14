package hbnu.project.zhiyanproject.model.enums;

/**
 * 项目可见性枚举
 *
 * @author ErgouTree
 */
public enum ProjectVisibility {

    /**
     * 公开
     */
    PUBLIC("公开", "项目对所有用户可见"),

    /**
     * 私有
     */
    PRIVATE("私有", "项目仅对成员可见");

    private final String visibilityName;
    private final String description;

    ProjectVisibility(String visibilityName, String description) {
        this.visibilityName = visibilityName;
        this.description = description;
    }

    public String getVisibilityName() {
        return visibilityName;
    }

    public String getDescription() {
        return description;
    }
}
