package hbnu.project.zhiyanauth.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 关联学术成果请求体
 *
 * @author ErgouTree
 */
@Data
@Schema(description = "关联学术成果请求")
public class AchievementLinkBody {

    @NotNull(message = "成果ID不能为空")
    @Schema(description = "成果ID", required = true)
    private Long achievementId;

    @NotNull(message = "项目ID不能为空")
    @Schema(description = "项目ID", required = true)
    private Long projectId;

    @Schema(description = "展示顺序（可选，默认0）")
    private Integer displayOrder;

    @Schema(description = "备注说明（可选）", maxLength = 500)
    private String remark;
}