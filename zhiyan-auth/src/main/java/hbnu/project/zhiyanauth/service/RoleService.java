package hbnu.project.zhiyanauth.service;

import hbnu.project.zhiyanauth.model.dto.RoleDTO;
import hbnu.project.zhiyanauth.model.enums.SysRole;
import hbnu.project.zhiyancommonbasic.domain.R;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

/**
 * 角色服务接口
 * 提供角色管理、角色分配等核心功能
 * 
 * 职责说明：
 * - 本服务专注于系统角色的管理和分配
 * - 系统角色包括：DEVELOPER（开发者）、USER（普通用户）、GUEST（访客）
 * - 项目内角色由项目服务管理
 * 
 * @author ErgouTree
 * @version 3.0
 * @rewrite Tokito
 */
public interface RoleService {

    // ==================== 角色信息查询 ====================

    /**
     * 获取所有系统角色列表（分页）
     * 
     * @param pageable 分页参数
     * @return 角色列表
     */
    R<Page<RoleDTO>> getAllRoles(Pageable pageable);

    /**
     * 根据ID获取角色详细信息
     * 
     * @param roleId 角色ID
     * @return 角色详细信息
     */
    R<RoleDTO> getRoleById(Long roleId);

    /**
     * 根据名称查找角色
     * 
     * @param roleName 角色名称
     * @return 角色信息
     */
    R<RoleDTO> getRoleByName(String roleName);

    /**
     * 根据名称获取角色ID
     * 
     * 注意：此方法会查找所有类型的角色（SYSTEM 和 PROJECT）
     * 由于角色名称全局唯一，不会有重复
     * 
     * @param roleName 角色名称（如：USER, DEVELOPER, GUEST）
     * @return 角色ID
     */
    R<Long> getRoleIdByName(String roleName);

    // ==================== 角色管理（CRUD） ====================

    /**
     * 创建新角色
     * 仅开发者可用
     * 
     * @param roleDTO 角色信息
     * @return 创建后的角色信息
     */
    R<RoleDTO> createRole(RoleDTO roleDTO);

    /**
     * 更新角色信息
     * 仅开发者可用
     * 
     * @param roleId 角色ID
     * @param roleDTO 更新的角色信息
     * @return 更新后的角色信息
     */
    R<RoleDTO> updateRole(Long roleId, RoleDTO roleDTO);

    /**
     * 删除角色
     * 仅开发者可用，系统默认角色不可删除
     * 
     * @param roleId 角色ID
     * @return 删除结果
     */
    R<Void> deleteRole(Long roleId);

    // ==================== 用户角色管理 ====================

    /**
     * 获取用户的所有角色
     * 
     * @param userId 用户ID
     * @return 角色名称集合
     */
    R<Set<String>> getUserRoles(Long userId);

    /**
     * 为用户分配角色
     * 仅开发者可用
     * 
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 分配结果
     */
    R<Void> assignRolesToUser(Long userId, List<Long> roleIds);

    /**
     * 移除用户的角色
     * 仅开发者可用
     * 
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 移除结果
     */
    R<Void> removeRolesFromUser(Long userId, List<Long> roleIds);

    /**
     * 为用户分配系统角色（用于用户注册时）
     * 默认为新用户分配 USER 角色
     * 
     * @param userId 用户ID
     * @param sysRole 系统角色枚举
     * @return 分配结果
     */
    R<Void> assignSystemRoleToUser(Long userId, SysRole sysRole);

    // ==================== 角色权限管理 ====================

    /**
     * 为角色分配权限
     * 仅开发者可用
     * 
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @return 分配结果
     */
    R<Void> assignPermissionsToRole(Long roleId, List<Long> permissionIds);

    /**
     * 移除角色的权限
     * 仅开发者可用
     * 
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @return 移除结果
     */
    R<Void> removePermissionsFromRole(Long roleId, List<Long> permissionIds);

    /**
     * 获取角色的所有权限
     * 
     * @param roleId 角色ID
     * @return 权限名称集合
     */
    R<Set<String>> getRolePermissions(Long roleId);

    // ==================== 角色用户查询 ====================

    /**
     * 获取拥有指定角色的用户列表
     * 
     * @param roleId 角色ID
     * @param pageable 分页参数
     * @return 用户ID列表
     */
    R<Page<Long>> getRoleUsers(Long roleId, Pageable pageable);

    /**
     * 统计拥有指定角色的用户数量
     * 
     * @param roleId 角色ID
     * @return 用户数量
     */
    R<Long> countRoleUsers(Long roleId);

    // ==================== 系统初始化 ====================

    /**
     * 初始化系统默认角色
     * 创建 DEVELOPER、USER、GUEST 三个系统角色
     * 
     * @return 初始化结果
     */
    R<Void> initializeSystemRoles();

    /**
     * 检查系统角色是否已初始化
     * 
     * @return 是否已初始化
     */
    R<Boolean> isSystemRolesInitialized();
}
