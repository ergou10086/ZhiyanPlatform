package hbnu.project.zhiyanproject.model.enums;

/**
 * 项目状态枚举
 *
 * @author ErgouTree
 */
public enum ProjectStatus {

    /**
     * 规划中
     */
    PLANNING("规划中", "项目处于规划阶段"),

    /**
     * 进行中
     */
    ONGOING("进行中", "项目正在执行中"),

    /**
     * 已完成
     */
    COMPLETED("已完成", "项目已完成"),

    /**
     * 已归档
     */
    ARCHIVED("已归档", "项目已归档");

    private final String statusName;
    private final String description;

    ProjectStatus(String statusName, String description) {
        this.statusName = statusName;
        this.description = description;
    }

    public String getStatusName() {
        return statusName;
    }

    public String getDescription() {
        return description;
    }
}