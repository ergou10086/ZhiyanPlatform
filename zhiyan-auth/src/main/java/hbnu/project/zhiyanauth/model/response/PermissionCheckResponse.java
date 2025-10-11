package hbnu.project.zhiyanauth.model.response;

import lombok.Builder;
import lombok.Data;

/**
 * 权限校验响应体
 *
 * @author Tokito
 */
@Data
@Builder
public class PermissionCheckResponse {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 权限标识
     */
    private String permission;

    /**
     * 是否拥有权限
     */
    private Boolean hasPermission;

    /**
     * 消息说明
     */
    private String message;
}