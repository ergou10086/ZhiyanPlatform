package hbnu.project.zhiyanproject.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新角色请求
 *
 * @author AI Assistant
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新角色请求")
public class UpdateRoleRequest {

    @NotBlank(message = "角色代码不能为空")
    @Schema(description = "角色代码 (OWNER/MEMBER)", required = true, example = "MEMBER")
    private String roleCode;
}

