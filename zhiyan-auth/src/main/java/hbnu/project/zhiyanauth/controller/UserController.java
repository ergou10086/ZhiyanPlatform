package hbnu.project.zhiyanauth.controller;

import hbnu.project.zhiyanauth.model.dto.UserDTO;
import hbnu.project.zhiyanauth.model.form.UserProfileUpdateBody;
import hbnu.project.zhiyanauth.model.response.UserInfoResponse;
import hbnu.project.zhiyanauth.service.UserService;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
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

/**
 * 用户管理控制器
 * 用户信息管理，用户搜索，权限查看，用户管理功能等
 *
 * @author Tokito（这不是假的）
 */
@RestController
@RequestMapping("/auth/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "用户管理", description = "用户信息管理相关接口")
public class UserController {

    private final UserService userService;

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public R<UserInfoResponse> getCurrentUser() {
        log.info("获取当前用户信息");

        try {
            // 从 Security Context 中获取当前用户ID
            Long userId = SecurityUtils.getUserId();
            if (userId == null) {
                return R.fail("用户未登录");
            }

            // 调用服务层获取用户信息
            R<UserDTO> result = userService.getCurrentUser(userId);
            if (!R.isSuccess(result)) {
                return R.fail(result.getMsg());
            }

            // 转换为Response对象
            UserDTO userDTO = result.getData();
            UserInfoResponse response = convertToUserInfoResponse(userDTO);

            return R.ok(response);
        } catch (Exception e) {
            log.error("获取当前用户信息失败", e);
            return R.fail("获取用户信息失败");
        }
    }


    /**
     * 更新当前用户资料
     * 需要权限：profile:manage（所有用户都有）
     */
    @PutMapping("/me")
    // TODO: 临时注释权限检查，开发完成后需要恢复
    // @PreAuthorize("hasAuthority('profile:manage')")
    @Operation(summary = "更新个人资料", description = "更新当前登录用户的个人信息")
    public R<UserInfoResponse> updateProfile(
            @Valid @RequestBody UserProfileUpdateBody updateBody) {
        log.info("更新用户资料: {}", updateBody);

        try {
            // 获取当前用户ID
            Long userId = SecurityUtils.getUserId();
            if (userId == null) {
                return R.fail("用户未登录");
            }

            // 调用服务层更新资料
            R<UserDTO> result = userService.updateUserProfile(userId, updateBody);
            if (!R.isSuccess(result)) {
                return R.fail(result.getMsg());
            }

            // 转换为Response对象
            UserDTO userDTO = result.getData();
            UserInfoResponse response = convertToUserInfoResponse(userDTO);

            return R.ok(response, "资料更新成功");
        } catch (Exception e) {
            log.error("更新用户资料失败", e);
            return R.fail("更新资料失败");
        }
    }


    /**
     * 根据ID获取用户详细信息（包含角色和权限）
     * 需要权限：system:user:view
     */
    @GetMapping("/{userId}")
    // TODO: 临时注释权限检查，开发完成后需要恢复
    // @PreAuthorize("hasAuthority('system:user:view')")
    @Operation(summary = "获取用户详情", description = "根据ID获取用户详细信息（包含角色和权限）")
    public R<UserInfoResponse> getUserById(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        log.info("获取用户详情: 用户ID={}", userId);

        try {
            // 调用服务层获取用户详细信息
            R<UserDTO> result = userService.getUserWithRolesAndPermissions(userId);
            if (!R.isSuccess(result)) {
                return R.fail(result.getMsg());
            }

            // 转换为Response对象（包含角色和权限）
            UserDTO userDTO = result.getData();
            UserInfoResponse response = convertToUserInfoResponseWithRoles(userDTO);

            return R.ok(response);
        } catch (Exception e) {
            log.error("获取用户详情失败: userId={}", userId, e);
            return R.fail("获取用户详情失败");
        }
    }


    /**
     * 获取用户列表（管理员功能，分页）
     * 需要权限：system:user:list
     */
    @GetMapping
    // TODO: 临时注释权限检查，开发完成后需要恢复
    //@PreAuthorize("hasAuthority('system:user:list')")
    @Operation(summary = "获取用户列表", description = "分页查询用户列表（管理员功能）")
    public R<Page<UserInfoResponse>> getUserList(
            @Parameter(description = "页码，从0开始")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "搜索关键词（邮箱、姓名、机构）")
            @RequestParam(required = false) String keyword) {
        log.info("获取用户列表: 页码={}, 每页数量={}, 关键词={}", page, size, keyword);

        try {
            // 创建分页参数（按创建时间降序）
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

            // 调用服务层查询用户列表
            R<Page<UserDTO>> result = userService.getUserList(pageable, keyword);
            if (!R.isSuccess(result)) {
                return R.fail(result.getMsg());
            }

            // 转换为Response对象
            Page<UserDTO> userPage = result.getData();
            Page<UserInfoResponse> responsePage = userPage.map(this::convertToUserInfoResponse);

            return R.ok(responsePage);
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return R.fail("获取用户列表失败");
        }
    }


    /**
     * 锁定用户
     * 需要权限：system:user:lock
     */
    @PostMapping("/{userId}/lock")
    // TODO: 临时注释权限检查，开发完成后需要恢复
    //@PreAuthorize("hasAuthority('system:user:lock')")
    @Operation(summary = "锁定用户", description = "锁定指定用户，禁止其登录（管理员功能）")
    public R<Void> lockUser(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        log.info("锁定用户: 用户ID={}", userId);

        try {
            return userService.lockUser(userId, true);
        } catch (Exception e) {
            log.error("锁定用户失败: userId={}", userId, e);
            return R.fail("锁定用户失败");
        }
    }


    /**
     * 解锁用户
     * 需要权限：system:user:lock（锁定和解锁使用同一权限）
     */
    @PostMapping("/{userId}/unlock")
    // TODO: 临时注释权限检查，开发完成后需要恢复
    //@PreAuthorize("hasAuthority('system:user:lock')")
    @Operation(summary = "解锁用户", description = "解锁指定用户，允许其登录（管理员功能）")
    public R<Void> unlockUser(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        log.info("解锁用户: 用户ID={}", userId);

        try {
            return userService.lockUser(userId, false);
        } catch (Exception e) {
            log.error("解锁用户失败: userId={}", userId, e);
            return R.fail("解锁用户失败");
        }
    }


    /**
     * 软删除用户
     * 需要权限：system:user:delete
     */
    @DeleteMapping("/{userId}")
    // TODO: 临时注释权限检查，开发完成后需要恢复
    //@PreAuthorize("hasAuthority('system:user:delete')")
    @Operation(summary = "删除用户", description = "软删除指定用户（管理员功能）")
    public R<Void> deleteUser(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        log.info("删除用户: 用户ID={}", userId);

        try {
            // 不允许删除自己
            Long currentUserId = SecurityUtils.getUserId();
            if (userId.equals(currentUserId)) {
                return R.fail("不能删除自己");
            }

            return userService.deleteUser(userId);
        } catch (Exception e) {
            log.error("删除用户失败: userId={}", userId, e);
            return R.fail("删除用户失败");
        }
    }


    /**
     * 获取用户的角色列表
     * 需要权限：system:user:view
     */
    @GetMapping("/{userId}/roles")
    // TODO: 临时注释权限检查，开发完成后需要恢复
    //@PreAuthorize("hasAuthority('system:user:view')")
    @Operation(summary = "获取用户角色", description = "获取指定用户的角色列表")
    public R<List<String>> getUserRoles(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        log.info("获取用户角色: 用户ID={}", userId);

        try {
            // 调用服务层获取用户信息（包含角色）
            R<UserDTO> result = userService.getUserWithRolesAndPermissions(userId);
            if (!R.isSuccess(result)) {
                return R.fail(result.getMsg());
            }

            List<String> roles = result.getData().getRoles();
            return R.ok(roles);
        } catch (Exception e) {
            log.error("获取用户角色失败: userId={}", userId, e);
            return R.fail("获取用户角色失败");
        }
    }


    /**
     * 获取用户的权限列表
     * 需要权限：system:user:view
     */
    @GetMapping("/{userId}/permissions")
    // TODO: 临时注释权限检查，开发完成后需要恢复
    //@PreAuthorize("hasAuthority('system:user:view')")
    @Operation(summary = "获取用户权限", description = "获取指定用户的权限列表")
    public R<List<String>> getUserPermissions(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        log.info("获取用户权限: 用户ID={}", userId);

        try {
            // 调用服务层获取用户信息（包含权限）
            R<UserDTO> result = userService.getUserWithRolesAndPermissions(userId);
            if (!R.isSuccess(result)) {
                return R.fail(result.getMsg());
            }

            List<String> permissions = result.getData().getPermissions();
            return R.ok(permissions);
        } catch (Exception e) {
            log.error("获取用户权限失败: userId={}", userId, e);
            return R.fail("获取用户权限失败");
        }
    }




    /**
     * 批量更新用户状态（管理员功能）
     * 需要权限：system:user:lock
     */
    @PostMapping("/batch-update-status")
    // TODO: 临时注释权限检查，开发完成后需要恢复
    //@PreAuthorize("hasAuthority('system:user:lock')")
    @Operation(summary = "批量更新用户状态", description = "批量锁定或解锁用户（管理员功能）")
    public R<Void> batchUpdateStatus(
            @Parameter(description = "用户ID列表", required = true)
            @RequestParam List<Long> userIds,
            @Parameter(description = "是否锁定", required = true)
            @RequestParam boolean isLocked) {
        log.info("批量更新用户状态: 用户数量={}, 锁定={}", userIds.size(), isLocked);

        try {
            // 获取当前用户ID，防止锁定自己
            Long currentUserId = SecurityUtils.getUserId();

            int successCount = 0;
            int failCount = 0;

            for (Long userId : userIds) {
                // 跳过当前用户
                if (userId.equals(currentUserId)) {
                    log.warn("跳过当前用户: userId={}", userId);
                    failCount++;
                    continue;
                }

                R<Void> result = userService.lockUser(userId, isLocked);
                if (R.isSuccess(result)) {
                    successCount++;
                } else {
                    failCount++;
                }
            }

            String message = String.format("批量操作完成：成功 %d 个，失败 %d 个", successCount, failCount);
            log.info(message);

            return failCount == 0 ? R.ok(null, message) : R.ok(null, message);
        } catch (Exception e) {
            log.error("批量更新用户状态失败", e);
            return R.fail("批量更新失败");
        }
    }


    /**
     * 辅助方法：将UserDTO转换为UserInfoResponse（不包含角色权限）
     */
    private UserInfoResponse convertToUserInfoResponse(UserDTO userDTO) {
        return UserInfoResponse.builder()
                .id(userDTO.getId())
                .email(userDTO.getEmail())
                .name(userDTO.getName())
                .avatarUrl(userDTO.getAvatarUrl())
                .title(userDTO.getTitle())
                .institution(userDTO.getInstitution())
                .status(userDTO.getIsLocked() ? "LOCKED" : "ACTIVE")
                .build();
    }


    /**
     * 辅助方法：将UserDTO转换为UserInfoResponse（包含角色权限）
     */
    private UserInfoResponse convertToUserInfoResponseWithRoles(UserDTO userDTO) {
        UserInfoResponse response = convertToUserInfoResponse(userDTO);
        response.setRoles(userDTO.getRoles());
        response.setPermissions(userDTO.getPermissions());
        return response;
    }
}