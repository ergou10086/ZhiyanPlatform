package hbnu.project.zhiyanauth.mapper;

import hbnu.project.zhiyanauth.model.dto.RoleDTO;
import hbnu.project.zhiyanauth.model.entity.Role;
import hbnu.project.zhiyanauth.model.entity.RolePermission;
import hbnu.project.zhiyanauth.model.entity.UserRole;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色实体转换器
 * 使用MapStruct提供Role实体与RoleDTO之间的高效转换功能
 *
 * @author ErgouTree
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {

    /**
     * 将Role实体转换为RoleDTO
     * 基础转换，不包含权限信息
     *
     * @param role 角色实体
     * @return RoleDTO
     */
    @Mapping(target = "permissions", ignore = true)
    RoleDTO toDTO(Role role);

    /**
     * 将Role实体转换为包含权限的RoleDTO
     * 完整转换，包含角色的权限信息
     *
     * @param role 角色实体（需要已加载rolePermissions关联）
     * @return 包含权限信息的RoleDTO
     */
    @Mapping(target = "permissions", expression = "java(extractPermissionNames(role.getRolePermissions()))")
    RoleDTO toDTOWithPermissions(Role role);

    /**
     * 从RolePermission关联中提取权限名称列表
     */
    default List<String> extractPermissionNames(List<RolePermission> rolePermissions) {
        if (rolePermissions == null || rolePermissions.isEmpty()) {
            return new ArrayList<>();
        }
        return rolePermissions.stream()
                .map(rp -> rp.getPermission().getName())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 将Role实体列表转换为RoleDTO列表
     *
     * @param roles 角色实体列表
     * @return RoleDTO列表
     */
    List<RoleDTO> toDTOList(List<Role> roles);

    /**
     * 将Role实体列表转换为包含权限的RoleDTO列表
     *
     * @param roles 角色实体列表
     * @return 包含权限信息的RoleDTO列表
     */
    List<RoleDTO> toDTOListWithPermissions(List<Role> roles);

    /**
     * 将RoleDTO转换为Role实体
     * 用于创建新角色
     *
     * @param roleDTO 角色DTO
     * @return Role实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userRoles", ignore = true)
    @Mapping(target = "rolePermissions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Role fromDTO(RoleDTO roleDTO);

    /**
     * 更新Role实体的信息
     *
     * @param role 目标角色实体
     * @param roleDTO 更新数据
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userRoles", ignore = true)
    @Mapping(target = "rolePermissions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateRole(@MappingTarget Role role, RoleDTO roleDTO);

    /**
     * 创建简化的RoleDTO
     * 只包含基础信息，用于列表展示
     *
     * @param role 角色实体
     * @return 简化的RoleDTO
     */
    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    RoleDTO toSimpleDTO(Role role);

    /**
     * 提取角色名称列表
     *
     * @param roles 角色实体列表
     * @return 角色名称列表
     */
    default List<String> extractRoleNames(List<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return new ArrayList<>();
        }
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toList());
    }

    /**
     * 从UserRole关联中提取角色名称列表
     *
     * @param userRoles 用户角色关联列表
     * @return 角色名称列表
     */
    default List<String> extractRoleNamesFromUserRoles(List<UserRole> userRoles) {
        if (userRoles == null || userRoles.isEmpty()) {
            return new ArrayList<>();
        }
        return userRoles.stream()
                .map(ur -> ur.getRole().getName())
                .distinct()
                .collect(Collectors.toList());
    }
}