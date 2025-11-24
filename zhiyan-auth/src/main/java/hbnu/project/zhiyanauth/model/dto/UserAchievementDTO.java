package hbnu.project.zhiyanauth.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户成果关联 DTO
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户成果关联信息")
public class UserAchievementDTO {

    @Schema(description = "关联记录ID")
    private String id;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "成果ID")
    private String achievementId;

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "成果标题")
    private String achievementTitle;

    @Schema(description = "成果类型")
    private String achievementType;

    @Schema(description = "成果状态")
    private String achievementStatus;

    @Schema(description = "展示顺序")
    private Integer displayOrder;

    @Schema(description = "备注说明")
    private String remark;

    @Schema(description = "关联时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
