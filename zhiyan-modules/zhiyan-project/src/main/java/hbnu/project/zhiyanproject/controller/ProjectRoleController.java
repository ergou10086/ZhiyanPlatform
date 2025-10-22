package hbnu.project.zhiyanproject.controller;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import hbnu.project.zhiyanproject.model.entity.ProjectRole;
import hbnu.project.zhiyanproject.model.enums.ProjectMemberRole;
import hbnu.project.zhiyanproject.model.enums.ProjectPermission;
import hbnu.project.zhiyanproject.service.ProjectRoleService;
import hbnu.project.zhiyanproject.utils.ProjectSecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 项目角色控制器
 * 提供项目内角色的增删改查、分配与权限校验接口
 */
@Slf4j
@RestController
@RequestMapping("/api/projects/roles")
@RequiredArgsConstructor
@Tag(name = "项目角色管理", description = "项目内角色的管理与权限校验接口")
@SecurityRequirement(name = "Bearer Authentication")
public class ProjectRoleController {

    private final ProjectRoleService projectRoleService;
    private final ProjectSecurityUtils projectSecurityUtils;

    /**
     * 创建项目角色（需项目管理权限）
     */
    @PostMapping("/projects/{projectId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "创建项目角色")
    public R<ProjectRole> createProjectRole(
            @PathVariable @Parameter(description = "项目ID") Long projectId,
            @RequestBody @jakarta.validation.Valid hbnu.project.zhiyanproject.model.form.CreateProjectRoleRequest request) {

        Long userId = SecurityUtils.getUserId();
        log.info("用户[{}]在项目[{}]创建角色: {}", userId, projectId, request.getRoleEnum());
        projectSecurityUtils.requirePermission(projectId, ProjectPermission.PROJECT_MANAGE);
        return projectRoleService.createProjectRole(projectId, request.getRoleEnum(), request.getCustomRoleName());
    }

    /**
     * 删除项目角色（需项目管理权限）
     */
    @DeleteMapping("/{roleId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "删除项目角色")
    public R<Void> deleteProjectRole(@PathVariable @Parameter(description = "角色ID") Long roleId,
                                     @RequestParam @Parameter(description = "项目ID") Long projectId) {
        Long userId = SecurityUtils.getUserId();
        log.info("用户[{}]删除项目[{}]的角色[{}]", userId, projectId, roleId);
        projectSecurityUtils.requirePermission(projectId, ProjectPermission.PROJECT_MANAGE);
        return projectRoleService.deleteProjectRole(roleId);
    }

    /**
     * 更新项目角色（需项目管理权限）
     */
    @PutMapping("/{roleId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "更新项目角色")
    public R<ProjectRole> updateProjectRole(
            @PathVariable @Parameter(description = "角色ID") Long roleId,
            @RequestBody @jakarta.validation.Valid hbnu.project.zhiyanproject.model.form.UpdateProjectRoleRequest request) {
        Long userId = SecurityUtils.getUserId();
        log.info("用户[{}]更新项目[{}]角色[{}]", userId, request.getProjectId(), roleId);
        projectSecurityUtils.requirePermission(request.getProjectId(), ProjectPermission.PROJECT_MANAGE);
        return projectRoleService.updateProjectRole(roleId, request.getName(), request.getDescription());
    }

    /**
     * 获取角色详情
     */
    @GetMapping("/{roleId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取角色详情")
    public R<ProjectRole> getProjectRoleById(@PathVariable @Parameter(description = "角色ID") Long roleId) {
        return projectRoleService.getProjectRoleById(roleId);
    }

    /**
     * 获取项目下的角色列表
     */
    @GetMapping("/projects/{projectId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取项目角色列表")
    public R<Page<ProjectRole>> getProjectRoles(@PathVariable @Parameter(description = "项目ID") Long projectId,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size,
                                                @RequestParam(defaultValue = "createdAt") String sortBy,
                                                @RequestParam(defaultValue = "DESC") String direction) {
        Sort.Direction sortDirection = "ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return projectRoleService.getProjectRolesByProjectId(projectId, pageable);
    }

    /**
     * 为用户分配项目角色（需项目管理权限）
     */
    @PostMapping("/projects/{projectId}/assign")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "分配用户角色")
    public R<Void> assignRoleToUser(
            @PathVariable @Parameter(description = "项目ID") Long projectId,
            @RequestBody @jakarta.validation.Valid hbnu.project.zhiyanproject.model.form.AssignRoleRequest request) {
        Long operatorId = SecurityUtils.getUserId();
        log.info("用户[{}]为用户[{}]在项目[{}]分配角色[{}]", operatorId, request.getUserId(), projectId, request.getRoleEnum());
        projectSecurityUtils.requirePermission(projectId, ProjectPermission.PROJECT_MANAGE);
        return projectRoleService.assignRoleToUser(request.getUserId(), projectId, request.getRoleEnum());
    }

    /**
     * 移除用户的项目角色（需项目管理权限）
     */
    @DeleteMapping("/projects/{projectId}/assign/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "移除用户角色")
    public R<Void> removeUserRole(@PathVariable @Parameter(description = "项目ID") Long projectId,
                                  @PathVariable @Parameter(description = "用户ID") Long userId) {
        Long operatorId = SecurityUtils.getUserId();
        log.info("用户[{}]移除用户[{}]在项目[{}]的角色", operatorId, userId, projectId);
        projectSecurityUtils.requirePermission(projectId, ProjectPermission.PROJECT_MANAGE);
        return projectRoleService.removeUserRole(userId, projectId);
    }

    /**
     * 获取当前用户在项目中的角色
     */
    @GetMapping("/projects/{projectId}/my-role")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取我的项目角色")
    public R<ProjectMemberRole> getMyRole(@PathVariable @Parameter(description = "项目ID") Long projectId) {
        Long currentUserId = SecurityUtils.getUserId();
        return projectRoleService.getUserRoleInProject(currentUserId, projectId);
    }

    /**
     * 获取当前用户在项目中的权限集合
     */
    @GetMapping("/projects/{projectId}/my-permissions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取我的项目权限")
    public R<Set<ProjectPermission>> getMyPermissions(@PathVariable @Parameter(description = "项目ID") Long projectId) {
        Long currentUserId = SecurityUtils.getUserId();
        return projectRoleService.getUserPermissionsInProject(currentUserId, projectId);
    }

    /**
     * 检查当前用户在项目中是否拥有指定权限（枚举）
     */
    @GetMapping("/projects/{projectId}/has-permission")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "校验我的权限（枚举）")
    public R<Boolean> hasPermission(@PathVariable @Parameter(description = "项目ID") Long projectId,
                                    @RequestParam @Parameter(description = "权限枚举") ProjectPermission permission) {
        Long currentUserId = SecurityUtils.getUserId();
        return projectRoleService.hasPermission(currentUserId, projectId, permission);
    }

    /**
     * 检查当前用户在项目中是否拥有指定权限（字符串代码）
     */
    @GetMapping("/projects/{projectId}/has-permission-code")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "校验我的权限（代码）")
    public R<Boolean> hasPermissionCode(@PathVariable @Parameter(description = "项目ID") Long projectId,
                                        @RequestParam @Parameter(description = "权限代码") String permissionCode) {
        Long currentUserId = SecurityUtils.getUserId();
        return projectRoleService.hasPermission(currentUserId, projectId, permissionCode);
    }

    /**
     * 获取项目成员及其角色列表
     */
    @GetMapping("/projects/{projectId}/members")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取项目成员角色列表")
    public R<Page<Object>> getProjectMembersWithRoles(@PathVariable @Parameter(description = "项目ID") Long projectId,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size,
                                                      @RequestParam(defaultValue = "joinedAt") String sortBy,
                                                      @RequestParam(defaultValue = "DESC") String direction) {
        Sort.Direction sortDirection = "ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return projectRoleService.getProjectMembersWithRoles(projectId, pageable);
    }

    /**
     * 初始化项目默认角色（需项目管理权限）
     */
    @PostMapping("/projects/{projectId}/init-defaults")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "初始化项目默认角色")
    public R<List<ProjectRole>> initializeDefaultRoles(@PathVariable @Parameter(description = "项目ID") Long projectId) {
        Long userId = SecurityUtils.getUserId();
        log.info("用户[{}]初始化项目[{}]默认角色", userId, projectId);
        projectSecurityUtils.requirePermission(projectId, ProjectPermission.PROJECT_MANAGE);
        return projectRoleService.initializeDefaultRoles(projectId);
    }
}


