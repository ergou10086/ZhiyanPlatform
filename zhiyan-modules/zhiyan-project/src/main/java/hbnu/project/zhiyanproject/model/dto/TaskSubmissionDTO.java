package hbnu.project.zhiyanproject.model.dto;

import hbnu.project.zhiyanproject.model.enums.ReviewStatus;
import hbnu.project.zhiyanproject.model.enums.SubmissionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务提交记录DTO
 *
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "任务提交记录信息")
public class TaskSubmissionDTO {

    @Schema(description = "提交记录ID", example = "1977989681735929856")
    private String id;

    @Schema(description = "任务ID", example = "1977989681735929856")
    private String taskId;

    @Schema(description = "任务标题", example = "设计数据库模型")
    private String taskTitle;

    @Schema(description = "项目ID", example = "1977989681735929856")
    private String projectId;

    @Schema(description = "项目名称", example = "智研平台后端开发")
    private String projectName;

    @Schema(description = "提交人ID", example = "1977989681735929856")
    private String submitterId;

    @Schema(description = "提交人信息")
    private UserDTO submitter;

    @Schema(description = "提交类型", example = "COMPLETE")
    private SubmissionType submissionType;

    @Schema(description = "提交说明", example = "已完成数据库设计，包含用户表、项目表和任务表的ER图。")
    private String submissionContent;

    @Schema(description = "附件URL列表")
    private List<String> attachmentUrls;

    @Schema(description = "提交时间", example = "2025-11-08T10:30:00")
    private LocalDateTime submissionTime;

    @Schema(description = "审核状态", example = "PENDING")
    private ReviewStatus reviewStatus;

    @Schema(description = "审核人ID", example = "1977989681735929856")
    private String reviewerId;

    @Schema(description = "审核人信息")
    private UserDTO reviewer;

    @Schema(description = "审核意见", example = "任务完成质量高，符合要求，同意通过。")
    private String reviewComment;

    @Schema(description = "审核时间", example = "2025-11-08T15:00:00")
    private LocalDateTime reviewTime;

    @Schema(description = "实际工时（小时）", example = "8.5")
    private BigDecimal actualWorktime;

    @Schema(description = "提交版本号", example = "1")
    private Integer version;

    @Schema(description = "是否为最终提交", example = "true")
    private Boolean isFinal;

    @Schema(description = "创建时间", example = "2025-11-08T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2025-11-08T15:00:00")
    private LocalDateTime updatedAt;
}