package hbnu.project.zhiyanauth.mapper;

import hbnu.project.zhiyanauth.model.dto.PermissionDTO;
import hbnu.project.zhiyanauth.model.dto.RoleDTO;
import hbnu.project.zhiyanauth.model.dto.UserDTO;
import hbnu.project.zhiyanauth.model.entity.Permission;
import hbnu.project.zhiyanauth.model.entity.Role;
import hbnu.project.zhiyanauth.model.entity.User;
import hbnu.project.zhiyanauth.model.entity.UserRole;
import hbnu.project.zhiyanauth.model.form.RegisterBody;
import hbnu.project.zhiyanauth.model.form.UserProfileUpdateBody;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * 转换器管理类
 * 提供统一的实体转换入口，封装各个MapStruct Mapper的功能
 * 由于MapStruct生成的实现类是接口，Spring会自动注入实现
 *
 * @author ErgouTree
 */
@Component
@RequiredArgsConstructor
public class MapperManager {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;

    // ==================== 用户相关转换 ====================

    /**
     * 将User实体转换为UserDTO（基础信息）
     */
    public UserDTO convertToUserDTO(User user) {
        if (user == null) {
            return null;
        }
        return userMapper.toDTO(user);
    }

    /**
     * 将User实体转换为包含角色和权限的UserDTO
     */
    public UserDTO convertToUserDTOWithRolesAndPermissions(User user) {
        if (user == null) {
            return null;
        }
        return userMapper.toDTOWithRolesAndPermissions(user);
    }

    /**
     * 将User实体列表转换为UserDTO列表（基础信息）
     */
    public List<UserDTO> convertToUserDTOList(List<User> users) {
        if (users == null || users.isEmpty()) {
            return new ArrayList<>();
        }
        return userMapper.toDTOList(users);
    }

    /**
     * 将注册表单转换为User实体
     */
    public User convertFromRegisterBody(RegisterBody registerBody, String passwordHash) {
        if (registerBody == null || passwordHash == null) {
            return null;
        }
        return userMapper.fromRegisterBody(registerBody, passwordHash);
    }

    /**
     * 更新User实体的用户资料信息
     */
    public void updateUserProfile(User user, UserProfileUpdateBody updateBody) {
        if (user != null && updateBody != null) {
            userMapper.updateUserProfile(user, updateBody);
        }
    }

    /**
     * 创建用于登录返回的UserDTO（简化信息）
     */
    public UserDTO convertToLoginUserDTO(User user) {
        if (user == null) {
            return null;
        }
        return userMapper.toLoginDTO(user);
    }

    /**
     * 提取用户角色名称列表
     */
    public List<String> extractUserRoleNames(List<UserRole> userRoles) {
        return roleMapper.extractRoleNamesFromUserRoles(userRoles);
    }

    /**
     * 提取用户权限名称列表
     */
    public List<String> extractUserPermissionNames(List<UserRole> userRoles) {
        return userMapper.extractPermissionNames(userRoles);
    }

    // ==================== 角色相关转换 ====================

    /**
     * 将Role实体转换为RoleDTO（基础信息）
     */
    public RoleDTO convertToRoleDTO(Role role) {
        if (role == null) {
            return null;
        }
        return roleMapper.toDTO(role);
    }

    /**
     * 将Role实体转换为包含权限的RoleDTO
     */
    public RoleDTO convertToRoleDTOWithPermissions(Role role) {
        if (role == null) {
            return null;
        }
        return roleMapper.toDTOWithPermissions(role);
    }

    /**
     * 将Role实体转换为简化的RoleDTO
     */
    public RoleDTO convertToSimpleRoleDTO(Role role) {
        if (role == null) {
            return null;
        }
        return roleMapper.toSimpleDTO(role);
    }

