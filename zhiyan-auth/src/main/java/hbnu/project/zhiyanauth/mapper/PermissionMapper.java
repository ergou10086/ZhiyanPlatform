package hbnu.project.zhiyanauth.mapper;

import hbnu.project.zhiyanauth.model.dto.PermissionDTO;
import hbnu.project.zhiyanauth.model.entity.Permission;
import hbnu.project.zhiyanauth.model.entity.RolePermission;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限实体转换器
 * 使用MapStruct提供Permission实体与PermissionDTO之间的高效转换功能
 *
 * @author ErgouTree
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true) // 禁用Builder避免属性识别问题
)
public interface PermissionMapper {

    /**
     * 将Permission实体转换为PermissionDTO
     */
    @Named("toFullDTO")
    @Mapping(target = "createdAt", ignore = true) // Permission实体没有这些字段
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    PermissionDTO toDTO(Permission permission);

    /**
     * 将Permission实体列表转换为PermissionDTO列表
     */
    @IterableMapping(qualifiedByName = "toFullDTO")
    List<PermissionDTO> toDTOList(List<Permission> permissions);

    /**
     * 将PermissionDTO转换为Permission实体
     * 用于创建新权限
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rolePermissions", ignore = true)
    Permission fromDTO(PermissionDTO permissionDTO);

    /**
     * 更新Permission实体的信息
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rolePermissions", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePermission(@MappingTarget Permission permission, PermissionDTO permissionDTO);

    /**
     * 创建简化的PermissionDTO
     * 只包含基础信息，用于列表展示
     */
    @Named("toSimpleDTO")
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    PermissionDTO toSimpleDTO(Permission permission);

    /**
     * 批量创建简化的PermissionDTO列表
     */
    @IterableMapping(qualifiedByName = "toSimpleDTO")
    List<PermissionDTO> toSimpleDTOList(List<Permission> permissions);

    /**
     * 提取权限名称列表
     */
    default List<String> extractPermissionNames(List<Permission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return new ArrayList<>();
        }
        return permissions.stream()
                .map(Permission::getName)
                .collect(Collectors.toList());
    }

    /**
     * 从RolePermission关联中提取权限名称列表
     */
    default List<String> extractPermissionNamesFromRolePermissions(List<RolePermission> rolePermissions) {
        if (rolePermissions == null || rolePermissions.isEmpty()) {
            return new ArrayList<>();
        }
        return rolePermissions.stream()
                .map(rp -> rp.getPermission().getName())
                .distinct()
                .collect(Collectors.toList());
    }
}