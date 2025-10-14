package hbnu.project.zhiyanauth.mapper;

import hbnu.project.zhiyanauth.model.dto.UserDTO;
import hbnu.project.zhiyanauth.model.entity.User;
import hbnu.project.zhiyanauth.model.entity.UserRole;
import hbnu.project.zhiyanauth.model.form.RegisterBody;
import hbnu.project.zhiyanauth.model.form.UserProfileUpdateBody;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户实体转换器
 * 使用MapStruct提供User实体与UserDTO之间的高效转换功能
 *
 * @author ErgouTree
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    /**
     * 将User实体转换为UserDTO
     * 基础转换，不包含角色和权限信息
     */
    @Named("toDTO")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    UserDTO toDTO(User user);

    /**
     * 将User实体转换为包含角色和权限的UserDTO
     * 完整转换，包含用户的角色和权限信息
     */
    @Named("toDTOWithRolesAndPermissions")
    @Mapping(target = "roles", expression = "java(extractRoleNames(user.getUserRoles()))")
    @Mapping(target = "permissions", expression = "java(extractPermissionNames(user.getUserRoles()))")
    UserDTO toDTOWithRolesAndPermissions(User user);

    /**
     * 将User实体转换为包含角色的UserDTO，权限单独设置
     * 避免懒加载问题
     */
    @Named("toDTOWithRoles")
    @Mapping(target = "roles", expression = "java(extractRoleNames(user.getUserRoles()))")
    @Mapping(target = "permissions", ignore = true)
    UserDTO toDTOWithRoles(User user);

    /**
     * 从UserRole关联中提取角色名称列表
     */
    default List<String> extractRoleNames(List<UserRole> userRoles) {
        if (userRoles == null || userRoles.isEmpty()) {
            return new ArrayList<>();
        }
        return userRoles.stream()
                .map(ur -> ur.getRole().getName())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 从UserRole关联中提取权限名称列表
     */
    default List<String> extractPermissionNames(List<UserRole> userRoles) {
        if (userRoles == null || userRoles.isEmpty()) {
            return new ArrayList<>();
        }
        Set<String> permissions = userRoles.stream()
                .flatMap(ur -> ur.getRole().getRolePermissions().stream())
                .map(rp -> rp.getPermission().getName())
                .collect(Collectors.toSet());
        return new ArrayList<>(permissions);
    }

    /**
     * 将User实体列表转换为UserDTO列表
     */
    @IterableMapping(qualifiedByName = "toDTO")
    List<UserDTO> toDTOList(List<User> users);

    /**
     * 将注册表单转换为User实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", source = "passwordHash")
    @Mapping(target = "isLocked", constant = "false")
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "userRoles", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "title", ignore = true)
    @Mapping(target = "institution", ignore = true)
    @Mapping(target = "status", ignore = true)
    User fromRegisterBody(RegisterBody registerBody, String passwordHash);

    /**
     * 从UserProfileUpdateBody更新User实体
     * 允许更新姓名、职称、机构、头像等字段
     *
     * @param user 目标用户实体
     * @param updateBody 更新表单
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "isLocked", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "userRoles", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserProfile(@MappingTarget User user, UserProfileUpdateBody updateBody);

    /**
     * 创建简化的UserDTO
     * 只包含基础信息，用于列表展示
     *
     * @param user 用户实体
     * @return 简化的UserDTO
     */
    @Named("toSimpleDTO")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    UserDTO toSimpleDTO(User user);
}

