package hbnu.project.zhiyanauth.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 权限信息响应体
 *
 * @author Tokito
 */
@Data
@Builder
@Schema(description = "权限信息响应体")
public class PermissionInfoResponse {

    /**
     * 权限ID
     */
    @Schema(description = "权限ID", example = "1977989681735929856")
    private Long id;

    /**
     * 权限名称
     */
    @Schema(description = "权限名称（唯一标识符）", example = "project:create")
    private String name;

    /**
     * 权限描述
     */
    @Schema(description = "权限描述", example = "创建项目的权限")
    private String description;

    /**
     * 拥有该权限的角色数量
     */
    @Schema(description = "拥有该权限的角色数量", example = "5")
    private Long roleCount;
}