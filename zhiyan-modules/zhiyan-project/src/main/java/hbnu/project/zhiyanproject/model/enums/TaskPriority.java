package hbnu.project.zhiyanproject.model.enums;

/**
 * 任务优先级枚举
 *
 * @author ErgouTree
 */
public enum TaskPriority {

    /**
     * 高优先级
     */
    HIGH("高", "高优先级任务"),

    /**
     * 中优先级
     */
    MEDIUM("中", "中等优先级任务"),

    /**
     * 低优先级
     */
    LOW("低", "低优先级任务");

    private final String priorityName;
    private final String description;

    TaskPriority(String priorityName, String description) {
        this.priorityName = priorityName;
        this.description = description;
    }

    public String getPriorityName() {
        return priorityName;
    }

    public String getDescription() {
        return description;
    }
}
