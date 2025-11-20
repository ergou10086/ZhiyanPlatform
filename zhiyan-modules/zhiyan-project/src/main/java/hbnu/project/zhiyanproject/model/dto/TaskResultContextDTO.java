package hbnu.project.zhiyanproject.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 任务成果上下文 DTO
 * 用于聚合任务详情、所有提交记录等信息，供 AI/前端使用
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "任务成果上下文信息")
public class TaskResultContextDTO {

    @Schema(description = "任务详情")
    private TaskDetailDTO task;

    @Schema(description = "该任务的所有提交记录（按版本倒序）")
    private List<TaskSubmissionDTO> submissions;

    @Schema(description = "最新一次提交记录（submissions[0] 的快捷引用，可能为空）")
    private TaskSubmissionDTO latestSubmission;

    @Schema(description = "已批准的最终提交记录（如果存在），否则为空")
    private TaskSubmissionDTO finalApprovedSubmission;
}
