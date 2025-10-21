package hbnu.project.zhiyanauth.model.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 角色详情响应体
 *
 * @author ErgouTree
 * @version 2.0
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
     * 角色拥有的权限名称集合
     */
    private Set<String> permissions;


}