package hbnu.project.zhiyanauth.controller;

import hbnu.project.zhiyanauth.model.dto.PermissionDTO;
import hbnu.project.zhiyanauth.model.dto.RoleDTO;
import hbnu.project.zhiyanauth.model.entity.Permission;
import hbnu.project.zhiyanauth.model.entity.Role;
import hbnu.project.zhiyanauth.model.entity.RolePermission;
import hbnu.project.zhiyanauth.model.form.*;
import hbnu.project.zhiyanauth.model.response.PermissionDetailResponse;
import hbnu.project.zhiyanauth.model.response.PermissionInfoResponse;
import hbnu.project.zhiyanauth.model.response.RoleInfoResponse;
import hbnu.project.zhiyanauth.repository.RolePermissionRepository;
import hbnu.project.zhiyanauth.repository.UserRoleRepository;
import hbnu.project.zhiyanauth.service.PermissionService;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 系统权限管理控制器
 * 进行权限的CRUD，查看拥有某权限的角色列表，权限校验等
 *
 * @author Tokito
 */
@RestController
//@RequestMapping("/auth/permissions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "权限管理", description = "系统权限管理相关接口")
public class PermissionController {

    private final PermissionService permissionService;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRoleRepository userRoleRepository;

    /**
     * 获取所有权限列表（分页）
     * 需要权限：system:permission:list
     */
    @GetMapping
    //@PreAuthorize("hasAuthority('system:permission:list')")
    @Operation(summary = "获取权限列表", description = "获取系统中所有权限（分页）")
    public R<Page<PermissionInfoResponse>> getAllPermissions(
            @Parameter(description = "页码，从0开始")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "20") int size) {
        log.info("获取权限列表: 页码={}, 每页数量={}", page, size);

        try {
            // 1. 创建分页参数（按创建时间降序）
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

            // 2. 调用服务层获取权限列表
            R<Page<PermissionDTO>> result = permissionService.getAllPermissions(pageable);

            if (!R.isSuccess(result)) {
                return R.fail(result.getMsg());
            }

            // 3. 转换为Response对象
            Page<PermissionDTO> permissionPage = result.getData();
            Page<PermissionInfoResponse> responsePage = permissionPage.map(dto ->
                    PermissionInfoResponse.builder()
                            .id(dto.getId())
                            .name(dto.getName())
                            .description(dto.getDescription())
                            .build()
            );

            return R.ok(responsePage);
        } catch (Exception e) {
            log.error("获取权限列表失败", e);
            return R.fail("获取权限列表失败");
        }
    }

    /**
     * 根据ID获取权限详情
     * 需要权限：system:permission:list
     */
    @GetMapping("/{permissionId}")
    @Transactional(readOnly = true)
   // @PreAuthorize("hasAuthority('system:permission:list')")
    @Operation(summary = "获取权限详情", description = "根据ID获取权限详细信息（包含拥有该权限的角色列表）")
    public R<PermissionDetailResponse> getPermissionById(
            @Parameter(description = "权限ID", required = true)
            @PathVariable Long permissionId) {
        log.info("获取权限详情: 权限ID={}", permissionId);

        try {
            // 1. 查询权限基本信息
            Permission permission = permissionService.findById(permissionId);
            if (permission == null) {
                return R.fail("权限不存在");
            }

            // 2. 查询拥有该权限的角色列表
            List<RolePermission> rolePermissions = rolePermissionRepository.findByPermissionId(permissionId);
            List<Role> roles = rolePermissions.stream()
                    .map(RolePermission::getRole)
                    .collect(Collectors.toList());

            List<RoleDTO> roleDTOs = roles.stream()
                    .map(r -> RoleDTO.builder()
                            .id(r.getId())
                            .name(r.getName())
                            .description(r.getDescription())
                            .createdAt(r.getCreatedAt())
                            .updatedAt(r.getUpdatedAt())
                            .build())
                    .collect(Collectors.toList());

            // 3. 构建详情响应
            PermissionDetailResponse response = PermissionDetailResponse.builder()
                    .id(permission.getId())
                    .name(permission.getName())
                    .description(permission.getDescription())
                    .roleCount((long) roles.size())
                    .roles(roleDTOs)
                    .build();

            return R.ok(response);
        } catch (Exception e) {
            log.error("获取权限详情失败: permissionId={}", permissionId, e);
            return R.fail("获取权限详情失败");
        }
    }

    /**
     * 创建新权限
     * 需要权限：system:permission:assign（权限分配权限，包含权限创建）
     */
    @PostMapping
    //@PreAuthorize("hasAuthority('system:permission:assign')")
    @Operation(summary = "创建权限", description = "创建新的系统权限")
    public R<PermissionInfoResponse> createPermission(
            @Valid @RequestBody CreatePermissionBody request) {
        log.info("创建权限: 权限名={}, 描述={}", request.getName(), request.getDescription());

        try {
            // 1. 构建PermissionDTO
            PermissionDTO permissionDTO = PermissionDTO.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .build();

            // 2. 创建权限
            R<PermissionDTO> createResult = permissionService.createPermission(permissionDTO);
            if (!R.isSuccess(createResult)) {
                return R.fail(createResult.getMsg());
            }

            PermissionDTO createdPermission = createResult.getData();

            // 3. 构建响应
            PermissionInfoResponse response = PermissionInfoResponse.builder()
                    .id(createdPermission.getId())
                    .name(createdPermission.getName())
                    .description(createdPermission.getDescription())
                    .build();

            return R.ok(response, "权限创建成功");
        } catch (Exception e) {
            log.error("创建权限失败: request={}", request, e);
            return R.fail("创建权限失败");
        }
    }

    /**
     * 更新权限信息
     * 需要权限：system:permission:assign
     */
    @PutMapping("/{permissionId}")
   // @PreAuthorize("hasAuthority('system:permission:assign')")
    @Operation(summary = "更新权限", description = "更新权限基本信息（名称、描述）")
    public R<PermissionInfoResponse> updatePermission(
            @Parameter(description = "权限ID", required = true)
            @PathVariable Long permissionId,
            @Valid @RequestBody UpdatePermissionBody request) {
        log.info("更新权限: 权限ID={}, 权限名={}", permissionId, request.getName());

        try {
            // 1. 构建PermissionDTO
            PermissionDTO permissionDTO = PermissionDTO.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .build();

            // 2. 调用服务层更新
            R<PermissionDTO> updateResult = permissionService.updatePermission(permissionId, permissionDTO);
            if (!R.isSuccess(updateResult)) {
                return R.fail(updateResult.getMsg());
            }

            // 3. 构建响应
            PermissionDTO updatedPermission = updateResult.getData();
            PermissionInfoResponse response = PermissionInfoResponse.builder()
                    .id(updatedPermission.getId())
                    .name(updatedPermission.getName())
                    .description(updatedPermission.getDescription())
                    .build();

            return R.ok(response, "权限更新成功");
        } catch (Exception e) {
            log.error("更新权限失败: permissionId={}, request={}", permissionId, request, e);
            return R.fail("更新权限失败");
        }
    }

    /**
     * 删除权限
     * 需要权限：system:permission:assign
     */
    @DeleteMapping("/{permissionId}")
   // @PreAuthorize("hasAuthority('system:permission:assign')")
    @Operation(summary = "删除权限", description = "删除指定权限（需检查是否有角色使用）")
    public R<Void> deletePermission(
            @Parameter(description = "权限ID", required = true)
            @PathVariable Long permissionId) {
        log.info("删除权限: 权限ID={}", permissionId);

        // 直接调用服务层，服务层已包含所有校验逻辑
        return permissionService.deletePermission(permissionId);
    }

    /**
     * 检查用户是否拥有指定权限
     * 需要权限：system:permission:list
     */
    @PostMapping("/check")
    //@PreAuthorize("hasAuthority('system:permission:list')")
    @Operation(summary = "检查用户权限", description = "检查指定用户是否拥有指定权限")
    public R<Boolean> checkPermission(
            @Valid @RequestBody PermissionCheckBody request) {
        log.info("检查用户权限: 用户ID={}, 权限={}", request.getUserId(), request.getPermission());

        try {
            return permissionService.hasPermission(request.getUserId(), request.getPermission());
        } catch (Exception e) {
            log.error("检查用户权限失败: request={}", request, e);
            return R.fail("权限检查失败");
        }
    }

    /**
     * 批量检查用户权限
     * 需要权限：system:permission:list
     */
    @PostMapping("/check-batch")
   // @PreAuthorize("hasAuthority('system:permission:list')")
    @Operation(summary = "批量检查用户权限", description = "批量检查用户是否拥有指定权限列表中的任一权限")
    public R<Map<String, Boolean>> checkPermissionBatch(
            @Valid @RequestBody BatchPermissionCheckBody request) {
        log.info("批量检查用户权限: 用户ID={}, 权限数量={}",
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
     * 获取用户的所有权限
     * 需要权限：system:permission:list
     */
    @GetMapping("/user/{userId}")
    //@PreAuthorize("hasAuthority('system:permission:list')")
    @Operation(summary = "获取用户权限", description = "获取指定用户的所有权限")
    public R<Set<String>> getUserPermissions(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        log.info("获取用户权限: 用户ID={}", userId);

        try {
            return permissionService.getUserPermissions(userId);
        } catch (Exception e) {
            log.error("获取用户权限失败: userId={}", userId, e);
            return R.fail("获取用户权限失败");
        }
    }

    /**
     * 获取拥有指定权限的角色列表
     * 需要权限：system:permission:list
     */
    @GetMapping("/{permissionId}/roles")
    @Transactional(readOnly = true)
    //@PreAuthorize("hasAuthority('system:permission:list')")
    @Operation(summary = "获取权限的角色列表", description = "获取拥有指定权限的所有角色")
    public R<List<RoleInfoResponse>> getPermissionRoles(
            @Parameter(description = "权限ID", required = true)
            @PathVariable Long permissionId) {
        log.info("获取权限的角色列表: 权限ID={}", permissionId);

        try {
            // 1. 校验权限是否存在
            Permission permission = permissionService.findById(permissionId);
            if (permission == null) {
                return R.fail("权限不存在");
            }

            // 2. 查询拥有该权限的所有角色
            List<RolePermission> rolePermissions = rolePermissionRepository.findByPermissionId(permissionId);
            List<Role> roles = rolePermissions.stream()
                    .map(RolePermission::getRole)
                    .collect(Collectors.toList());

            // 3. 转换为Response对象
            List<RoleInfoResponse> responses = roles.stream()
                    .map(r -> {
                        long userCount = userRoleRepository.findByRoleId(r.getId()).size();
                        return RoleInfoResponse.builder()
                                .id(r.getId())
                                .name(r.getName())
                                .description(r.getDescription())
                                .userCount(userCount)
                                .build();
                    })
                    .collect(Collectors.toList());

            return R.ok(responses);
        } catch (Exception e) {
            log.error("获取权限的角色列表失败: permissionId={}", permissionId, e);
            return R.fail("获取权限的角色列表失败");
        }
    }
}