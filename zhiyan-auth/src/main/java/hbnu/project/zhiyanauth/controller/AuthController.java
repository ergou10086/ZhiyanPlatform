package hbnu.project.zhiyanauth.controller;

import com.alibaba.nacos.api.model.v2.Result;
import hbnu.project.zhiyanauth.model.dto.TokenDTO;
import hbnu.project.zhiyanauth.model.form.LoginBody;
import hbnu.project.zhiyanauth.model.form.RegisterBody;
import hbnu.project.zhiyanauth.model.form.ResetPasswordBody;
import hbnu.project.zhiyanauth.model.form.VerificationCodeBody;
import hbnu.project.zhiyanauth.model.response.TokenRefreshRespone;
import hbnu.project.zhiyanauth.model.response.TokenValidateResponse;
import hbnu.project.zhiyanauth.model.response.UserLoginResponse;
import hbnu.project.zhiyanauth.model.response.UserRegisterResponse;
import hbnu.project.zhiyanauth.service.AuthService;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyanauth.model.form.PermissionCheckBody;
import hbnu.project.zhiyanauth.model.form.BatchPermissionCheckBody;
import hbnu.project.zhiyanauth.model.response.PermissionCheckResponse;
import hbnu.project.zhiyanauth.model.response.BatchPermissionCheckResponse;
import hbnu.project.zhiyanauth.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户认证控制器
 * 负责用户注册、登录、密码重置等认证相关功能
 *
 * @author yxy
 */
