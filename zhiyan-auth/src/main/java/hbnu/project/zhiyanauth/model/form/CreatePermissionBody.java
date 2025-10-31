package hbnu.project.zhiyanauth.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建权限请求体
 *
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建权限请求体")
public class CreatePermissionBody {

    /**
     * 权限名称（必填）
     */
    @NotBlank(message = "权限名称不能为空")
    @Size(max = 100, message = "权限名称长度不能超过100个字符")
    @Schema(description = "权限名称（唯一标识符）", example = "project:create", required = true)
    private String name;

    /**
     * 权限描述（可选）
     */
    @Schema(description = "权限描述", example = "创建项目的权限")
    private String description;
}