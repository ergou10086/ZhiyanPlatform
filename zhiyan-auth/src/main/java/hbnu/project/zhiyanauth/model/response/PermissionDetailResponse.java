package hbnu.project.zhiyanauth.model.response;

import hbnu.project.zhiyanauth.model.dto.RoleDTO;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限详情响应体
 * 包含权限基本信息和关联的角色列表
 *
 * @author Tokito
 */
@Data
@Builder
public class PermissionDetailResponse {

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

    /**
     * 拥有该权限的角色列表（简化信息）
     */
    private List<RoleDTO> roles;




}