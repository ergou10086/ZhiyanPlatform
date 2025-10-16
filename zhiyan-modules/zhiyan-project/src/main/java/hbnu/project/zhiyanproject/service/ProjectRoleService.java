package hbnu.project.zhiyanproject.service;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyanproject.model.entity.ProjectRole;
import hbnu.project.zhiyanproject.model.enums.ProjectMemberRole;
import hbnu.project.zhiyanproject.model.enums.ProjectPermission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

/**
 * 项目角色服务接口
 *
 * @author Tokito
 */
public interface ProjectRoleService {

    /**
     * 创建项目角色
     *
     * @param projectId 项目ID
     * @param roleEnum 角色枚举
     * @param customRoleName 自定义角色名称（可选）
     * @return 创建结果
     */
    R<ProjectRole> createProjectRole(Long projectId, ProjectMemberRole roleEnum, String customRoleName);

    /**
     * 删除项目角色
     *
     * @param roleId 角色ID
     * @return 删除结果
     */
    R<Void> deleteProjectRole(Long roleId);

    /**
     * 更新项目角色
     *
     * @param roleId 角色ID
     * @param name 角色名称
     * @param description 角色描述
     * @return 更新结果
     */
    R<ProjectRole> updateProjectRole(Long roleId, String name, String description);

    /**
     * 根据ID查询项目角色
     *
     * @param roleId 角色ID
     * @return 项目角色
     */
    R<ProjectRole> getProjectRoleById(Long roleId);

    /**
     * 根据项目ID查询项目角色列表
     *
     * @param projectId 项目ID
     * @param pageable 分页参数
     * @return 项目角色列表
     */
    R<Page<ProjectRole>> getProjectRolesByProjectId(Long projectId, Pageable pageable);

    /**
     * 为用户分配项目角色
     *
     * @param userId 用户ID
     * @param projectId 项目ID
     * @param roleEnum 角色枚举
     * @return 分配结果
     */
    R<Void> assignRoleToUser(Long userId, Long projectId, ProjectMemberRole roleEnum);

    /**
     * 移除用户的项目角色
     *
     * @param userId 用户ID
     * @param projectId 项目ID
     * @return 移除结果
     */
    R<Void> removeUserRole(Long userId, Long projectId);

    /**
     * 获取用户在项目中的角色
     *
     * @param userId 用户ID
     * @param projectId 项目ID
     * @return 用户角色
     */
    R<ProjectMemberRole> getUserRoleInProject(Long userId, Long projectId);

    /**
     * 获取用户在项目中的权限
     *
     * @param userId 用户ID
     * @param projectId 项目ID
     * @return 权限列表
     */
    R<Set<ProjectPermission>> getUserPermissionsInProject(Long userId, Long projectId);

    /**
     * 检查用户在项目中是否拥有指定权限
     *
     * @param userId 用户ID
     * @param projectId 项目ID
     * @param permission 权限
     * @return 是否拥有权限
     */
    R<Boolean> hasPermission(Long userId, Long projectId, ProjectPermission permission);

    /**
     * 检查用户在项目中是否拥有指定权限（字符串形式）
     *
     * @param userId 用户ID
     * @param projectId 项目ID
     * @param permissionCode 权限代码
     * @return 是否拥有权限
     */
    R<Boolean> hasPermission(Long userId, Long projectId, String permissionCode);

    /**
     * 获取项目中的所有成员及其角色
     *
     * @param projectId 项目ID
     * @param pageable 分页参数
     * @return 成员角色列表
     */
    R<Page<Object>> getProjectMembersWithRoles(Long projectId, Pageable pageable);

    /**
     * 初始化项目默认角色
     *
     * @param projectId 项目ID
     * @return 初始化结果
     */
    R<List<ProjectRole>> initializeDefaultRoles(Long projectId);
}
