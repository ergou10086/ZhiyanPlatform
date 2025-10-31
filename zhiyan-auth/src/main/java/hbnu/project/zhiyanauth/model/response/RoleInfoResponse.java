package hbnu.project.zhiyanauth.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 角色信息响应体
 *
 * @author Tokito
 */
@Data
@Builder
@Schema(description = "角色信息响应体")
public class RoleInfoResponse {

    /**
     * 角色ID
     */
    @Schema(description = "角色ID", example = "1977989681735929856")
    private Long id;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称", example = "DEVELOPER")
    private String name;

    /**
     * 角色描述
     */
    @Schema(description = "角色描述", example = "系统开发者角色")
    private String description;

    /**
     * 角色类型（SYSTEM/PROJECT）
     */
    @Schema(description = "角色类型", example = "SYSTEM", allowableValues = {"SYSTEM", "PROJECT"})
    private String roleType;

    /**
     * 项目ID（项目角色时有值）
     */
    @Schema(description = "项目ID（项目角色时有值）", example = "1977989681735929856")
    private Long projectId;

    /**
     * 是否为系统默认角色
     */
    @Schema(description = "是否为系统默认角色", example = "true")
    private Boolean isSystemDefault;

    /**
     * 该角色的用户数量
     */
    @Schema(description = "该角色的用户数量", example = "10")
    private Long userCount;


}