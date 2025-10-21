package hbnu.project.zhiyanauth.model.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 权限信息响应体
 *
 * @author Tokito
 */
@Data
@Builder
public class PermissionInfoResponse {

    /**
     * 权限ID
     */
    private Long id;

    /**
     * 权限名称
     */
    private String name;

    /**
     * 权限描述
     */
    private String description;

    /**
     * 拥有该权限的角色数量
     */
    private Long roleCount;
}