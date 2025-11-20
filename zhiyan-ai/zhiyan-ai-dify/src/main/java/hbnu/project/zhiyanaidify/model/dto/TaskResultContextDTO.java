package hbnu.project.zhiyanaidify.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 任务成果上下文 DTO（AI 模块本地拷贝）
 * 与 zhiyan-project 模块的 TaskResultContextDTO JSON 结构对齐，
 * 用于封装任务详情及所有提交记录，方便一次性传给 AI。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResultContextDTO {

    /** 任务详情 */
    private TaskDetailDTO task;

    /** 所有提交记录（按版本倒序） */
    private List<TaskSubmissionDTO> submissions;

    /** 最新提交（submissions[0] 的便捷引用） */
    private TaskSubmissionDTO latestSubmission;

    /** 已批准的最终提交（如果存在），否则为 null */
    private TaskSubmissionDTO finalApprovedSubmission;
}
