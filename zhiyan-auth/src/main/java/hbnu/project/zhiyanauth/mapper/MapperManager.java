package hbnu.project.zhiyanauth.mapper;

import hbnu.project.zhiyanauth.model.dto.PermissionDTO;
import hbnu.project.zhiyanauth.model.dto.RoleDTO;
import hbnu.project.zhiyanauth.model.dto.UserDTO;
import hbnu.project.zhiyanauth.model.entity.Permission;
import hbnu.project.zhiyanauth.model.entity.Role;
import hbnu.project.zhiyanauth.model.entity.User;
import hbnu.project.zhiyanauth.model.form.UserProfileUpdateBody;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper 管理器
 * 统一管理所有的 MapStruct Mapper，便于在 Service 中注入使用
 * 提供便捷的转换方法
 *
 * @author ErgouTree
 */
@Component
@RequiredArgsConstructor
public class MapperManager {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;

    // ==================== User 相关转换方法 ====================

    /**
     * 将 User 实体转换为 UserDTO（不包含角色和权限）
     */
    public UserDTO convertToUserDTO(User user) {
        return userMapper.toDTO(user);
    }

    /**
     * 将 User 实体转换为 UserDTO（包含角色和权限）
     */
    public UserDTO convertToUserDTOWithRolesAndPermissions(User user) {
        return userMapper.toDTOWithRolesAndPermissions(user);
    }

    /**
     * 将 User 实体列表转换为 UserDTO 列表
     */
    public List<UserDTO> convertToUserDTOList(List<User> users) {
        return userMapper.toDTOList(users);
    }

    /**
     * 更新用户资料
     */
    public void updateUserProfile(User user, UserProfileUpdateBody updateBody) {
        userMapper.updateUserProfile(user, updateBody);
    }

    // ==================== Role 相关转换方法 ====================

    /**
     * 将 Role 实体转换为 RoleDTO（不包含权限）
     */
    public RoleDTO convertToRoleDTO(Role role) {
        return roleMapper.toDTO(role);
    }

    /**
     * 将 Role 实体转换为 RoleDTO（包含权限）
     */
    public RoleDTO convertToRoleDTOWithPermissions(Role role) {
        return roleMapper.toDTOWithPermissions(role);
    }

    /**
     * 将 Role 实体列表转换为 RoleDTO 列表（不包含权限）
     */
    public List<RoleDTO> convertToRoleDTOList(List<Role> roles) {
        return roleMapper.toDTOList(roles);
    }

    /**
     * 将 Role 实体列表转换为 RoleDTO 列表（包含权限）
     */
    public List<RoleDTO> convertToRoleDTOListWithPermissions(List<Role> roles) {
        return roleMapper.toDTOListWithPermissions(roles);
    }

    /**
     * 从 RoleDTO 创建 Role 实体
     */
    public Role convertToRole(RoleDTO roleDTO) {
        return roleMapper.fromDTO(roleDTO);
    }

    /**
     * 从 RoleDTO 创建 Role 实体（兼容旧方法名）
     */
    public Role convertFromRoleDTO(RoleDTO roleDTO) {
        return roleMapper.fromDTO(roleDTO);
    }

    /**
     * 更新 Role 实体
     */
    public void updateRole(Role role, RoleDTO roleDTO) {
        roleMapper.updateRole(role, roleDTO);
    }

    // ==================== Permission 相关转换方法 ====================

    /**
     * 将 Permission 实体转换为 PermissionDTO
     */
    public PermissionDTO convertToPermissionDTO(Permission permission) {
        return permissionMapper.toDTO(permission);
    }

    /**
     * 将 Permission 实体列表转换为 PermissionDTO 列表
     */
    public List<PermissionDTO> convertToPermissionDTOList(List<Permission> permissions) {
        return permissionMapper.toDTOList(permissions);
    }

    /**
     * 从 PermissionDTO 创建 Permission 实体
     */
    public Permission convertToPermission(PermissionDTO permissionDTO) {
        return permissionMapper.fromDTO(permissionDTO);
    }

    /**
     * 从 PermissionDTO 创建 Permission 实体（兼容旧方法名）
     */
    public Permission convertFromPermissionDTO(PermissionDTO permissionDTO) {
        return permissionMapper.fromDTO(permissionDTO);
    }

    /**
     * 更新 Permission 实体
     */
    public void updatePermission(Permission permission, PermissionDTO permissionDTO) {
        permissionMapper.updatePermission(permission, permissionDTO);
    }

    // ==================== Mapper 实例获取方法 ====================

    /**
     * 获取 UserMapper
     *
     * @return UserMapper 实例
     */
    public UserMapper getUserMapper() {
        return userMapper;
    }

    /**
     * 获取 RoleMapper
     *
     * @return RoleMapper 实例
     */
    public RoleMapper getRoleMapper() {
        return roleMapper;
    }

    /**
     * 获取 PermissionMapper
     *
     * @return PermissionMapper 实例
     */
    public PermissionMapper getPermissionMapper() {
        return permissionMapper;
    }
}
