package hbnu.project.zhiyanauth.controller;//package hbnu.project.zhiyanauthservice.controller;
//
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.bind.annotation.*;
//
///**
// * 用户认证控制器
// * 负责用户注册、登录、密码重置等认证相关功能
//// *
// * @author ErgouTree
// */
//@RestController
//@RequestMapping("/zhiyan/auth")
//@RequiredArgsConstructor
//@Slf4j
//@Tag(name = "用户认证", description = "用户注册、登录、验证等认证相关接口")
//public class AuthController {
//    // TODO: 注入AuthService
//    // TODO: 各种各样的注入
//    // private final AuthService authService;
//
//    /**
//     * 发送验证码
//     */
//    @PostMapping("/send-verfcode")
//    @Operation(summary = "发送验证码", description = "向指定邮箱发送验证码，支持注册、重置密码等场景")
//    public Result<Void> sendVerificationCode(
//            @Valid @RequestBody SendVerificationCodeRequest request) {
//        log.info("发送验证码请求: 邮箱={}, 类型={}", request.getEmail(), request.getType());
//
//        // TODO: 实现发送验证码逻辑
//        // 1. 校验邮箱格式和域名白名单
//        // 2. 生成随机验证码
//        // 3. 存储验证码到Redis（5分钟有效期）
//
//        return Result.success();
//    }
//
//
//    /**
//     * 用户注册
//     * 邮箱 + 验证码方式注册
//     */
//    @PostMapping("/register")
//    @Operation(summary = "用户注册", description = "通过邮箱和验证码进行用户注册")
//    public Result<UserRegisterResponse> register(
//            @Valid @RequestBody UserRegisterRequest request) {
//        log.info("用户注册请求: 邮箱={}, 姓名={}", request.getEmail(), request.getName());
//
//        // TODO: 实现用户注册逻辑
//        // 1. 校验验证码是否正确且未过期
//        // 2. 检查邮箱是否已存在
//        // 3. 加密密码（BCrypt）
//        // 4. 创建用户记录
//        // 5. 分配默认角色（普通用户）
//        // 6. 返回注册成功信息
//
//        return Result.success();
//    }
//
//
//    /**
//     * 用户登录
//     * 支持邮箱+密码 或 用户名+密码
//     */
//    @PostMapping("/login")
//    @Operation(summary = "用户登录", description = "用户登录获取访问令牌")
//    public Result<UserLoginResponse> login(
//            @Valid @RequestBody UserLoginRequest request) {
//        log.info("用户登录请求: 账号={}", request.getAccount());
//
//        // TODO: 实现用户登录逻辑
//        // 1. 根据账号（邮箱或用户名）查询用户
//        // 2. 校验密码
//        // 3. 检查用户状态（是否锁定、删除）
//        // 4. 生成JWT Access Token和Refresh Token
//        // 5. 存储Refresh Token到Redis
//        // 6. 返回用户信息和令牌
//
//        return Result.success();
//    }
//
//
//    /**
//     * 刷新访问令牌
//     */
//    @PostMapping("/refresh")
//    @Operation(summary = "刷新令牌", description = "使用refresh token获取新的access token")
//    public Result<TokenRefreshResponse> refreshToken(
//            @Valid @RequestBody TokenRefreshRequest request) {
//        log.info("令牌刷新请求");
//
//        // TODO: 实现令牌刷新逻辑
//        // 1. 校验Refresh Token的有效性
//        // 2. 检查Token是否在黑名单中
//        // 3. 生成新的Access Token（可选择是否生成新的Refresh Token）
//        // 4. 使旧的Refresh Token失效
//        // 5. 返回新的令牌
//
//        return Result.success();
//    }
//
//
//    /**
//     * 用户登出
//     */
//    @PostMapping("/logout")
//    @Operation(summary = "用户登出", description = "用户登出，使令牌失效")
//    public Result<Void> logout(
//            @Parameter(description = "访问令牌", required = true)
//            @RequestHeader("Authorization") String token) {
//        log.info("用户登出请求");
//
//        // TODO: 实现用户登出逻辑
//        // 1. 解析JWT Token获取用户信息
//        // 2. 将Token加入黑名单（Redis）
//        // 3. 删除对应的Refresh Token
//
//        return Result.success();
//    }
//
//
//    /**
//     * 忘记密码 - 发送重置验证码
//     */
//    @PostMapping("/forgot-password")
//    @Operation(summary = "忘记密码", description = "发送密码重置验证码到邮箱")
//    public Result<Void> forgotPassword(
//            @Valid @RequestBody ForgotPasswordRequest request) {
//        log.info("忘记密码请求: 邮箱={}", request.getEmail());
//
//        // TODO: 实现忘记密码逻辑
//        // 1. 检查邮箱是否存在
//        // 2. 生成密码重置验证码
//        // 3. 存储验证码到Redis
//        // 4. 发送重置密码邮件
//
//        return Result.success();
//    }
//
//
//
//    /**
//     * 重置密码
//     */
//    @PostMapping("/reset-password")
//    @Operation(summary = "重置密码", description = "通过验证码重置密码")
//    public Result<Void> resetPassword(
//            @Valid @RequestBody ResetPasswordRequest request) {
//        log.info("重置密码请求: 邮箱={}", request.getEmail());
//
//        // TODO: 实现重置密码逻辑
//        // 1. 校验验证码
//        // 2. 加密新密码
//        // 3. 更新用户密码
//        // 4. 使该用户所有现有Token失效
//
//        return Result.success();
//    }
//
//
//    /**
//     * 验证令牌有效性
//     */
//    @GetMapping("/validate")
//    @Operation(summary = "验证令牌", description = "验证访问令牌是否有效")
//    public Result<TokenValidateResponse> validateToken(
//            @Parameter(description = "访问令牌", required = true)
//            @RequestHeader("Authorization") String token) {
//        log.info("令牌验证请求");
//
//        // TODO: 实现令牌验证逻辑
//        // 1. 校验JWT Token签名和有效期
//        // 2. 检查Token是否在黑名单中
//        // 3. 返回Token中的用户信息
//
//        return Result.success();
//    }
//
//    /**
//     * 权限校验接口（供其他微服务调用）
//     */
//    @PostMapping("/check-permission")
//    @Operation(summary = "权限校验", description = "检查用户是否拥有指定权限（内部接口）")
//    public Result<PermissionCheckResponse> checkPermission(
//            @Valid @RequestBody PermissionCheckRequest request) {
//        log.info("权限校验请求: 用户ID={}, 权限={}", request.getUserId(), request.getPermission());
//
//        // TODO: 实现权限校验逻辑
//        // 1. 根据用户ID查询用户角色
//        // 2. 根据角色查询权限列表
//        // 3. 检查是否包含指定权限
//        // 4. 结果缓存到Redis中
//
//        return Result.success();
//    }
//
//
//    /**
//     * 批量权限校验
//     */
//    @PostMapping("/check-permissions")
//    @Operation(summary = "批量权限校验", description = "批量检查用户权限（内部接口）")
//    public Result<BatchPermissionCheckResponse> checkPermissions(
//            @Valid @RequestBody BatchPermissionCheckRequest request) {
//        log.info("批量权限校验请求: 用户ID={}, 权限数量={}",
//                request.getUserId(), request.getPermissions().size());
//
//        // TODO: 实现批量权限校验逻辑
//        // 1. 查询用户所有权限
//        // 2. 逐一检查请求的权限列表
//        // 3. 返回权限检查结果映射
//
//        return Result.success();
//    }
//
//}
