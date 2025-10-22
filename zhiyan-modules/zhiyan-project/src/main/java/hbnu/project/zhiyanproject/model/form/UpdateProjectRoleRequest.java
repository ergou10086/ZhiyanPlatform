package hbnu.project.zhiyanproject.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新项目角色请求体
 *
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新项目角色请求")
public class UpdateProjectRoleRequest {

    @NotNull(message = "项目ID不能为空")
    @Schema(description = "项目ID", required = true)
    private Long projectId;

    @Schema(description = "角色名称")
    private String name;

    @Schema(description = "角色描述")
    private String description;
}

