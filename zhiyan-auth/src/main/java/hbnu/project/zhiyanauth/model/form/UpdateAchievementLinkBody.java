package hbnu.project.zhiyanauth.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新成果关联信息请求体
 *
 * @author ErgouTree
 */
@Data
@Schema(description = "更新成果关联信息")
public class UpdateAchievementLinkBody {

    @Schema(description = "展示顺序")
    private Integer displayOrder;

    @Schema(description = "备注说明", maxLength = 500)
    private String remark;
}
