package hbnu.project.zhiyanauth.response;

import hbnu.project.zhiyanauth.model.dto.PermissionDTO;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色详情响应体
 *
 * @author Tokito
 */
@Data
@Builder
public class RoleDetailResponse {

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
     * 角色拥有的权限列表
     */
    private List<PermissionDTO> permissions;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 更新人
     */
    private String updatedBy;
}