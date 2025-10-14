package hbnu.project.zhiyanauth.model.response;
import lombok.Builder;
import lombok.Data;

/**
 * 角色信息响应体
 *
 * @author Tokito
 */
@Data
@Builder
public class RoleInfoResponse {

    /**
     * 角色ID
     */
    private Long id;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 角色类型（SYSTEM/PROJECT）
     */
    private String roleType;

    /**
     * 项目ID（项目角色时有值）
     */
    private Long projectId;

    /**
     * 是否为系统默认角色
     */
    private Boolean isSystemDefault;

    /**
     * 该角色的用户数量
     */
    private Long userCount;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 更新人
     */
    private String updatedBy;
}