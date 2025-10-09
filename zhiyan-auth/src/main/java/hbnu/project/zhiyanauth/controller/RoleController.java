package hbnu.project.zhiyanauth.controller;

import hbnu.project.zhiyanauth.model.dto.PermissionDTO;
import hbnu.project.zhiyanauth.model.dto.RoleDTO;
import hbnu.project.zhiyanauth.model.entity.Permission;
import hbnu.project.zhiyanauth.model.entity.Role;
import hbnu.project.zhiyanauth.model.entity.User;
import hbnu.project.zhiyanauth.model.entity.UserRole;
import hbnu.project.zhiyanauth.model.form.*;
import hbnu.project.zhiyanauth.repository.RolePermissionRepository;
import hbnu.project.zhiyanauth.repository.UserRoleRepository;
import hbnu.project.zhiyanauth.response.PermissionInfoResponse;
import hbnu.project.zhiyanauth.response.RoleDetailResponse;
import hbnu.project.zhiyanauth.response.RoleInfoResponse;
import hbnu.project.zhiyanauth.response.UserInfoResponse;
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
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色管理控制器
 * 角色CRUD，权限分配，用户角色关联管理，关系查询等角色管理功能
 *
 * @author Tokito
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "角色权限管理", description = "角色和权限管理相关接口")
public class RoleController {