    /**
     * 将Role实体列表转换为RoleDTO列表（基础信息）
     */
    public List<RoleDTO> convertToRoleDTOList(List<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return new ArrayList<>();
        }
        return roleMapper.toDTOList(roles);
    }

    /**
     * 将Role实体列表转换为包含权限的RoleDTO列表
     */
    public List<RoleDTO> convertToRoleDTOListWithPermissions(List<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return new ArrayList<>();
        }
        return roleMapper.toDTOListWithPermissions(roles);
    }

    /**
     * 将RoleDTO转换为Role实体
     */
    public Role convertFromRoleDTO(RoleDTO roleDTO) {
        if (roleDTO == null) {
            return null;
        }
        return roleMapper.fromDTO(roleDTO);
    }

    /**
     * 更新Role实体的信息
     */
    public void updateRole(Role role, RoleDTO roleDTO) {
        if (role != null && roleDTO != null) {
            roleMapper.updateRole(role, roleDTO);
        }
    }

    /**
     * 提取角色名称列表
     */
    public List<String> extractRoleNames(List<Role> roles) {
        return roleMapper.extractRoleNames(roles);
    }

    // ==================== 权限相关转换 ====================

    /**
     * 将Permission实体转换为PermissionDTO
     */
    public PermissionDTO convertToPermissionDTO(Permission permission) {
        if (permission == null) {
            return null;
        }
        return permissionMapper.toDTO(permission);
    }

    /**
     * 将Permission实体转换为简化的PermissionDTO
     */
    public PermissionDTO convertToSimplePermissionDTO(Permission permission) {
        if (permission == null) {
            return null;
        }
        return permissionMapper.toSimpleDTO(permission);
    }

    /**
     * 将Permission实体列表转换为PermissionDTO列表
     */
    public List<PermissionDTO> convertToPermissionDTOList(List<Permission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return new ArrayList<>();
        }
        return permissionMapper.toDTOList(permissions);
    }

    /**
     * 将Permission实体列表转换为简化的PermissionDTO列表
     */
    public List<PermissionDTO> convertToSimplePermissionDTOList(List<Permission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return new ArrayList<>();
        }
        return permissionMapper.toSimpleDTOList(permissions);
    }

    /**
     * 将PermissionDTO转换为Permission实体
     */
    public Permission convertFromPermissionDTO(PermissionDTO permissionDTO) {
        if (permissionDTO == null) {
            return null;
        }
        return permissionMapper.fromDTO(permissionDTO);
    }

    /**
     * 更新Permission实体的信息
     */
    public void updatePermission(Permission permission, PermissionDTO permissionDTO) {
        if (permission != null && permissionDTO != null) {
            permissionMapper.updatePermission(permission, permissionDTO);
        }
    }

    /**
     * 提取权限名称列表
     */
    public List<String> extractPermissionNames(List<Permission> permissions) {
        return permissionMapper.extractPermissionNames(permissions);
    }

    // ==================== 高级转换方法 ====================

    /**
     * 构建包含完整信息的UserDTO
     * 包含用户基础信息、角色列表和权限列表
     *
     * @param user 用户实体（需要预加载userRoles及其关联的role和rolePermissions）
     * @return 包含完整信息的UserDTO
     */
    public UserDTO buildCompleteUserDTO(User user) {
        if (user == null) {
            return null;
        }

        // 直接使用UserMapper的完整转换方法
        return userMapper.toDTOWithRolesAndPermissions(user);
    }

    /**
     * 构建简化的用户信息
     * 只包含登录所需的基础信息，不包含角色和权限
     */
    public UserDTO buildSimpleUserDTO(User user) {
        if (user == null) {
            return null;
        }
        return userMapper.toLoginDTO(user);
    }

    /**
     * 批量构建包含完整信息的UserDTO列表
     *
     * @param users 用户实体列表（需要预加载相关关联）
     * @return 包含完整信息的UserDTO列表
     */
    public List<UserDTO> buildCompleteUserDTOList(List<User> users) {
        if (users == null || users.isEmpty()) {
            return new ArrayList<>();
        }

        List<UserDTO> result = new ArrayList<>();
        for (User user : users) {
            UserDTO dto = buildCompleteUserDTO(user);
            if (dto != null) {
                result.add(dto);
            }
        }
        return result;
    }

    /**
     * 批量构建简化的UserDTO列表
     */
    public List<UserDTO> buildSimpleUserDTOList(List<User> users) {
        if (users == null || users.isEmpty()) {
            return new ArrayList<>();
        }
        return userMapper.toDTOList(users);
    }

    // ==================== 辅助转换方法 ====================

    /**
     * 检查用户是否有指定角色
     *
     * @param userRoles 用户角色关联列表
     * @param roleName 角色名称
     * @return 是否拥有指定角色
     */
    public boolean hasRole(List<UserRole> userRoles, String roleName) {
        if (userRoles == null || userRoles.isEmpty() || roleName == null) {
            return false;
        }

        List<String> roleNames = extractUserRoleNames(userRoles);
        return roleNames.contains(roleName);
    }

    /**
     * 检查用户是否有指定权限
     *
     * @param userRoles 用户角色关联列表
     * @param permissionName 权限名称
     * @return 是否拥有指定权限
     */
    public boolean hasPermission(List<UserRole> userRoles, String permissionName) {
        if (userRoles == null || userRoles.isEmpty() || permissionName == null) {
            return false;
        }

        List<String> permissionNames = extractUserPermissionNames(userRoles);
        return permissionNames.contains(permissionName);
    }

    /**
     * 检查用户是否有任意一个指定权限
     *
     * @param userRoles 用户角色关联列表
     * @param permissionNames 权限名称列表
     * @return 是否拥有任意一个指定权限
     */
    public boolean hasAnyPermission(List<UserRole> userRoles, List<String> permissionNames) {
        if (userRoles == null || userRoles.isEmpty() || permissionNames == null || permissionNames.isEmpty()) {
            return false;
        }

        List<String> userPermissions = extractUserPermissionNames(userRoles);
        return permissionNames.stream().anyMatch(userPermissions::contains);
    }

    /**
     * 检查用户是否拥有所有指定权限
     *
     * @param userRoles 用户角色关联列表
     * @param permissionNames 权限名称列表
     * @return 是否拥有所有指定权限
     */
    public boolean hasAllPermissions(List<UserRole> userRoles, List<String> permissionNames) {
        if (userRoles == null || userRoles.isEmpty() || permissionNames == null || permissionNames.isEmpty()) {
            return false;
        }

        List<String> userPermissions = extractUserPermissionNames(userRoles);
        return new HashSet<>(userPermissions).containsAll(permissionNames);
    }
}