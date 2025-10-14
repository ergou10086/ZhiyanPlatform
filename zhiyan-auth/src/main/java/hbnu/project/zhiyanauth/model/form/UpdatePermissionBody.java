package hbnu.project.zhiyanauth.model.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新权限请求体
 *
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePermissionBody {

    /**
     * 权限名称（必填）
     */
    @NotBlank(message = "权限名称不能为空")
    @Size(max = 100, message = "权限名称长度不能超过100个字符")
    private String name;

    /**
     * 权限描述（可选）
     */
    private String description;
}