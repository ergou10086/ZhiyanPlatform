package hbnu.project.zhiyanproject.model.enums;

import lombok.Getter;

/**
 * 任务提交类型枚举
 *
 * @author Tokito
 */
@Getter
public enum SubmissionType {
    /**
     * 完成提交（最终提交，任务完成）
     */
    COMPLETE("完成提交"),

    /**
     * 阶段性提交（中间进度汇报）
     */
    PARTIAL("阶段性提交"),

    /**
     * 里程碑提交（重要节点）
     */
    MILESTONE("里程碑提交");

    private final String description;

    SubmissionType(String description) {
        this.description = description;
    }
}