package hbnu.project.zhiyanproject.model.form;

import hbnu.project.zhiyanproject.model.enums.SubmissionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 提交任务请求体
 *
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "提交任务请求")
public class SubmitTaskRequest {

    @NotBlank(message = "提交说明不能为空")
    @Size(min = 10, max = 5000, message = "提交说明长度必须在10-5000字符之间")
    @Schema(description = "提交说明（必填，描述任务完成情况）", required = true,
            example = "已完成数据库设计，包含用户表、项目表和任务表的ER图，并已经过技术评审。")
    private String submissionContent;

    @Schema(description = "提交类型（COMPLETE-完成提交，PARTIAL-阶段性提交，MILESTONE-里程碑提交）",
            example = "COMPLETE")
    private SubmissionType submissionType = SubmissionType.COMPLETE;

    @Schema(description = "附件URL列表（可选，用于提交成果文件、截图等）",
            example = "[\"https://minio.example.com/files/report.pdf\", \"https://minio.example.com/files/screenshot.png\"]")
    private List<String> attachmentUrls;

    @Schema(description = "实际工时（单位：小时，可选）", example = "8.5")
    private BigDecimal actualWorktime;

    @Schema(description = "是否为最终提交（TRUE-任务完成的最终提交，FALSE-阶段性提交）",
            example = "true")
    private Boolean isFinal = true;
}