package hbnu.project.zhiyanproject.model.enums;

/**
 * 加入申请状态枚举
 *
 * @author ErgouTree
 */
public enum JoinRequestStatus {

    /**
     * 待处理
     */
    PENDING("待处理", "申请待处理"),

    /**
     * 已批准
     */
    APPROVED("已批准", "申请已批准"),

    /**
     * 已拒绝
     */
    REJECTED("已拒绝", "申请已拒绝");

    private final String statusName;
    private final String description;

    JoinRequestStatus(String statusName, String description) {
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
