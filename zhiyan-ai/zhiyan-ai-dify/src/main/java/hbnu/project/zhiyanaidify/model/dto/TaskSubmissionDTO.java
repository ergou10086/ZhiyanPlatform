package hbnu.project.zhiyanaidify.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务提交记录DTO（AI 模块本地拷贝）
 * 仅保留 AI 所需字段，结构与 zhiyan-project 模块的 TaskSubmissionDTO JSON 对齐，
 * 方便通过 Feign 进行反序列化，而无需直接依赖 zhiyan-project 模块。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskSubmissionDTO {

    private String id;

    private String taskId;

    private String taskTitle;

    private String taskCreatorId;

    private String projectId;

    private String projectName;

    private String submitterId;

    /**
     * 提交人姓名（由后端在 UserDTO 中取 name 并填充，或由前端透传）
     */
    private String submitterName;

    private String submissionContent;

    /**
     * 附件 URL 列表
     */
    private List<String> attachmentUrls;

    private LocalDateTime submissionTime;

    /**
     * 审核状态（字符串枚举，与 zhiyan-project 的 ReviewStatus.name 对齐）
     */
    private String reviewStatus;

    /**
     * 审核意见
     */
    private String reviewComment;

    private BigDecimal actualWorktime;

    private Integer version;

    private Boolean isFinal;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
