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
 * 处理权限管理和验证
 *
 * @author ErgouTree
 */
public interface PermissionService {

    /**
     * 检查用户是否拥有指定权限
     *
     * @param userId 用户ID
     * @param permission 权限标识
     * @return 是否拥有权限
     */
    R<Boolean> hasPermission(Long userId, String permission);

    /**
     * 获取用户所有权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    R<Set<String>> getUserPermissions(Long userId);

    /**
     * 批量检查权限
     *
     * @param userId 用户ID
     * @param permissions 权限列表
     * @return 权限检查结果
     */
    R<Boolean> hasAnyPermission(Long userId, List<String> permissions);

    /**
     * 获取所有权限列表
     *
     * @param pageable 分页参数
     * @return 权限列表
     */
    R<Page<PermissionDTO>> getAllPermissions(Pageable pageable);

    /**
     * 创建权限
     *
     * @param permissionDTO 权限信息
     * @return 创建结果
     */
    R<PermissionDTO> createPermission(PermissionDTO permissionDTO);

    /**
     * 更新权限
     *
     * @param permissionId 权限ID
     * @param permissionDTO 权限信息
     * @return 更新结果
     */
    R<PermissionDTO> updatePermission(Long permissionId, PermissionDTO permissionDTO);

    /**
     * 删除权限
     *
     * @param permissionId 权限ID
     * @return 删除结果
     */
    R<Void> deletePermission(Long permissionId);

    /**
     * 根据ID查找权限
     *
     * @param permissionId 权限ID
     * @return 权限信息
     */
    Permission findById(Long permissionId);
}
