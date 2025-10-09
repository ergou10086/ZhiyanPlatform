package hbnu.project.zhiyanauth.model.form;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 移除角色权限请求体
 *
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemovePermissionsBody {

    /**
     * 权限ID列表
     */
    @NotEmpty(message = "权限ID列表不能为空")
    private List<Long> permissionIds;
}