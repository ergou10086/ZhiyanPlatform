package hbnu.project.zhiyanmessgae.model.enums;

/**
 * 通知类型(消息场景)枚举
 *
 * @author ErgouTree
 */
public enum MessageScene {

    TASK_ASSIGN("TASK", "任务分配"),
    TASK_STATUS_CHANGED("TASK", "任务状态更新"),
    TASK_REVIEW_REQUEST("TASK", "待审核任务"),
    TASK_REVIEW_RESULT("TASK", "任务审核结果"),
    TASK_DEADLINE_REMIND("TASK", "任务到期提醒"),
    TASK_OVERDUE("TASK", "任务逾期警告"),
    PROJECT_CREATED("PROJECT","项目创建"),
    PROJECT_ARCHIVED("PROJECT", "项目归档通知"),
    PROJECT_DELETED("PROJECT", "项目删除通知"),
    PROJECT_MEMBER_APPLY("PROJECT", "成员加入申请"),
    PROJECT_MEMBER_INVITED("PROJECT","项目成员邀请"),
    PROJECT_MEMBER_REMOVED("PROJECT","项目成员移除"),
    PROJECT_MEMBER_APPROVAL("PROJECT", "成员加入审批结果"),
    PROJECT_ROLE_CHANGED("PROJECT", "角色权限变更"),
    PROJECT_STATUS_CHANGED("PROJECT", "项目状态变更"),
    ACHIEVEMENT_REVIEW_REQUEST("ACHIEVEMENT", "成果评审请求"),
    ACHIEVEMENT_STATUS_CHANGED("ACHIEVEMENT", "成果状态变更"),
    ACHIEVEMENT_PUBLISHED("ACHIEVEMENT", "成果发布通知"),
    SYSTEM_SECURITY_ALERT("SYSTEM", "账号安全提醒"),
    SYSTEM_BROADCAST("SYSTEM", "平台广播");

    private final String module;
    private final String desc;

    MessageScene(String module, String desc) {
        this.module = module;
        this.desc = desc;
    }
}
