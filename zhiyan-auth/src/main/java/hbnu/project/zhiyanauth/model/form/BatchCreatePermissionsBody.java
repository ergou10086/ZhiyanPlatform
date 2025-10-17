package hbnu.project.zhiyanauth.model.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量创建权限请求体
 *
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchCreatePermissionsBody {

    /**
     * 权限列表
     */
    @NotNull(message = "权限列表不能为空")
    @NotEmpty(message = "权限列表不能为空")
    @Valid
    private List<PermissionItem> permissions;

    /**
     * 权限项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PermissionItem {
        /**
         * 权限名称
         */
        @NotNull(message = "权限名称不能为空")
        private String name;

        /**
         * 权限描述
         */
        private String description;
    }
}

