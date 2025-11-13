package hbnu.project.zhiyanactivelog.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务操作类型枚举
 *
 * @author ErgouTree
 */
@Getter
@AllArgsConstructor
public enum TaskOperationType {

    /**
     * 创建任务
     */
    CREATE("CREATE", "创建任务"),

    /**
     * 更新任务
     */
    UPDATE("UPDATE", "更新任务"),

    /**
     * 删除任务
     */
    DELETE("DELETE", "删除任务"),

    /**
     * 分配任务
     */
    ASSIGN("ASSIGN", "分配任务"),

    /**
     * 提交任务
     */
    SUBMIT("SUBMIT", "提交任务"),

    /**
     * 审核任务
     */
    REVIEW("REVIEW", "审核任务"),

    /**
     * 状态变更
     */
    STATUS_CHANGE("STATUS_CHANGE", "状态变更");

    private final String code;
    private final String desc;
}
