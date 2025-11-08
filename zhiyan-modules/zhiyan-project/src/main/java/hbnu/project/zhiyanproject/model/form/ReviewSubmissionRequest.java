package hbnu.project.zhiyanproject.model.form;

import hbnu.project.zhiyanproject.model.enums.ReviewStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审核任务提交请求体
 *
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "审核任务提交请求")
public class ReviewSubmissionRequest {

    @NotNull(message = "审核结果不能为空")
    @Schema(description = "审核结果（APPROVED-批准，REJECTED-拒绝）", required = true,
            example = "APPROVED")
    private ReviewStatus reviewStatus;

    @Size(max = 2000, message = "审核意见长度不能超过2000字符")
    @Schema(description = "审核意见（可选，拒绝时建议填写）",
            example = "任务完成质量高，符合要求，同意通过。")
    private String reviewComment;
}