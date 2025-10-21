package hbnu.project.zhiyanauth.model.form;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 为用户分配角色请求体
 *
 * @author ErgouTree
 * @version 3.0
 * @rewrite Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignUserRoleBody {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 角色ID列表（支持批量分配）
     */
    @NotEmpty(message = "角色ID列表不能为空")
    private List<Long> roleIds;
}