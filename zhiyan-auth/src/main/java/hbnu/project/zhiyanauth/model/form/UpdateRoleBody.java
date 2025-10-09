package hbnu.project.zhiyanauth.model.form;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新角色请求体
 *
 * @author Tokitp
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleBody {

    /**
     * 角色名称
     */
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    private String name;

    /**
     * 角色描述
     */
    @Size(max = 500, message = "角色描述长度不能超过500个字符")
    private String description;
}