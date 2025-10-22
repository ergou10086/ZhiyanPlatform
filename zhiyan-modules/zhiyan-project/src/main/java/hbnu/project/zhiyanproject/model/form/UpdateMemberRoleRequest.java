package hbnu.project.zhiyanproject.model.form;

import hbnu.project.zhiyanproject.model.enums.ProjectMemberRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新成员角色请求体
 *
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新成员角色请求")
public class UpdateMemberRoleRequest {

    @NotNull(message = "新角色不能为空")
    @Schema(description = "新角色", required = true)
    private ProjectMemberRole newRole;
}