    private final RoleService roleService;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    /**
     * 获取所有角色列表
     */
    @GetMapping
    @Operation(summary = "获取角色列表", description = "获取系统中所有角色（分页）")
    public R<Page<RoleInfoResponse>> getAllRoles(
            @Parameter(description = "页码，从0开始")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "20") int size) {
        log.info("获取角色列表: 页码={}, 每页数量={}", page, size);

        try {
            // 1. 创建分页参数（按创建时间降序）
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

            // 2. 调用服务层获取角色列表
            R<Page<RoleDTO>> result = roleService.getAllRoles(pageable);

            if (!R.isSuccess(result)) {
                return R.fail(result.getMsg());
            }

            // 3. 转换为Response对象
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
     */
    @GetMapping("/{roleId}")
    @Operation(summary = "获取角色详情", description = "根据ID获取角色详细信息（包含权限列表）")
    public R<RoleDetailResponse> getRoleById(
            @Parameter(description = "角色ID", required = true)
            @PathVariable Long roleId) {
        log.info("获取角色详情: 角色ID={}", roleId);

        try {
            // 1. 查询角色基本信息
            Role role = roleService.findById(roleId);
            if (role == null) {
                return R.fail("角色不存在");
            }

            // 2. 查询角色关联的权限列表
            List<Permission> permissions = rolePermissionRepository.findByRoleId(roleId).stream()
                    .map(rp -> rp.getPermission())
                    .collect(Collectors.toList());

            List<PermissionDTO> permissionDTOs = permissions.stream()
                    .map(p -> PermissionDTO.builder()
                            .id(p.getId())
                            .name(p.getName())
                            .description(p.getDescription())
                            .createdAt(p.getCreatedAt())
                            .updatedAt(p.getUpdatedAt())
                            .build())
                    .collect(Collectors.toList());

            // 3. 查询拥有该角色的用户数量
            long userCount = userRoleRepository.findByRoleId(roleId).size();

            // 4. 构建详情响应
            RoleDetailResponse response = RoleDetailResponse.builder()
                    .id(role.getId())
                    .name(role.getName())
                    .description(role.getDescription())
                    .roleType(role.getRoleType())
                    .projectId(role.getProjectId())
                    .isSystemDefault(role.getIsSystemDefault())
                    .userCount(userCount)
                    .permissions(permissionDTOs)
                    .createdAt(role.getCreatedAt())
                    .updatedAt(role.getUpdatedAt())
                    .createdBy(role.getCreatedBy())
                    .updatedBy(role.getUpdatedBy())
                    .build();

            return R.ok(response);
        } catch (Exception e) {
            log.error("获取角色详情失败: roleId={}", roleId, e);
            return R.fail("获取角色详情失败");
        }
    }


    /**
     * 创建新角色
     */
    @PostMapping
    @Operation(summary = "创建角色", description = "创建新的角色（可同时分配权限）")
    public R<RoleInfoResponse> createRole(
            @Valid @RequestBody CreateRoleBody request) {
        log.info("创建角色: 角色名={}, 描述={}", request.getName(), request.getDescription());

        try {
            // 1. 构建RoleDTO
            RoleDTO roleDTO = RoleDTO.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .build();

            // 2. 创建角色
            R<RoleDTO> createResult = roleService.createRole(roleDTO);
            if (!R.isSuccess(createResult)) {
                return R.fail(createResult.getMsg());
            }

            RoleDTO createdRole = createResult.getData();

            // 3. 如果指定了权限，则分配权限
            if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
                R<Void> assignResult = roleService.assignPermissionsToRole(
                        createdRole.getId(),
                        request.getPermissionIds()
                );
                if (!R.isSuccess(assignResult)) {
                    log.warn("角色创建成功但权限分配失败: roleId={}", createdRole.getId());
                }
            }

            // 4. 查询用户数量（新创建的角色用户数为0）
            RoleInfoResponse response = RoleInfoResponse.builder()
                    .id(createdRole.getId())
                    .name(createdRole.getName())
                    .description(createdRole.getDescription())
                    .userCount(0L)
                    .createdAt(createdRole.getCreatedAt())
                    .updatedAt(createdRole.getUpdatedAt())
                    .createdBy(createdRole.getCreatedBy())
                    .updatedBy(createdRole.getUpdatedBy())
                    .build();

            return R.ok(response, "角色创建成功");
        } catch (Exception e) {
            log.error("创建角色失败: request={}", request, e);
            return R.fail("创建角色失败");
        }
    }


    /**
     * 更新角色信息
     */
    @PutMapping("/{roleId}")
    @Operation(summary = "更新角色", description = "更新角色基本信息（名称、描述）")
    public R<RoleInfoResponse> updateRole(
            @Parameter(description = "角色ID", required = true)
            @PathVariable Long roleId,
            @Valid @RequestBody UpdateRoleBody request) {
        log.info("更新角色: 角色ID={}, 角色名={}", roleId, request.getName());

        try {
            // 1. 构建RoleDTO
            RoleDTO roleDTO = RoleDTO.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .build();

            // 2. 调用服务层更新
            R<RoleDTO> updateResult = roleService.updateRole(roleId, roleDTO);
            if (!R.isSuccess(updateResult)) {
                return R.fail(updateResult.getMsg());
            }

            // 3. 查询用户数量
            long userCount = userRoleRepository.findByRoleId(roleId).size();

            // 4. 构建响应
            RoleDTO updatedRole = updateResult.getData();
            RoleInfoResponse response = RoleInfoResponse.builder()
                    .id(updatedRole.getId())
                    .name(updatedRole.getName())
                    .description(updatedRole.getDescription())
                    .userCount(userCount)
                    .createdAt(updatedRole.getCreatedAt())
                    .updatedAt(updatedRole.getUpdatedAt())
                    .createdBy(updatedRole.getCreatedBy())
                    .updatedBy(updatedRole.getUpdatedBy())
                    .build();

            return R.ok(response, "角色更新成功");
        } catch (Exception e) {
            log.error("更新角色失败: roleId={}, request={}", roleId, request, e);
            return R.fail("更新角色失败");
        }
    }


    /**
     * 删除角色
     */
    @DeleteMapping("/{roleId}")
    @Operation(summary = "删除角色", description = "删除指定角色（需检查是否有用户使用）")
    public R<Void> deleteRole(
            @Parameter(description = "角色ID", required = true)
            @PathVariable Long roleId) {
        log.info("删除角色: 角色ID={}", roleId);

        // 直接调用服务层，服务层已包含所有校验逻辑
        return roleService.deleteRole(roleId);
    }


    /**
     * 为角色分配权限
     */
    @PostMapping("/{roleId}/permissions")
    @Operation(summary = "为角色分配权限", description = "为指定角色分配权限（增量分配）")
    public R<Void> assignPermissions(
            @Parameter(description = "角色ID", required = true)
            @PathVariable Long roleId,
            @Valid @RequestBody AssignPermissionsBody request) {
        log.info("为角色分配权限: 角色ID={}, 权限数量={}", roleId, request.getPermissionIds().size());

        // 直接调用服务层
        return roleService.assignPermissionsToRole(roleId, request.getPermissionIds());
    }


    /**
     * 移除角色的权限
     */
    @DeleteMapping("/{roleId}/permissions")
    @Operation(summary = "移除角色权限", description = "移除角色的指定权限")
    public R<Void> removePermissions(
            @Parameter(description = "角色ID", required = true)
            @PathVariable Long roleId,
            @Valid @RequestBody RemovePermissionsBody request) {
        log.info("移除角色权限: 角色ID={}, 权限数量={}", roleId, request.getPermissionIds().size());

        // 直接调用服务层
        return roleService.removePermissionsFromRole(roleId, request.getPermissionIds());
    }


    /**
     * 获取角色的权限列表
     */
    @GetMapping("/{roleId}/permissions")
    @Operation(summary = "获取角色权限", description = "获取指定角色的所有权限")
    public R<List<PermissionInfoResponse>> getRolePermissions(
            @Parameter(description = "角色ID", required = true)
            @PathVariable Long roleId) {
        log.info("获取角色权限: 角色ID={}", roleId);

        try {
            // 1. 校验角色是否存在
            Role role = roleService.findById(roleId);
            if (role == null) {
                return R.fail("角色不存在");
            }

            // 2. 查询角色关联的所有权限
            List<Permission> permissions = rolePermissionRepository.findByRoleId(roleId).stream()
                    .map(rp -> rp.getPermission())
                    .collect(Collectors.toList());

            // 3. 转换为Response对象
            List<PermissionInfoResponse> responses = permissions.stream()
                    .map(p -> PermissionInfoResponse.builder()
                            .id(p.getId())
                            .name(p.getName())
                            .description(p.getDescription())
                            .createdAt(p.getCreatedAt())
                            .updatedAt(p.getUpdatedAt())
                            .build())
                    .collect(Collectors.toList());

            return R.ok(responses);
        } catch (Exception e) {
            log.error("获取角色权限失败: roleId={}", roleId, e);
            return R.fail("获取角色权限失败");
        }
    }


    /**
     * 获取拥有指定角色的用户列表
     */
    @GetMapping("/{roleId}/users")
    @Operation(summary = "获取角色用户", description = "获取拥有指定角色的用户列表（分页）")
    public R<List<UserInfoResponse>> getRoleUsers(
            @Parameter(description = "角色ID", required = true)
            @PathVariable Long roleId,
            @Parameter(description = "页码，从0开始")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "20") int size) {
        log.info("获取角色用户: 角色ID={}, 页码={}, 每页数量={}", roleId, page, size);

        try {
            // 1. 校验角色是否存在
            Role role = roleService.findById(roleId);
            if (role == null) {
                return R.fail("角色不存在");
            }

            // 2. 查询拥有该角色的用户
            List<User> users = userRoleRepository.findByRoleId(roleId).stream()
                    .map(UserRole::getUser)
                    .skip((long) page * size)
                    .limit(size)
                    .collect(Collectors.toList());

            // 3. 转换为Response对象
            List<UserInfoResponse> responses = users.stream()
                    .map(u -> UserInfoResponse.builder()
                            .id(u.getId())
                            .email(u.getEmail())
                            .name(u.getName())
                            .avatarUrl(u.getAvatarUrl())
                            .title(u.getTitle())
                            .institution(u.getInstitution())
                            .status(u.getStatus() != null ? u.getStatus().name() : null)
                            .build())
                    .collect(Collectors.toList());

            return R.ok(responses);
        } catch (Exception e) {
            log.error("获取角色用户失败: roleId={}", roleId, e);
            return R.fail("获取角色用户失败");
        }
    }


    /**
     * 为用户分配角色
     */
    @PostMapping("/assign-user-role")
    @Operation(summary = "为用户分配角色", description = "为指定用户分配角色")
    public R<Void> assignUserRole(
            @Valid @RequestBody AssignUserRoleBody request) {
        log.info("为用户分配角色: 用户ID={}, 角色ID={}", request.getUserId(), request.getRoleId());

        // 调用服务层，传入单个角色ID的列表
        return roleService.assignRolesToUser(request.getUserId(), Arrays.asList(request.getRoleId()));
    }


    /**
     * 移除用户角色
     */
    @DeleteMapping("/remove-user-role")
    @Operation(summary = "移除用户角色", description = "移除用户的指定角色")
    public R<Void> removeUserRole(
            @Valid @RequestBody RemoveUserRoleBody request) {
        log.info("移除用户角色: 用户ID={}, 角色ID={}", request.getUserId(), request.getRoleId());

        // 调用服务层，传入单个角色ID的列表
        return roleService.removeRolesFromUser(request.getUserId(), Arrays.asList(request.getRoleId()));
    }

    /**
     * 辅助方法：将RoleDTO转换为RoleInfoResponse
     */
    private RoleInfoResponse convertToRoleInfoResponse(RoleDTO roleDTO) {
        // 查询用户数量
        long userCount = userRoleRepository.findByRoleId(roleDTO.getId()).size();

        return RoleInfoResponse.builder()
                .id(roleDTO.getId())
                .name(roleDTO.getName())
                .description(roleDTO.getDescription())
                .userCount(userCount)
                .createdAt(roleDTO.getCreatedAt())
                .updatedAt(roleDTO.getUpdatedAt())
                .createdBy(roleDTO.getCreatedBy())
                .updatedBy(roleDTO.getUpdatedBy())
                .build();
    }
}