package hbnu.project.zhiyanmessgae.model.enums;

import lombok.Getter;

/**
 * 任务优先级枚举
 *
 * @author ErgouTree
 */
@Getter
public enum MessagePriority {

    HIGH,
    MEDIUM,
    LOW;

    public static MessagePriority ofScene(MessageScene scene) {
        return switch (scene) {
            case TASK_OVERDUE, TASK_REVIEW_REQUEST, TASK_REVIEW_RESULT,
                 PROJECT_MEMBER_APPLY, SYSTEM_SECURITY_ALERT -> HIGH;
            case TASK_DEADLINE_REMIND, PROJECT_ROLE_CHANGED,
                 ACHIEVEMENT_REVIEW_REQUEST, ACHIEVEMENT_STATUS_CHANGED -> MEDIUM;
            default -> LOW;
        };
    }
}
