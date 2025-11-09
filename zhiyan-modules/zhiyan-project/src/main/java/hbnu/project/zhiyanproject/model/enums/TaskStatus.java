package hbnu.project.zhiyanproject.model.enums;

/**
 * 任务状态枚举
 *
 * @author ErgouTree
 */
public enum TaskStatus {

    /**
     * 待办
     */
    TODO("待办", "任务待处理"),

    /**
     * 进行中
     */
    IN_PROGRESS("进行中", "任务正在执行中"),

    /**
     * 阻塞
     */
    BLOCKED("阻塞", "任务被阻塞"),

    /**
     * 待审核
     */
    PENDING_REVIEW("待审核", "任务已提交，等待审核"),

    /**
     * 已完成
     */
    DONE("已完成", "任务已完成");

    private final String statusName;
    private final String description;

    TaskStatus(String statusName, String description) {
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
