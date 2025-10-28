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
import java.util.Map;

/**
 * 用户管理控制器
 * 提供用户信息管理、用户查询、权限查看等功能
 * 
 * 职责说明：
 * - 本控制器专注于用户信息的增删改查
 * - 认证相关功能（注册、登录、token管理）由 AuthController 负责
 * - 角色和权限的管理由 RoleController 和 PermissionController 负责
 * - 本控制器同时提供内部接口供其他微服务调用
 *
 * @author ErgouTree
 * @version 3.0
 * @rewrite Tokito
 */
@RestController
@RequestMapping("/zhiyan/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "用户管理", description = "用户信息管理相关接口")
public class UserController {

    private final UserService userService;

    // ==================== 用户信息查询接口 ====================

    /**
     * 获取当前登录用户信息
     * 路径: GET /api/users/me
     * 权限: 所有已登录用户
     */
    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的基本信息")
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
     * 根据ID获取用户详细信息（包含角色和权限）
     * 路径: GET /api/users/{userId}
     * 角色: DEVELOPER（只有开发者可以查看用户详情）
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('DEVELOPER')")
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
     * 路径: GET /api/users
     * 角色: DEVELOPER（只有开发者可以查看用户列表）
     */
    @GetMapping
    @PreAuthorize("hasRole('DEVELOPER')")
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
     * 搜索用户（用于项目成员邀请等场景）
     * 路径: GET /api/users/search
     * 权限: 所有已登录用户
     */
    @GetMapping("/search")
    @Operation(summary = "搜索用户", description = "根据关键词搜索用户（用于成员邀请等场景）")
    public R<Page<UserInfoResponse>> searchUsers(
            @Parameter(description = "搜索关键词", required = true)
            @RequestParam String keyword,
            @Parameter(description = "页码，从0开始")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "10") int size) {
        log.info("搜索用户: 关键词={}, 页码={}, 每页数量={}", keyword, page, size);

        try {
            Pageable pageable = PageRequest.of(page, size);
            R<Page<UserDTO>> result = userService.searchUsers(keyword, pageable);
            
            if (!R.isSuccess(result)) {
                return R.fail(result.getMsg());
            }

            Page<UserDTO> userPage = result.getData();
            Page<UserInfoResponse> responsePage = userPage.map(this::convertToUserInfoResponse);

            return R.ok(responsePage);
        } catch (Exception e) {
            log.error("搜索用户失败: keyword={}", keyword, e);
            return R.fail("搜索用户失败");
        }
    }

    // ==================== 用户信息修改接口 ====================

    /**
     * 更新当前用户资料
     * 路径: PUT /api/users/me
     * 权限: 所有已登录用户（只能更新自己的资料）
     */
    @PutMapping("/me")
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

    // ==================== 用户状态管理接口（管理员功能） ====================

    /**
     * 锁定用户
     * 路径: POST /api/users/{userId}/lock
     * 角色: DEVELOPER（只有开发者可以锁定用户）
     */
    @PostMapping("/{userId}/lock")
    @PreAuthorize("hasRole('DEVELOPER')")
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
     * 路径: POST /api/users/{userId}/unlock
     * 角色: DEVELOPER（只有开发者可以解锁用户）
     */
    @PostMapping("/{userId}/unlock")
    @PreAuthorize("hasRole('DEVELOPER')")
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
     * 路径: DELETE /api/users/{userId}
     * 角色: DEVELOPER（只有开发者可以删除用户）
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('DEVELOPER')")
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
     * 批量更新用户状态（管理员功能）
     * 路径: POST /api/users/batch-update-status
     * 角色: DEVELOPER（只有开发者可以批量更新用户状态）
     */
    @PostMapping("/batch-update-status")
    @PreAuthorize("hasRole('DEVELOPER')")
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

    // ==================== 角色权限查询接口 ====================

    /**
     * 获取用户的角色列表
     * 路径: GET /api/users/{userId}/roles
     * 角色: DEVELOPER（只有开发者可以查看用户角色）
     */
    @GetMapping("/{userId}/roles")
    @PreAuthorize("hasRole('DEVELOPER')")
    @Operation(summary = "获取用户角色", description = "获取指定用户的角色列表")
    public R<List<String>> getUserRoles(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        log.info("获取用户角色: 用户ID={}", userId);

        try {
            return userService.getUserRoles(userId);
        } catch (Exception e) {
            log.error("获取用户角色失败: userId={}", userId, e);
            return R.fail("获取用户角色失败");
        }
    }

    /**
     * 获取用户的权限列表
     * 路径: GET /api/users/{userId}/permissions
     * 角色: DEVELOPER（只有开发者可以查看用户权限）
     */
    @GetMapping("/{userId}/permissions")
    @PreAuthorize("hasRole('DEVELOPER')")
    @Operation(summary = "获取用户权限", description = "获取指定用户的权限列表")
    public R<List<String>> getUserPermissions(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        log.info("获取用户权限: 用户ID={}", userId);

        try {
            return userService.getUserPermissions(userId);
        } catch (Exception e) {
            log.error("获取用户权限失败: userId={}", userId, e);
            return R.fail("获取用户权限失败");
        }
    }

    // ==================== 内部接口（服务间调用） ====================

    /**
     * 根据邮箱查询用户信息（服务间调用接口）
     * 路径: GET /api/users/email
     * 用于其他微服务通过Feign调用查询用户
     * 无需权限校验（内部调用）
     */
    @GetMapping("/email")
    @Operation(summary = "根据邮箱查询用户", description = "根据邮箱查询用户基本信息（服务间调用）")
    public R<UserDTO> getUserByEmail(
            @Parameter(description = "用户邮箱", required = true)
            @RequestParam("email") String email) {
        log.info("根据邮箱查询用户: email={}", email);

        try {
            return userService.getUserByEmail(email);
        } catch (Exception e) {
            log.error("根据邮箱查询用户失败: email={}", email, e);
            return R.fail("查询用户失败");
        }
    }

    /**
     * 根据姓名查询用户信息（服务间调用接口）
     * 路径: GET /api/users/name
     * 用于其他微服务通过Feign调用查询用户
     * 无需权限校验（内部调用）
     */
    @GetMapping("/name")
    @Operation(summary = "根据姓名查询用户", description = "根据姓名查询用户基本信息（服务间调用）")
    public R<UserDTO> getUserByName(
            @Parameter(description = "用户姓名", required = true)
            @RequestParam("name") String name) {
        log.info("根据姓名查询用户: name={}", name);

        try {
            return userService.getUserByName(name);
        } catch (Exception e) {
            log.error("根据姓名查询用户失败: name={}", name, e);
            return R.fail("查询用户失败");
        }
    }

    /**
     * 批量根据ID查询用户信息（服务间调用接口）
     * 路径: POST /api/users/batch-query
     * 用于其他微服务批量查询成员信息
     * 无需权限校验（内部调用）
     */
    @PostMapping("/batch-query")
    @Operation(summary = "批量查询用户", description = "根据ID列表批量查询用户信息（服务间调用）")
    public R<List<UserDTO>> getUsersByIds(
            @Parameter(description = "用户ID列表", required = true)
            @RequestBody List<Long> userIds) {
        log.info("批量查询用户: 数量={}", userIds.size());

        try {
            return userService.getUsersByIds(userIds);
        } catch (Exception e) {
            log.error("批量查询用户失败", e);
            return R.fail("批量查询用户失败");
        }
    }

    /**
     * 检查用户是否拥有指定权限（内部接口/API网关调用）
     * 路径: POST /api/users/{userId}/has-permission
     * 用于权限校验流程
     * 无需权限校验（内部调用）
     */
    @PostMapping("/{userId}/has-permission")
    @Operation(summary = "检查用户权限", description = "检查用户是否拥有指定权限（内部接口）")
    public R<Boolean> hasPermission(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "权限标识符", required = true)
            @RequestParam String permission) {
        log.debug("检查用户权限: userId={}, permission={}", userId, permission);

        try {
            return userService.hasPermission(userId, permission);
        } catch (Exception e) {
            log.error("检查用户权限失败: userId={}, permission={}", userId, permission, e);
            return R.fail("权限检查失败");
        }
    }

    /**
     * 批量检查用户权限（内部接口）
     * 路径: POST /api/users/{userId}/has-permissions
     * 用于一次性校验多个权限
     * 无需权限校验（内部调用）
     */
    @PostMapping("/{userId}/has-permissions")
    @Operation(summary = "批量检查用户权限", description = "批量检查用户是否拥有多个权限（内部接口）")
    public R<Map<String, Boolean>> hasPermissions(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "权限标识符列表", required = true)
            @RequestBody List<String> permissions) {
        log.debug("批量检查用户权限: userId={}, permissions数量={}", userId, permissions.size());

        try {
            return userService.hasPermissions(userId, permissions);
        } catch (Exception e) {
            log.error("批量检查用户权限失败: userId={}", userId, e);
            return R.fail("批量权限检查失败");
        }
    }

    /**
     * 检查用户是否拥有指定角色（内部接口）
     * 路径: POST /api/users/{userId}/has-role
     * 用于角色校验
     * 无需权限校验（内部调用）
     */
    @PostMapping("/{userId}/has-role")
    @Operation(summary = "检查用户角色", description = "检查用户是否拥有指定角色（内部接口）")
    public R<Boolean> hasRole(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "角色名称", required = true)
            @RequestParam String roleName) {
        log.debug("检查用户角色: userId={}, roleName={}", userId, roleName);

        try {
            return userService.hasRole(userId, roleName);
        } catch (Exception e) {
            log.error("检查用户角色失败: userId={}, roleName={}", userId, roleName, e);
            return R.fail("角色检查失败");
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 将UserDTO转换为UserInfoResponse（不包含角色权限）
     */
    private UserInfoResponse convertToUserInfoResponse(UserDTO userDTO) {
        return UserInfoResponse.builder()
                .id(userDTO.getId())
                .email(userDTO.getEmail())
                .name(userDTO.getName())
                .avatarUrl(extractAvatarUrl(userDTO.getAvatarUrl()))
                .title(userDTO.getTitle())
                .institution(userDTO.getInstitution())
                .status(userDTO.getIsLocked() ? "LOCKED" : "ACTIVE")
                .build();
    }

    /**
     * 提取头像URL
     * 处理 avatarUrl 可能是 JSON 字符串或直接 URL 的情况
     */
    private String extractAvatarUrl(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            return null;
        }
        
        // 如果是 JSON 格式，解析出实际的 URL
        if (avatarUrl.startsWith("{")) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(avatarUrl);
                // 优先返回 minio_url，其次返回 cdn_url
                if (node.has("minio_url") && !node.get("minio_url").asText().isEmpty()) {
                    return node.get("minio_url").asText();
                }
                if (node.has("minioUrl") && !node.get("minioUrl").asText().isEmpty()) {
                    return node.get("minioUrl").asText();
                }
                if (node.has("cdn_url") && !node.get("cdn_url").asText().isEmpty()) {
                    return node.get("cdn_url").asText();
                }
                if (node.has("cdnUrl") && !node.get("cdnUrl").asText().isEmpty()) {
                    return node.get("cdnUrl").asText();
                }
            } catch (Exception e) {
                log.warn("解析头像URL JSON失败: {}", avatarUrl, e);
            }
        }
        
        // 直接返回 URL（不是 JSON 格式或解析失败）
        return avatarUrl;
    }

    /**
     * 将UserDTO转换为UserInfoResponse（包含角色权限）
     */
    private UserInfoResponse convertToUserInfoResponseWithRoles(UserDTO userDTO) {
        UserInfoResponse response = convertToUserInfoResponse(userDTO);
        response.setRoles(userDTO.getRoles());
        response.setPermissions(userDTO.getPermissions());
        return response;
    }
}
