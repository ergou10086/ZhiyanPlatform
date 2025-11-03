package hbnu.project.zhiyanproject.controller;

import hbnu.project.common.log.annotation.AccessLog;
import hbnu.project.common.log.annotation.OperationLog;
import hbnu.project.common.log.annotation.OperationType;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import hbnu.project.zhiyanproject.model.dto.*;
import hbnu.project.zhiyanproject.model.enums.ProjectMemberRole;
import hbnu.project.zhiyanproject.model.enums.ProjectPermission;
import hbnu.project.zhiyanproject.model.form.AssignRoleRequest;
import hbnu.project.zhiyanproject.service.ProjectMemberService;
import hbnu.project.zhiyanproject.utils.ProjectSecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 项目角色控制器（重构版）
 * 职责：
 * 1. 查询角色定义（从枚举）
 * 2. 查询用户在项目中的角色和权限
 * 3. 为用户分配项目角色
 * 
 * 注意：
 * - 不允许创建/修改/删除角色定义，角色在枚举中预定义
 * - 成员管理功能（更新角色、移除成员、获取成员列表）已移至 ProjectMemberController
 *
 * @author AI Assistant
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "项目角色管理", description = "项目角色查询和分配接口（不允许修改角色定义）")
@SecurityRequirement(name = "Bearer Authentication")
@AccessLog("项目角色管理")
public class ProjectRoleController {

    private final ProjectMemberService projectMemberService;
    private final ProjectSecurityUtils projectSecurityUtils;

    // ==================== 角色定义查询 ====================

