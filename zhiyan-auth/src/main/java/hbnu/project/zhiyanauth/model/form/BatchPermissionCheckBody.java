package hbnu.project.zhiyanauth.model.form;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量权限校验请求体
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchPermissionCheckBody {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 权限标识列表
     */
    @NotEmpty(message = "权限列表不能为空")
    private List<String> permissions;
}