@RestController
@RequestMapping("/zhiyan/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "用户认证", description = "用户注册、登录、验证等认证相关接口")
public class AuthController {

    private final AuthService authService;
    private final PermissionService permissionService;


    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "通过邮箱和验证码进行用户注册")
    public R<UserRegisterResponse> register(
            @Valid @RequestBody RegisterBody request) {
        log.info("用户注册请求: 邮箱={}, 姓名={}", request.getEmail(), request.getName());

        // 直接调用 auth 模块的服务
        return authService.register(request);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录获取访问令牌")
    public R<UserLoginResponse> login(
            @Valid @RequestBody LoginBody loginBody) {
        log.info("用户登录API请求: 邮箱={}", loginBody.getEmail());

        // 直接调用 auth 模块的服务
        return authService.login(loginBody);
    }

    /**
     * 验证验证码
     */
    @PostMapping("/verify-code")
    @Operation(summary = "验证验证码", description = "单独验证验证码是否正确")
    public R<Boolean> verifyCode(
            @RequestParam String email,
            @RequestParam String code,
            @RequestParam String type) {
        log.info("验证验证码API请求: 邮箱={}, 类型={}", email, type);

        return authService.verifyCode(email, code, type);
    }


    /**
     * 检查邮箱是否已注册
     */
    @GetMapping("/check-email")
    @Operation(summary = "检查邮箱", description = "检查邮箱是否已被注册")
    public R<Boolean> checkEmail(@RequestParam String email) {
        log.info("检查邮箱API请求: 邮箱={}", email);

        return authService.checkEmail(email);
    }



    /**
     //     * 刷新访问令牌
     // TODO: 实现令牌刷新逻辑
     // 1. 校验Refresh Token的有效性
     // 2. 检查Token是否在黑名单中
     // 3. 生成新的Access Token（可选择是否生成新的Refresh Token）
     // 4. 使旧的Refresh Token失效
     // 5. 返回新的令牌
     //     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌", description = "使用Refresh Token获取新的Access Token")
    public R<TokenDTO> refreshToken(@Valid @RequestBody TokenRefreshRespone request) {
        log.info("令牌刷新请求 - refreshToken: {}", request.getRefreshToken());
        TokenDTO tokenDTO = authService.refreshToken(request.getRefreshToken());
        return R.ok(tokenDTO);
    }

    /**
     * 验证令牌有效性
     */
    @GetMapping("/validate")
    @Operation(summary = "验证令牌", description = "验证访问令牌是否有效")
    public Result<TokenValidateResponse> validateToken(
            @RequestHeader("Authorization") String token) {
        log.info("令牌验证请求");

        // 直接调用服务层完成验证逻辑
        TokenValidateResponse response =authService.validateTokenWithDetails(token);

        return Result.success(response);
    }

    /**
     * 忘记密码 - 发送重置验证码
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "忘记密码", description = "发送密码重置验证码到邮箱")
    public R<Void> forgotPassword(@Valid @RequestBody ResetPasswordBody request) {
        log.info("忘记密码请求: 邮箱={}", request.getEmail());

        // 调用 service
        return authService.forgotPassword(request.getEmail());
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    @Operation(summary = "重置密码", description = "通过验证码重置密码")
    public R<Void> resetPassword(@Valid @RequestBody ResetPasswordBody request) {
        log.info("重置密码请求: 邮箱={}", request.getEmail());

        return authService.resetPassword(request);
    }



    /**
     * 用户登出接口
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出，使令牌失效")
    public R<String> logout(@RequestHeader("Authorization") String tokenHeader) {
        authService.logout(tokenHeader);
        return R.ok(  null,"登出成功"); // 返回 R<Void>
    }


    /**
     * 发送验证码
     */
    @PostMapping("/send-verfcode")
    @Operation(summary = "发送验证码", description = "向指定邮箱发送验证码，支持注册、重置密码等场景")
    public R<Void> sendVerificationCode(
            @Valid @RequestBody VerificationCodeBody verificationCodeBody) {
        log.info("发送验证码请求: 邮箱={}, 类型={}", verificationCodeBody.getEmail(), verificationCodeBody.getType());

        // 直接调用 auth 模块的服务
        return authService.sendVerificationCode(verificationCodeBody);
    }




    /**
     * 权限校验接口（供其他微服务调用）
     */
    @PostMapping("/check-permission")
    @Operation(summary = "权限校验", description = "检查用户是否拥有指定权限（内部接口）")
    public R<PermissionCheckResponse> checkPermission(
            @Valid @RequestBody PermissionCheckBody request) {
        log.info("权限校验请求: 用户ID={}, 权限={}", request.getUserId(), request.getPermission());

        try {
            // 1. 调用PermissionService检查权限
            R<Boolean> result = permissionService.hasPermission(request.getUserId(), request.getPermission());

            if (!R.isSuccess(result)) {
                return R.fail(result.getMsg());
            }

            // 2. 构建响应
            PermissionCheckResponse response = PermissionCheckResponse.builder()
                    .userId(request.getUserId())
                    .permission(request.getPermission())
                    .hasPermission(result.getData())
                    .message(result.getData() ? "拥有该权限" : "无该权限")
                    .build();

            return R.ok(response);
        } catch (Exception e) {
            log.error("权限校验失败: userId={}, permission={}",
                    request.getUserId(), request.getPermission(), e);
            return R.fail("权限校验失败");
        }
    }


    /**
     * 批量权限校验
     */
    @PostMapping("/check-permissions")
    @Operation(summary = "批量权限校验", description = "批量检查用户权限（内部接口）")
    public R<BatchPermissionCheckResponse> checkPermissions(
            @Valid @RequestBody BatchPermissionCheckBody request) {
        log.info("批量权限校验请求: 用户ID={}, 权限数量={}",
                request.getUserId(), request.getPermissions().size());

        try {
            // 1. 获取用户所有权限
            R<Set<String>> userPermissionsResult = permissionService.getUserPermissions(request.getUserId());

            if (!R.isSuccess(userPermissionsResult)) {
                return R.fail(userPermissionsResult.getMsg());
            }

            Set<String> userPermissions = userPermissionsResult.getData();

            // 2. 逐一检查请求的权限列表
            Map<String, Boolean> permissionResults = request.getPermissions().stream()
                    .collect(Collectors.toMap(
                            permission -> permission,
                            userPermissions::contains
                    ));

            // 3. 构建响应
            BatchPermissionCheckResponse response = BatchPermissionCheckResponse.builder()
                    .userId(request.getUserId())
                    .permissionResults(permissionResults)
                    .message("批量权限检查完成")
                    .build();

            return R.ok(response);
        } catch (Exception e) {
            log.error("批量权限校验失败: userId={}, permissions={}",
                    request.getUserId(), request.getPermissions(), e);
            return R.fail("批量权限校验失败");
        }
    }
}

