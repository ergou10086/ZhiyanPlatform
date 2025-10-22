package hbnu.project.zhiyanauth.controller;

import hbnu.project.zhiyanauth.model.dto.PermissionDTO;
import hbnu.project.zhiyanauth.model.form.*;
import hbnu.project.zhiyanauth.model.response.PermissionDetailResponse;
import hbnu.project.zhiyanauth.model.response.PermissionInfoResponse;
import hbnu.project.zhiyanauth.service.PermissionService;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 权限管理控制器
 * 提供系统权限的管理、查询和校验功能
 * <p>
 * 职责说明：
 * - 本控制器专注于系统权限管理
 * - 所有管理接口需要 DEVELOPER 角色权限
 * - 权限校验接口供内部调用（API网关/微服务）
 * - 所有接口需要 JWT token 认证（除内部校验接口外）
 *
 * @author ErgouTree
 * @version 3.0
 * @rewrite Tokito
 */
@RestController
@RequestMapping("/auth/permissions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "权限管理", description = "系统权限管理相关接口")
public class PermissionController {

    private final PermissionService permissionService;
    private final RoleService roleService;

    // ==================== 权限信息查询接口 ====================

    /**
     * 获取所有权限列表（分页）
     * 路径: GET /auth/permissions
     * 角色: DEVELOPER（只有开发者可以查看所有权限）
     */
    @GetMapping
    @PreAuthorize("hasRole('DEVELOPER')")
    @Operation(summary = "获取权限列表", description = "分页查询所有系统权限（管理员功能）")
    public R<Page<PermissionInfoResponse>> getAllPermissions(
            @Parameter(description = "页码，从0开始")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "20") int size) {
        log.info("获取权限列表: 页码={}, 每页数量={}", page, size);

        try {
            // 创建分页参数
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

            // 调用服务层
            R<Page<PermissionDTO>> result = permissionService.getAllPermissions(pageable);
            if (!R.isSuccess(result)) {
                return R.fail(result.getMsg());
            }

            // 转换为Response对象
            Page<PermissionDTO> permissionPage = result.getData();
            Page<PermissionInfoResponse> responsePage = permissionPage.map(this::convertToPermissionInfoResponse);

            return R.ok(responsePage);
        } catch (Exception e) {
            log.error("获取权限列表失败", e);
            return R.fail("获取权限列表失败");
        }
    }

    /**
     * 根据ID获取权限详情
     * 路径: GET /auth/permissions/{permissionId}
     * 角色: DEVELOPER（只有开发者可以查看权限详情）
     */
    @GetMapping("/{permissionId}")
    @PreAuthorize("hasRole('DEVELOPER')")
    @Operation(summary = "获取权限详情", description = "根据ID获取权限详细信息（包含拥有该权限的角色列表）")
    public R<PermissionDetailResponse> getPermissionById(
            @Parameter(description = "权限ID", required = true)
            @PathVariable Long permissionId) {
        log.info("获取权限详情: permissionId={}", permissionId);

        try {
            // 查询权限基本信息
            R<PermissionDTO> permissionResult = permissionService.getPermissionById(permissionId);
            if (!R.isSuccess(permissionResult)) {
                return R.fail(permissionResult.getMsg());
            }

            PermissionDTO permissionDTO = permissionResult.getData();

            // 查询角色数量
            R<Long> countResult = permissionService.countPermissionRoles(permissionId);
            Long roleCount = R.isSuccess(countResult) ? countResult.getData() : 0L;

            // 构建详情响应
            PermissionDetailResponse response = PermissionDetailResponse.builder()
                    .id(permissionDTO.getId())
                    .name(permissionDTO.getName())
                    .description(permissionDTO.getDescription())
                    .roleCount(roleCount)
                    .build();

            return R.ok(response);
        } catch (Exception e) {
            log.error("获取权限详情失败: permissionId={}", permissionId, e);
            return R.fail("获取权限详情失败");
        }
    }

    // ==================== 权限管理接口（CRUD） ====================

    /**
     * 创建新权限
     * 路径: POST /auth/permissions
     * 角色: DEVELOPER（只有开发者可以创建权限）
     */
    @PostMapping
    @PreAuthorize("hasRole('DEVELOPER')")
    @Operation(summary = "创建权限", description = "创建新的系统权限（管理员功能）")
    public R<PermissionInfoResponse> createPermission(
            @Valid @RequestBody CreatePermissionBody request) {
        log.info("创建权限: name={}", request.getName());

        try {
            // 构建PermissionDTO
            PermissionDTO permissionDTO = PermissionDTO.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .build();

            // 创建权限
            R<PermissionDTO> createResult = permissionService.createPermission(permissionDTO);
            if (!R.isSuccess(createResult)) {
                return R.fail(createResult.getMsg());
            }

            // 构建响应
            PermissionDTO createdPermission = createResult.getData();
            PermissionInfoResponse response = convertToPermissionInfoResponse(createdPermission);

            return R.ok(response, "权限创建成功");
        } catch (Exception e) {
            log.error("创建权限失败: request={}", request, e);
            return R.fail("创建权限失败");
        }
    }

    /**
     * 更新权限信息
     * 路径: PUT /auth/permissions/{permissionId}
     * 角色: DEVELOPER（只有开发者可以更新权限）
     */
    @PutMapping("/{permissionId}")
    @PreAuthorize("hasRole('DEVELOPER')")
    @Operation(summary = "更新权限", description = "更新权限基本信息（管理员功能）")
    public R<PermissionInfoResponse> updatePermission(
            @Parameter(description = "权限ID", required = true)
            @PathVariable Long permissionId,
            @Valid @RequestBody UpdatePermissionBody request) {
        log.info("更新权限: permissionId={}, name={}", permissionId, request.getName());

        try {
            // 构建PermissionDTO
            PermissionDTO permissionDTO = PermissionDTO.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .build();

            // 更新权限
            R<PermissionDTO> updateResult = permissionService.updatePermission(permissionId, permissionDTO);
            if (!R.isSuccess(updateResult)) {
                return R.fail(updateResult.getMsg());
            }

            // 构建响应
            PermissionDTO updatedPermission = updateResult.getData();
            PermissionInfoResponse response = convertToPermissionInfoResponse(updatedPermission);

            return R.ok(response, "权限更新成功");
        } catch (Exception e) {
            log.error("更新权限失败: permissionId={}, request={}", permissionId, request, e);
            return R.fail("更新权限失败");
        }
    }

    /**
     * 删除权限
     * 路径: DELETE /auth/permissions/{permissionId}
     * 角色: DEVELOPER（只有开发者可以删除权限）
     */
    @DeleteMapping("/{permissionId}")
    @PreAuthorize("hasRole('DEVELOPER')")
    @Operation(summary = "删除权限", description = "删除指定权限（管理员功能，已被角色使用的权限不可删除）")
    public R<Void> deletePermission(
            @Parameter(description = "权限ID", required = true)
            @PathVariable Long permissionId) {
        log.info("删除权限: permissionId={}", permissionId);

        return permissionService.deletePermission(permissionId);
    }

    /**
     * 批量创建权限
     * 路径: POST /auth/permissions/batch
     * 角色: DEVELOPER（只有开发者可以批量创建权限）
     */
    @PostMapping("/batch")
    @PreAuthorize("hasRole('DEVELOPER')")
    @Operation(summary = "批量创建权限", description = "批量创建多个权限（管理员功能）")
    public R<List<PermissionInfoResponse>> batchCreatePermissions(
            @Valid @RequestBody BatchCreatePermissionsBody request) {
        log.info("批量创建权限: 数量={}", request.getPermissions().size());

        try {
            // 构建PermissionDTO列表
            List<PermissionDTO> permissionDTOs = request.getPermissions().stream()
                    .map(p -> PermissionDTO.builder()
                            .name(p.getName())
                            .description(p.getDescription())
                            .build())
                    .toList();

            // 批量创建
            R<List<PermissionDTO>> createResult = permissionService.batchCreatePermissions(permissionDTOs);
            if (!R.isSuccess(createResult)) {
                return R.fail(createResult.getMsg());
            }

            // 构建响应
            List<PermissionInfoResponse> responses = createResult.getData().stream()
                    .map(this::convertToPermissionInfoResponse)
                    .toList();

            return R.ok(responses, createResult.getMsg());
        } catch (Exception e) {
            log.error("批量创建权限失败", e);
            return R.fail("批量创建权限失败");
        }
    }

    // ==================== 权限校验接口（内部调用） ====================

    /**
     * 检查用户是否拥有指定权限
     * 路径: POST /auth/permissions/check
     * 权限: 内部接口，供API网关或其他微服务调用
     * 注意：此接口在 SecurityConfig 中配置为 permitAll，不需要认证
     */
    @PostMapping("/check")
    @Operation(summary = "检查用户权限", description = "检查指定用户是否拥有指定权限（内部接口）")
    public R<Boolean> checkPermission(
            @Valid @RequestBody PermissionCheckBody request) {
        log.debug("检查用户权限: userId={}, permission={}", request.getUserId(), request.getPermission());

        return permissionService.hasPermission(request.getUserId(), request.getPermission());
    }

    /**
     * 批量检查用户权限
     * 路径: POST /auth/permissions/check-batch
     * 权限: 内部接口，供API网关或其他微服务调用
     * 注意：此接口在 SecurityConfig 中配置为 permitAll，不需要认证
     */
    @PostMapping("/check-batch")
    @Operation(summary = "批量检查用户权限", description = "批量检查用户是否拥有指定权限列表中的任一权限（内部接口）")
    public R<Map<String, Boolean>> checkPermissionBatch(
            @Valid @RequestBody BatchPermissionCheckBody request) {
        log.debug("批量检查用户权限: userId={}, 权限数量={}", 
                request.getUserId(), request.getPermissions().size());

        try {
            // 获取用户所有权限
            R<Set<String>> userPermissionsResult = permissionService.getUserPermissions(request.getUserId());
            if (!R.isSuccess(userPermissionsResult)) {
                return R.fail(userPermissionsResult.getMsg());
            }

            Set<String> userPermissions = userPermissionsResult.getData();

            // 检查每个权限
            Map<String, Boolean> result = new HashMap<>();
            for (String permission : request.getPermissions()) {
                result.put(permission, userPermissions.contains(permission));
            }

            return R.ok(result);
        } catch (Exception e) {
            log.error("批量检查用户权限失败: request={}", request, e);
            return R.fail("权限批量检查失败");
        }
    }

    /**
     * 检查用户是否拥有任一权限
     * 路径: POST /auth/permissions/check-any
     * 权限: 内部接口，供API网关或其他微服务调用
     */
    @PostMapping("/check-any")
    @Operation(summary = "检查用户任一权限", description = "检查用户是否拥有权限列表中的任一权限（内部接口）")
    public R<Boolean> checkAnyPermission(
            @Valid @RequestBody BatchPermissionCheckBody request) {
        log.debug("检查用户任一权限: userId={}, 权限数量={}", 
                request.getUserId(), request.getPermissions().size());

        return permissionService.hasAnyPermission(request.getUserId(), request.getPermissions());
    }

    /**
     * 检查用户是否拥有所有权限
     * 路径: POST /auth/permissions/check-all
     * 权限: 内部接口，供API网关或其他微服务调用
     */
    @PostMapping("/check-all")
    @Operation(summary = "检查用户所有权限", description = "检查用户是否拥有权限列表中的所有权限（内部接口）")
    public R<Boolean> checkAllPermissions(
            @Valid @RequestBody BatchPermissionCheckBody request) {
        log.debug("检查用户所有权限: userId={}, 权限数量={}", 
                request.getUserId(), request.getPermissions().size());

        return permissionService.hasAllPermissions(request.getUserId(), request.getPermissions());
    }

    /**
     * 获取用户的所有权限
     * 路径: GET /auth/permissions/user/{userId}
     * 权限: 内部接口，供API网关或其他微服务调用
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户权限", description = "获取指定用户的所有权限（内部接口）")
    public R<Set<String>> getUserPermissions(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        log.debug("获取用户权限: userId={}", userId);

        return permissionService.getUserPermissions(userId);
    }

    // ==================== 权限角色查询接口 ====================

    /**
     * 获取拥有指定权限的角色列表
     * 路径: GET /auth/permissions/{permissionId}/roles
     * 角色: DEVELOPER（只有开发者可以查看权限角色）
     */
    @GetMapping("/{permissionId}/roles")
    @PreAuthorize("hasRole('DEVELOPER')")
    @Operation(summary = "获取权限角色列表", description = "获取拥有指定权限的所有角色（管理员功能）")
    public R<Page<Long>> getPermissionRoles(
            @Parameter(description = "权限ID", required = true)
            @PathVariable Long permissionId,
            @Parameter(description = "页码，从0开始")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "20") int size) {
        log.info("获取权限角色列表: permissionId={}, page={}, size={}", permissionId, page, size);

        try {
            Pageable pageable = PageRequest.of(page, size);
            return permissionService.getPermissionRoles(permissionId, pageable);
        } catch (Exception e) {
            log.error("获取权限角色列表失败: permissionId={}", permissionId, e);
            return R.fail("获取权限角色列表失败");
        }
    }

    // ==================== 系统初始化接口 ====================

    /**
     * 初始化系统默认权限
     * 路径: POST /auth/permissions/initialize
     * 角色: DEVELOPER（只有开发者可以初始化系统权限）
     */
    @PostMapping("/initialize")
    @PreAuthorize("hasRole('DEVELOPER')")
    @Operation(summary = "初始化系统权限", description = "根据SystemPermission枚举创建所有系统权限（管理员功能）")
    public R<Void> initializeSystemPermissions() {
        log.info("初始化系统权限");

        return permissionService.initializeSystemPermissions();
    }

    /**
     * 检查系统权限是否已初始化
     * 路径: GET /auth/permissions/initialized
     * 权限: 无需认证（用于系统启动检查）
     */
    @GetMapping("/initialized")
    @Operation(summary = "检查系统权限初始化状态", description = "检查系统默认权限是否已创建")
    public R<Boolean> isSystemPermissionsInitialized() {
        log.info("检查系统权限初始化状态");

        return permissionService.isSystemPermissionsInitialized();
    }

    // ==================== 辅助方法 ====================

    /**
     * 将PermissionDTO转换为PermissionInfoResponse
     */
    private PermissionInfoResponse convertToPermissionInfoResponse(PermissionDTO permissionDTO) {
        // 查询角色数量
        R<Long> countResult = permissionService.countPermissionRoles(permissionDTO.getId());
        Long roleCount = R.isSuccess(countResult) ? countResult.getData() : 0L;

        return PermissionInfoResponse.builder()
                .id(permissionDTO.getId())
                .name(permissionDTO.getName())
                .description(permissionDTO.getDescription())
                .roleCount(roleCount)
                .build();
    }
}
