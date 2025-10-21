package hbnu.project.zhiyanauth.service;

import hbnu.project.zhiyanauth.model.dto.PermissionDTO;
import hbnu.project.zhiyanauth.model.entity.Permission;
import hbnu.project.zhiyancommonbasic.domain.R;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

/**
 * 权限服务接口
 * 提供权限管理、权限校验等核心功能
 * 
 * 职责说明：
 * - 本服务专注于系统权限的管理和校验
 * - 为API网关和其他微服务提供权限校验接口
 * - 支持基于角色的访问控制（RBAC）模型
 * - 权限管理操作仅限 DEVELOPER 角色
 * 
 * @author ErgouTree
 * @version 3.0
 * @rewrite Tokito
 */
public interface PermissionService {

    // ==================== 权限校验接口（内部调用） ====================

    /**
     * 检查用户是否拥有指定权限
     * 供API网关或其他微服务调用，用于权限校验
     * 
     * @param userId 用户ID
     * @param permission 权限标识（如：project:delete, wiki:edit）
     * @return 是否拥有权限
     */
    R<Boolean> hasPermission(Long userId, String permission);

    /**
     * 检查用户是否拥有指定权限列表中的任一权限
     * 用于"满足任一权限即可访问"的场景
     * 
     * @param userId 用户ID
     * @param permissions 权限列表
     * @return 是否拥有任一权限
     */
    R<Boolean> hasAnyPermission(Long userId, List<String> permissions);

    /**
     * 检查用户是否拥有所有指定权限
     * 用于"必须同时拥有多个权限"的场景
     * 
     * @param userId 用户ID
     * @param permissions 权限列表
     * @return 是否拥有所有权限
     */
    R<Boolean> hasAllPermissions(Long userId, List<String> permissions);

    /**
     * 获取用户的所有权限
     * 用于JWT生成、前端权限菜单展示等场景
     * 
     * @param userId 用户ID
     * @return 用户权限名称集合
     */
    R<Set<String>> getUserPermissions(Long userId);

    // ==================== 权限信息查询 ====================

    /**
     * 获取所有权限列表（分页）
     * 
     * @param pageable 分页参数
     * @return 权限列表
     */
    R<Page<PermissionDTO>> getAllPermissions(Pageable pageable);

    /**
     * 根据ID获取权限详细信息
     * 
     * @param permissionId 权限ID
     * @return 权限详细信息
     */
    R<PermissionDTO> getPermissionById(Long permissionId);

    /**
     * 根据名称查找权限
     * 
     * @param permissionName 权限名称
     * @return 权限信息
     */
    R<PermissionDTO> getPermissionByName(String permissionName);

    /**
     * 根据ID查找权限实体（内部使用）
     * 
     * @param permissionId 权限ID
     * @return 权限实体
     */
    Permission findById(Long permissionId);

    // ==================== 权限管理（CRUD） ====================

    /**
     * 创建新权限
     * 仅开发者可用
     * 
     * @param permissionDTO 权限信息
     * @return 创建后的权限信息
     */
    R<PermissionDTO> createPermission(PermissionDTO permissionDTO);

    /**
     * 更新权限信息
     * 仅开发者可用
     * 
     * @param permissionId 权限ID
     * @param permissionDTO 更新的权限信息
     * @return 更新后的权限信息
     */
    R<PermissionDTO> updatePermission(Long permissionId, PermissionDTO permissionDTO);

    /**
     * 删除权限
     * 仅开发者可用，已被角色使用的权限不可删除
     * 
     * @param permissionId 权限ID
     * @return 删除结果
     */
    R<Void> deletePermission(Long permissionId);

    /**
     * 批量创建权限
     * 用于系统初始化或权限快速导入
     * 
     * @param permissionDTOs 权限信息列表
     * @return 创建结果
     */
    R<List<PermissionDTO>> batchCreatePermissions(List<PermissionDTO> permissionDTOs);

    // ==================== 权限角色查询 ====================

    /**
     * 获取拥有指定权限的角色列表
     * 
     * @param permissionId 权限ID
     * @param pageable 分页参数
     * @return 角色ID列表
     */
    R<Page<Long>> getPermissionRoles(Long permissionId, Pageable pageable);

    /**
     * 统计拥有指定权限的角色数量
     * 
     * @param permissionId 权限ID
     * @return 角色数量
     */
    R<Long> countPermissionRoles(Long permissionId);

    // ==================== 系统初始化 ====================

    /**
     * 初始化系统默认权限
     * 根据 SystemPermission 枚举创建所有系统权限
     * 
     * @return 初始化结果
     */
    R<Void> initializeSystemPermissions();

    /**
     * 检查系统权限是否已初始化
     * 
     * @return 是否已初始化
     */
    R<Boolean> isSystemPermissionsInitialized();
}