    /**
     * 获取所有项目角色定义（从枚举）
     */
    @GetMapping("/definitions")
    @Operation(summary = "获取项目角色定义列表", description = "返回所有项目角色的定义信息（从枚举中获取）")
    public R<List<RoleDefinitionDTO>> getAllRoleDefinitions() {
        log.debug("获取所有项目角色定义");
        
        List<RoleDefinitionDTO> roles = Arrays.stream(ProjectMemberRole.values())
                .map(role -> RoleDefinitionDTO.builder()
                        .code(role.name())
                        .name(role.getRoleName())
                        .description(role.getDescription())
                        .permissions(role.getPermissions().stream()
                                .map(ProjectPermission::getCode)
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
        
        return R.ok(roles);
    }

    /**
     * 获取指定角色的详细信息
     */
    @GetMapping("/definitions/{roleCode}")
    @Operation(summary = "获取角色定义详情", description = "根据角色代码获取角色的详细定义")
    public R<RoleDefinitionDTO> getRoleDefinition(
            @PathVariable @Parameter(description = "角色代码 (OWNER/MEMBER)") String roleCode) {
        
        log.debug("获取角色定义: {}", roleCode);
        
        try {
            ProjectMemberRole role = ProjectMemberRole.valueOf(roleCode);
            RoleDefinitionDTO dto = RoleDefinitionDTO.builder()
                    .code(role.name())
                    .name(role.getRoleName())
                    .description(role.getDescription())
                    .permissions(role.getPermissions().stream()
                            .map(ProjectPermission::getCode)
                            .collect(Collectors.toList()))
                    .build();
            return R.ok(dto);
        } catch (IllegalArgumentException e) {
            log.warn("角色代码不存在: {}", roleCode);
            return R.fail("角色不存在: " + roleCode);
        }
    }

    /**
     * 获取所有权限定义（从枚举）
     */
    @GetMapping("/permissions/definitions")
    @Operation(summary = "获取项目权限定义列表", description = "返回所有项目权限的定义信息（从枚举中获取）")
    public R<List<PermissionDefinitionDTO>> getAllPermissionDefinitions() {
        log.debug("获取所有项目权限定义");
        
        List<PermissionDefinitionDTO> permissions = Arrays.stream(ProjectPermission.values())
                .map(perm -> PermissionDefinitionDTO.builder()
                        .code(perm.getCode())
                        .description(perm.getDescription())
                        .build())
                .collect(Collectors.toList());
        
        return R.ok(permissions);
    }

    // ==================== 用户角色分配 ====================

    /**
     * 为用户分配项目角色（需项目管理权限）
     */
    @PostMapping("/projects/{projectId}/assign")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "分配用户角色", description = "将用户添加为项目成员并分配角色（需要成员管理权限）")
    @OperationLog(module = "项目角色管理", type = OperationType.GRANT, description = "分配用户角色", recordParams = true, recordResult = false)
    public R<Void> assignRoleToUser(
            @PathVariable @Parameter(description = "项目ID") Long projectId,
            @RequestBody @Valid AssignRoleRequest request) {

        Long operatorId = SecurityUtils.getUserId();
        log.info("用户[{}]为用户[{}]在项目[{}]分配角色[{}]",
                operatorId, request.getUserId(), projectId, request.getRoleCode());

        // 权限检查：只有 OWNER 才能分配角色
        projectSecurityUtils.requirePermission(projectId, ProjectPermission.MEMBER_MANAGE);

        // 验证角色代码
        ProjectMemberRole role;
        try {
            role = ProjectMemberRole.valueOf(request.getRoleCode());
        } catch (IllegalArgumentException e) {
            log.warn("无效的角色代码: {}", request.getRoleCode());
            return R.fail("无效的角色代码: " + request.getRoleCode());
        }

        // 分配角色（添加为项目成员，验证用户存在）
        return projectMemberService.addMemberWithValidation(projectId, request.getUserId(), role);
    }

    // Note: 更新用户角色功能已移至 ProjectMemberController#updateMemberRole
    // Note: 移除成员功能已移至 ProjectMemberController#removeMember

    // ==================== 角色权限查询 ====================

    /**
     * 获取当前用户在项目中的角色
     */
    @GetMapping("/projects/{projectId}/my-role")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取我的项目角色", description = "查询当前用户在指定项目中的角色信息")
    public R<RoleInfoDTO> getMyRole(
            @PathVariable @Parameter(description = "项目ID") Long projectId) {

        Long currentUserId = SecurityUtils.getUserId();
        log.debug("用户[{}]查询在项目[{}]中的角色", currentUserId, projectId);
        
        return projectMemberService.getUserRoleInfo(currentUserId, projectId);
    }

    /**
     * 获取当前用户在项目中的权限集合
     */
    @GetMapping("/projects/{projectId}/my-permissions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取我的项目权限", description = "查询当前用户在指定项目中的所有权限")
    public R<Set<String>> getMyPermissions(
            @PathVariable @Parameter(description = "项目ID") Long projectId) {

        Long currentUserId = SecurityUtils.getUserId();
        log.debug("用户[{}]查询在项目[{}]中的权限", currentUserId, projectId);
        
        return projectMemberService.getUserPermissions(currentUserId, projectId);
    }

    /**
     * 检查当前用户在项目中是否拥有指定权限
     */
    @GetMapping("/projects/{projectId}/has-permission")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "校验我的权限", description = "检查当前用户是否拥有指定的项目权限")
    public R<Boolean> hasPermission(
            @PathVariable @Parameter(description = "项目ID") Long projectId,
            @RequestParam @Parameter(description = "权限代码", example = "project:manage") String permissionCode) {

        Long currentUserId = SecurityUtils.getUserId();
        log.debug("用户[{}]检查在项目[{}]中是否拥有权限[{}]", currentUserId, projectId, permissionCode);
        
        return projectMemberService.hasPermission(currentUserId, projectId, permissionCode);
    }

    // Note: 获取项目成员列表功能已移至 ProjectMemberController#getProjectMembers

    /**
     * 获取指定用户在项目中的角色（项目成员可见）
     */
    @GetMapping("/projects/{projectId}/members/{userId}/role")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "查询用户角色", description = "查询指定用户在项目中的角色")
    public R<RoleInfoDTO> getUserRole(
            @PathVariable @Parameter(description = "项目ID") Long projectId,
            @PathVariable @Parameter(description = "用户ID") Long userId) {

        // 检查当前用户是否是项目成员
        Long currentUserId = SecurityUtils.getUserId();
        if (!projectSecurityUtils.isMember(projectId)) {
            log.warn("用户[{}]不是项目[{}]成员，无权查看", currentUserId, projectId);
            return R.fail("您不是项目成员");
        }

        log.debug("用户[{}]查询用户[{}]在项目[{}]中的角色", currentUserId, userId, projectId);
        
        return projectMemberService.getUserRoleInfo(userId, projectId);
    }
}
