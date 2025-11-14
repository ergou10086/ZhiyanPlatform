package hbnu.project.zhiyanauth.controller;

import hbnu.project.common.log.annotation.AccessLog;
import hbnu.project.common.log.annotation.OperationLog;
import hbnu.project.common.log.annotation.OperationType;
import hbnu.project.zhiyanauth.model.dto.RoleDTO;
import hbnu.project.zhiyanauth.model.form.*;
import hbnu.project.zhiyanauth.model.response.RoleDetailResponse;
import hbnu.project.zhiyanauth.model.response.RoleInfoResponse;
import hbnu.project.zhiyanauth.service.RoleService;
import hbnu.project.zhiyancommonbasic.domain.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
 * 角色管理控制器
 * 提供系统角色的管理、分配和查询功能
 * 
 * 职责说明：
 * - 本控制器专注于系统角色管理（DEVELOPER、USER、GUEST）
 * - 所有管理接口需要 DEVELOPER 角色权限
 * - 查询接口根据需要设置不同的权限级别
 * - 所有接口需要 JWT token 认证
 *
 * @author ErgouTree
 * @version 3.0
 * @rewrite Tokito
 */
@RestController
@RequestMapping("/zhiyan/auth/roles")   // 原 /auth/roles
@RequiredArgsConstructor
@Slf4j
@Tag(name = "角色管理", description = "系统角色管理相关接口")
@AccessLog("角色管理")
public class RoleController {

    private final RoleService roleService;

    // ==================== 角色信息查询接口 ====================

