package hbnu.project.zhiyanproject.model.form;

import hbnu.project.zhiyanproject.model.enums.ProjectMemberRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分配用户角色请求体
 *
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分配用户角色请求")
public class AssignRoleRequest {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", required = true)
    private Long userId;

    @NotNull(message = "角色枚举不能为空")
    @Schema(description = "角色枚举", required = true)
    private ProjectMemberRole roleEnum;
}