    /**
     * 获取所有角色列表（分页）
     * 路径: GET /auth/roles
     * 角色: DEVELOPER（只有开发者可以查看所有角色）
     */
    @GetMapping
    @PreAuthorize("hasRole('DEVELOPER')")
    @Operation(summary = "获取角色列表", description = "分页查询所有系统角色（管理员功能）")
    @OperationLog(module = "角色管理", type = OperationType.QUERY, description = "分页获取所有的角色列表，管理员调用")
    public R<Page<RoleInfoResponse>> getAllRoles(
            @Parameter(description = "页码，从0开始")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "20") int size) {
        log.info("获取角色列表: 页码={}, 每页数量={}", page, size);

        try {
            // 创建分页参数
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

            // 调用服务层
            R<Page<RoleDTO>> result = roleService.getAllRoles(pageable);
            if (!R.isSuccess(result)) {
                return R.fail(result.getMsg());
            }

            // 转换为Response对象
            Page<RoleDTO> rolePage = result.getData();
            Page<RoleInfoResponse> responsePage = rolePage.map(this::convertToRoleInfoResponse);

            return R.ok(responsePage);
        } catch (Exception e) {
            log.error("获取角色列表失败", e);
            return R.fail("获取角色列表失败");
        }
    }


    /**
     * 根据ID获取角色详情
     * 路径: GET /auth/roles/{roleId}
     * 角色: DEVELOPER（只有开发者可以查看角色详情）
     */
    @GetMapping("/{roleId}")
    @PreAuthorize("hasRole('DEVELOPER')")
    @Operation(summary = "获取角色详情", description = "根据ID获取角色详细信息（包含权限列表）")
    @OperationLog(module = "角色管理", type = OperationType.QUERY, description = "根据ID获取角色详细信息及其权限内容")
    public R<RoleDetailResponse> getRoleById(
            @Parameter(description = "角色ID", required = true)
            @PathVariable Long roleId) {
        log.info("获取角色详情: roleId={}", roleId);

        try {
            // 查询角色基本信息
            R<RoleDTO> roleResult = roleService.getRoleById(roleId);
            if (!R.isSuccess(roleResult)) {
                return R.fail(roleResult.getMsg());
            }

            RoleDTO roleDTO = roleResult.getData();

            // 查询角色权限
            R<Set<String>> permissionsResult = roleService.getRolePermissions(roleId);
            Set<String> permissions = R.isSuccess(permissionsResult) ? 
                    permissionsResult.getData() : Set.of();

            // 查询用户数量
            R<Long> countResult = roleService.countRoleUsers(roleId);
            Long userCount = R.isSuccess(countResult) ? countResult.getData() : 0L;

            // 构建详情响应
            RoleDetailResponse response = RoleDetailResponse.builder()
                    .id(roleDTO.getId())
                    .name(roleDTO.getName())
                    .description(roleDTO.getDescription())
                    .roleType(roleDTO.getRoleType())
                    .isSystemDefault(roleDTO.getIsSystemDefault())
                    .userCount(userCount)
                    .permissions(permissions)
                    .build();

            return R.ok(response);
        } catch (Exception e) {
            log.error("获取角色详情失败: roleId={}", roleId, e);
            return R.fail("获取角色详情失败");
        }
    }

    // ==================== 角色管理接口（CRUD） ====================

    /**
     * 创建新角色
     * 路径: POST /auth/roles
     * 角色: DEVELOPER（只有开发者可以创建角色）
     */
    @PostMapping
    @PreAuthorize("hasRole('DEVELOPER')")
    @Operation(summary = "创建角色", description = "创建新的系统角色（管理员功能）")
    @OperationLog(module = "角色管理", type = OperationType.INSERT, description = "创建角色")
    public R<RoleInfoResponse> createRole(
            @Valid @RequestBody CreateRoleBody request) {
        log.info("创建角色: name={}", request.getName());

        try {
            // 构建RoleDTO
            RoleDTO roleDTO = RoleDTO.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .build();

            // 创建角色
            R<RoleDTO> createResult = roleService.createRole(roleDTO);
            if (!R.isSuccess(createResult)) {
                return R.fail(createResult.getMsg());
            }

            RoleDTO createdRole = createResult.getData();

            // 如果指定了权限，则分配权限
            if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
                R<Void> assignResult = roleService.assignPermissionsToRole(
                        createdRole.getId(),
                        request.getPermissionIds()
                );
                if (!R.isSuccess(assignResult)) {
                    log.warn("角色创建成功但权限分配失败: roleId={}", createdRole.getId());
                }
            }

            // 构建响应
            RoleInfoResponse response = convertToRoleInfoResponse(createdRole);

            return R.ok(response, "角色创建成功");
        } catch (Exception e) {
            log.error("创建角色失败: request={}", request, e);
            return R.fail("创建角色失败");
        }
    }

    /**
     * 更新角色信息
     * 路径: PUT /auth/roles/{roleId}
     * 角色: DEVELOPER（只有开发者可以更新角色）
     */
    @PutMapping("/{roleId}")
    @PreAuthorize("hasRole('DEVELOPER')")
    @Operation(summary = "更新角色", description = "更新角色基本信息（管理员功能）")
    @OperationLog(module = "角色管理", type = OperationType.UPDATE, description = "更新角色")
    public R<RoleInfoResponse> updateRole(
            @Parameter(description = "角色ID", required = true)
            @PathVariable Long roleId,
            @Valid @RequestBody UpdateRoleBody request) {
        log.info("更新角色: roleId={}, name={}", roleId, request.getName());

        try {
            // 构建RoleDTO
            RoleDTO roleDTO = RoleDTO.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .build();

            // 更新角色
            R<RoleDTO> updateResult = roleService.updateRole(roleId, roleDTO);
            if (!R.isSuccess(updateResult)) {
                return R.fail(updateResult.getMsg());
            }

            // 构建响应
            RoleDTO updatedRole = updateResult.getData();
            RoleInfoResponse response = convertToRoleInfoResponse(updatedRole);

            return R.ok(response, "角色更新成功");
        } catch (Exception e) {
            log.error("更新角色失败: roleId={}, request={}", roleId, request, e);
            return R.fail("更新角色失败");
        }
    }

    /**
     * 删除角色
     * 路径: DELETE /auth/roles/{roleId}
     * 角色: DEVELOPER（只有开发者可以删除角色）
     */
    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasRole('DEVELOPER')")
    @Operation(summary = "删除角色", description = "删除指定角色（管理员功能，系统默认角色不可删除）")
    @OperationLog(module = "角色管理", type = OperationType.DELETE, description = "删除角色")
    public R<Void> deleteRole(
            @Parameter(description = "角色ID", required = true)
            @PathVariable Long roleId) {
        log.info("删除角色: roleId={}", roleId);

        return roleService.deleteRole(roleId);
    }

    // ==================== 用户角色管理接口 ====================

    /**
     * 为用户分配角色
     * 路径: POST /auth/roles/assign-user
     * 角色: DEVELOPER（只有开发者可以分配角色）
     */
    @PostMapping("/assign-user")
    @PreAuthorize("hasRole('DEVELOPER')")
    @Operation(summary = "为用户分配角色", description = "为指定用户分配一个或多个角色（管理员功能）")
    @OperationLog(module = "角色管理", type = OperationType.INSERT, description = "为用户分配角色")
    public R<Void> assignRolesToUser(
            @Valid @RequestBody AssignUserRoleBody request) {
        log.info("为用户分配角色: userId={}, roleIds={}", request.getUserId(), request.getRoleIds());

        return roleService.assignRolesToUser(request.getUserId(), request.getRoleIds());
    }

    /**
     * 移除用户的角色
     * 路径: POST /auth/roles/remove-user
     * 角色: DEVELOPER（只有开发者可以移除角色）
     */
    @PostMapping("/remove-user")
    @PreAuthorize("hasRole('DEVELOPER')")
    @Operation(summary = "移除用户角色", description = "移除用户的指定角色（管理员功能）")
    @OperationLog(module = "角色管理", type = OperationType.UPDATE, description = "移除用户的角色")
    public R<Void> removeRolesFromUser(
            @Valid @RequestBody RemoveUserRoleBody request) {
        log.info("移除用户角色: userId={}, roleIds={}", request.getUserId(), request.getRoleIds());

        return roleService.removeRolesFromUser(request.getUserId(), request.getRoleIds());
    }

    /**
     * 获取用户的所有角色
     * 路径: GET /auth/roles/user/{userId}
     * 角色: DEVELOPER（只有开发者可以查看用户角色）
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('DEVELOPER')")
    @Operation(summary = "获取用户角色", description = "获取指定用户的所有角色（管理员功能）")
    public R<Set<String>> getUserRoles(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        log.info("获取用户角色: userId={}", userId);

        return roleService.getUserRoles(userId);
    }

    // ==================== 角色权限管理接口 ====================

    /**
     * 为角色分配权限
     * 路径: POST /auth/roles/{roleId}/permissions
     * 角色: DEVELOPER（只有开发者可以分配权限）
     */
    @PostMapping("/{roleId}/permissions")
    @PreAuthorize("hasRole('DEVELOPER')")
    @Operation(summary = "为角色分配权限", description = "为指定角色分配权限（管理员功能）")
    @OperationLog(module = "角色管理", type = OperationType.GRANT, description = "分配角色权限")
    public R<Void> assignPermissions(
            @Parameter(description = "角色ID", required = true)
            @PathVariable Long roleId,
            @Valid @RequestBody AssignPermissionsBody request) {
        log.info("为角色分配权限: roleId={}, permissionIds数量={}", roleId, request.getPermissionIds().size());

        return roleService.assignPermissionsToRole(roleId, request.getPermissionIds());
    }

    /**
     * 移除角色的权限
     * 路径: DELETE /auth/roles/{roleId}/permissions
     * 角色: DEVELOPER（只有开发者可以移除权限）
     */
    @DeleteMapping("/{roleId}/permissions")
    @PreAuthorize("hasRole('DEVELOPER')")
    @Operation(summary = "移除角色权限", description = "移除角色的指定权限（管理员功能）")
    @OperationLog(module = "角色管理", type = OperationType.UPDATE, description = "移除角色权限")
    public R<Void> removePermissions(
            @Parameter(description = "角色ID", required = true)
            @PathVariable Long roleId,
            @Valid @RequestBody RemovePermissionsBody request) {
        log.info("移除角色权限: roleId={}, permissionIds数量={}", roleId, request.getPermissionIds().size());

        return roleService.removePermissionsFromRole(roleId, request.getPermissionIds());
    }

    /**
     * 获取角色的所有权限
     * 路径: GET /auth/roles/{roleId}/permissions
     * 角色: DEVELOPER（只有开发者可以查看角色权限）
     */
    @GetMapping("/{roleId}/permissions")
    @PreAuthorize("hasRole('DEVELOPER')")
    @Operation(summary = "获取角色权限", description = "获取指定角色的所有权限（管理员功能）")
    @OperationLog(module = "角色管理", type = OperationType.QUERY, description = "获取角色的所有权限")
    public R<Set<String>> getRolePermissions(
            @Parameter(description = "角色ID", required = true)
            @PathVariable Long roleId) {
        log.info("获取角色权限: roleId={}", roleId);

        return roleService.getRolePermissions(roleId);
    }

    // ==================== 角色用户查询接口 ====================

    /**
     * 获取拥有指定角色的用户列表
     * 路径: GET /auth/roles/{roleId}/users
     * 角色: DEVELOPER（只有开发者可以查看角色用户）
     */
    @GetMapping("/{roleId}/users")
    @PreAuthorize("hasRole('DEVELOPER')")
    @Operation(summary = "获取角色用户", description = "获取拥有指定角色的用户ID列表（管理员功能）")
    public R<Page<Long>> getRoleUsers(
            @Parameter(description = "角色ID", required = true)
            @PathVariable Long roleId,
            @Parameter(description = "页码，从0开始")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "20") int size) {
        log.info("获取角色用户: roleId={}, page={}, size={}", roleId, page, size);

        try {
            Pageable pageable = PageRequest.of(page, size);
            return roleService.getRoleUsers(roleId, pageable);
        } catch (Exception e) {
            log.error("获取角色用户失败: roleId={}", roleId, e);
            return R.fail("获取角色用户失败");
        }
    }

    // ==================== 系统初始化接口 ====================

    /**
     * 初始化系统默认角色
     * 路径: POST /auth/roles/initialize
     * 角色: DEVELOPER（只有开发者可以初始化系统角色）
     */
    @PostMapping("/initialize")
    @PreAuthorize("hasRole('DEVELOPER')")
    @Operation(summary = "初始化系统角色", description = "创建DEVELOPER、USER、GUEST三个系统默认角色（管理员功能）")
    public R<Void> initializeSystemRoles() {
        log.info("初始化系统角色");

        return roleService.initializeSystemRoles();
    }

    /**
     * 检查系统角色是否已初始化
     * 路径: GET /auth/roles/initialized
     * 权限: 无需认证（用于系统启动检查）
     */
    @GetMapping("/initialized")
    @Operation(summary = "检查系统角色初始化状态", description = "检查系统默认角色是否已创建")
    public R<Boolean> isSystemRolesInitialized() {
        log.info("检查系统角色初始化状态");

        return roleService.isSystemRolesInitialized();
    }

    // ==================== 辅助方法 ====================

    /**
     * 将RoleDTO转换为RoleInfoResponse
     */
    private RoleInfoResponse convertToRoleInfoResponse(RoleDTO roleDTO) {
        // 查询用户数量
        R<Long> countResult = roleService.countRoleUsers(roleDTO.getId());
        Long userCount = R.isSuccess(countResult) ? countResult.getData() : 0L;

        return RoleInfoResponse.builder()
                .id(roleDTO.getId())
                .name(roleDTO.getName())
                .description(roleDTO.getDescription())
                .userCount(userCount)
                .build();
    }
}
