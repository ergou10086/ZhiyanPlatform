package hbnu.project.zhiyanauth.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import hbnu.project.common.log.annotation.AccessLog;
import hbnu.project.common.log.annotation.OperationLog;
import hbnu.project.common.log.annotation.OperationType;
import hbnu.project.zhiyancommonoauth.client.OAuth2Client;
import hbnu.project.zhiyancommonoauth.config.properties.OAuth2Properties;
import hbnu.project.zhiyancommonoauth.model.dto.AuthorizationResult;
import hbnu.project.zhiyancommonoauth.model.dto.OAuth2UserInfo;
import hbnu.project.zhiyanauth.model.form.OAuth2BindAccountBody;
import hbnu.project.zhiyanauth.model.form.OAuth2SupplementInfoBody;
import hbnu.project.zhiyanauth.model.response.OAuth2LoginResponse;
import hbnu.project.zhiyanauth.model.response.UserLoginResponse;
import hbnu.project.zhiyanauth.service.OAuth2Service;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * OAuth2第三方登录控制器
 * 处理OAuth2授权和回调
 *
 * @author ErgouTree
 * @rewrite yui
 */
@Slf4j
@RestController
@RequestMapping("/zhiyan/auth/oauth2")
@RequiredArgsConstructor
@Tag(name = "OAuth2第三方登录", description = "GitHub等第三方登录相关接口")
@AccessLog("OAuth2认证服务")
public class OAuth2Controller {

    private final OAuth2Client oAuth2Client;
    private final OAuth2Service oAuth2Service;
    private final OAuth2Properties oAuth2Properties;

    /**
     * 获取授权URL
     * 前端调用此接口获取第三方登录的授权URL，然后跳转到该URL进行授权
     *
     * @param provider 提供商名称（如：github）
     * @return 授权URL和state
     */
    @GetMapping("/authorize/{provider}")
    @Operation(summary = "获取OAuth2授权URL", description = "获取第三方登录的授权URL，用户需要跳转到该URL完成授权")
    @OperationLog(module = "OAuth2认证", type = OperationType.OTHER, description = "获取OAuth2授权URL")
    @SentinelResource(value = "oauth2:authorize", blockHandler = "authorizeBlockHandler")
    public R<AuthorizationResult> getAuthorizationUrl(
            @Parameter(description = "OAuth2提供商名称", example = "github", required = true)
            @PathVariable String provider) {
        log.info("获取OAuth2授权URL请求 - 提供商: {}", provider);

        try {
            String redirectUri = buildCallbackUrl(provider);
            AuthorizationResult result = oAuth2Client.getAuthorizationUrl(provider, redirectUri);
            return R.ok(result, "获取授权URL成功");
        } catch (Exception e) {
            log.error("获取OAuth2授权URL失败 - 提供商: {}, 错误: {}", provider, e.getMessage(), e);
            return R.fail("获取授权URL失败: " + e.getMessage());
        }
    }

    /**
     * OAuth2回调接口
     * 第三方平台授权成功后，会回调到此接口，携带授权码code和state
     * 后端通过code获取用户信息，然后根据情况返回：
     * - 如果邮箱匹配到已有账号，直接登录成功
     * - 如果邮箱未匹配，返回需要绑定账号的状态
     * - 如果OAuth2信息不足（如缺少邮箱），返回需要补充信息的状态
     *
     * @param provider 提供商名称
     * @param code     授权码
     * @param state    状态参数（用于防CSRF攻击）
     * @return 登录响应（可能包含登录成功、需要绑定、需要补充信息等状态）
     */
    @GetMapping("/callback/{provider}")
    @Operation(summary = "OAuth2回调", description = "第三方平台授权成功后的回调接口，根据情况返回登录成功或引导信息")
    @OperationLog(
            module = "OAuth2认证",
            type = OperationType.LOGIN,
            description = "OAuth2第三方登录",
            recordParams = false,  // 不记录code等敏感参数
            recordResult = false   // 不记录token等敏感结果
    )
    @SentinelResource(value = "oauth2:callback", blockHandler = "callbackBlockHandler")
    public ResponseEntity<?> callback(
            @Parameter(description = "OAuth2提供商名称", example = "github", required = true)
            @PathVariable String provider,
            @Parameter(description = "授权码", required = true)
            @RequestParam String code,
            @Parameter(description = "状态参数（用于防CSRF攻击）", required = true)
            @RequestParam String state) {
        log.info("OAuth2回调请求 - 提供商: {}, code: {}, state: {}", provider, code, state);

        try {
            // 1. 构建回调URL
            String redirectUri = buildCallbackUrl(provider);

            // 2. 通过授权码获取用户信息
            OAuth2UserInfo userInfo = oAuth2Client.getUserInfoByCode(provider, code, state, redirectUri);
            log.info("获取OAuth2用户信息成功 - 提供商: {}, 用户ID: {}, 邮箱: {}", 
                    provider, userInfo.getProviderUserId(), userInfo.getEmail());

            // 3. 处理登录（可能返回登录成功、需要绑定、需要补充信息等状态）
            R<OAuth2LoginResponse> loginResult = oAuth2Service.handleOAuth2Login(userInfo);

            if (!R.isSuccess(loginResult) || loginResult.getData() == null) {
                log.warn("OAuth2登录失败 - 提供商: {}, 错误: {}", provider, loginResult.getMsg());
                return redirectToErrorPage(loginResult.getMsg());
            }

            OAuth2LoginResponse loginResponse = loginResult.getData();
            return switch (loginResponse.getStatus()) {
                case SUCCESS -> {
                    log.info("OAuth2登录成功 - 提供商: {}, 用户ID: {}", provider, userInfo.getProviderUserId());
                    yield redirectToHomePage(loginResponse);
                }
                case NEED_BIND -> {
                    log.info("OAuth2需要绑定账号 - 提供商: {}", provider);
                    yield redirectToBindPage(loginResponse);
                }
                case NEED_SUPPLEMENT -> {
                    log.info("OAuth2需要补充信息 - 提供商: {}", provider);
                    yield redirectToSupplementPage(loginResponse);
                }
            };

        } catch (IllegalStateException configException) {
            log.error("OAuth2回调配置错误 - 提供商: {}, 错误: {}", provider, configException.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(configException.getMessage());
        } catch (Exception e) {
            log.error("OAuth2回调处理失败 - 提供商: {}, 错误: {}", provider, e.getMessage(), e);
            return redirectToErrorPage("登录失败: " + e.getMessage());
        }
    }

    /**
     * 绑定已有账号
     * 当OAuth2邮箱匹配到已有账号时，用户输入密码验证后绑定
     *
     * @param bindBody 绑定请求体
     * @return 登录结果
     */
    @PostMapping("/bind")
    @Operation(summary = "绑定已有账号", description = "将OAuth2账号绑定到已有账号（需要验证密码）")
    @OperationLog(
            module = "OAuth2认证",
            type = OperationType.OTHER,
            description = "OAuth2绑定账号",
            recordParams = false,  // 不记录密码等敏感参数
            recordResult = false   // 不记录token等敏感结果
    )
    @SentinelResource(value = "oauth2:bind", blockHandler = "bindBlockHandler")
    public R<OAuth2LoginResponse> bindAccount(
            @Valid @RequestBody OAuth2BindAccountBody bindBody) {
        log.info("绑定OAuth2账号请求 - 提供商: {}, 邮箱: {}", 
                bindBody.getProvider(), bindBody.getEmail());

        try {
            return oAuth2Service.bindAccount(bindBody);
        } catch (Exception e) {
            log.error("绑定OAuth2账号失败 - 提供商: {}, 错误: {}", 
                    bindBody.getProvider(), e.getMessage(), e);
            return R.fail("绑定失败: " + e.getMessage());
        }
    }

    /**
     * 补充信息创建账号
     * 当OAuth2信息不足（如缺少邮箱）时，用户补充必要信息（邮箱、密码）后创建账号
     *
     * @param supplementBody 补充信息请求体
     * @return 登录结果
     */
    @PostMapping("/supplement")
    @Operation(summary = "补充信息创建账号", description = "补充必要信息（邮箱、密码）后创建新账号")
    @OperationLog(
            module = "OAuth2认证",
            type = OperationType.INSERT,
            description = "OAuth2补充信息创建账号",
            recordParams = false,  // 不记录密码等敏感参数
            recordResult = false   // 不记录token等敏感结果
    )
    @SentinelResource(value = "oauth2:supplement", blockHandler = "supplementBlockHandler")
    public R<OAuth2LoginResponse> supplementInfo(
            @Valid @RequestBody OAuth2SupplementInfoBody supplementBody) {
        log.info("补充信息创建账号请求 - 提供商: {}, 邮箱: {}", 
                supplementBody.getProvider(), supplementBody.getEmail());

        try {
            return oAuth2Service.supplementInfoAndCreateAccount(supplementBody);
        } catch (Exception e) {
            log.error("补充信息创建账号失败 - 提供商: {}, 错误: {}", 
                    supplementBody.getProvider(), e.getMessage(), e);
            return R.fail("创建账号失败: " + e.getMessage());
        }
    }

    /**
     * 登录成功后重定向到前端主页
     */
    private ResponseEntity<Void> redirectToHomePage(OAuth2LoginResponse response) {
        UserLoginResponse loginResponse = response.getLoginResponse();
        if (loginResponse == null) {
            throw new IllegalStateException("登录响应为空，无法完成跳转");
        }

        String target = requireFrontendUrl(
                oAuth2Properties.getFrontend().getSuccessUrl(),
                "frontend.success-url");

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(target)
                .queryParam("accessToken", loginResponse.getAccessToken())
                .queryParam("refreshToken", loginResponse.getRefreshToken())
                .queryParam("expiresIn", loginResponse.getExpiresIn())
                .queryParam("tokenType", loginResponse.getTokenType());

        if (Boolean.TRUE.equals(loginResponse.getRememberMe()) && StringUtils.isNotEmpty(loginResponse.getRememberMeToken())) {
            builder.queryParam("rememberMeToken", loginResponse.getRememberMeToken());
        }

        return buildRedirectResponse(builder.build(true).toUriString());
    }

    /**
     * 需要绑定账号时的重定向
     */
    private ResponseEntity<Void> redirectToBindPage(OAuth2LoginResponse response) {
        String target = requireFrontendUrl(
                oAuth2Properties.getFrontend().getBindUrl(),
                "frontend.bind-url");

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(target);
        OAuth2UserInfo userInfo = response.getOauth2UserInfo();
        if (userInfo != null) {
            if (StringUtils.isNotEmpty(userInfo.getProvider())) {
                builder.queryParam("provider", userInfo.getProvider());
            }
            if (StringUtils.isNotEmpty(userInfo.getProviderUserId())) {
                builder.queryParam("providerUserId", userInfo.getProviderUserId());
            }
            if (StringUtils.isNotEmpty(userInfo.getEmail())) {
                builder.queryParam("email", userInfo.getEmail());
            }
        }

        return buildRedirectResponse(builder.build(true).toUriString());
    }

    /**
     * 需要补充信息时的重定向
     */
    private ResponseEntity<Void> redirectToSupplementPage(OAuth2LoginResponse response) {
        String target = requireFrontendUrl(
                oAuth2Properties.getFrontend().getSupplementUrl(),
                "frontend.supplement-url");

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(target);
        OAuth2UserInfo userInfo = response.getOauth2UserInfo();
        if (userInfo != null) {
            if (StringUtils.isNotEmpty(userInfo.getProvider())) {
                builder.queryParam("provider", userInfo.getProvider());
            }
            if (StringUtils.isNotEmpty(userInfo.getProviderUserId())) {
                builder.queryParam("providerUserId", userInfo.getProviderUserId());
            }
            if (StringUtils.isNotEmpty(userInfo.getEmail())) {
                builder.queryParam("email", userInfo.getEmail());
            }
        }

        return buildRedirectResponse(builder.build(true).toUriString());
    }

    /**
     * 错误场景跳转
     */
    private ResponseEntity<Void> redirectToErrorPage(String message) {
        String target = requireFrontendUrl(
                oAuth2Properties.getFrontend().getErrorUrl(),
                "frontend.error-url");

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(target);
        if (StringUtils.isNotEmpty(message)) {
            builder.queryParam("message", message);
        }
        return buildRedirectResponse(builder.build(true).toUriString());
    }

    private ResponseEntity<Void> buildRedirectResponse(String url) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, url)
                .build();
    }

    private String requireFrontendUrl(String value, String propertyName) {
        if (StringUtils.isEmpty(value)) {
            throw new IllegalStateException(
                    "未配置 zhiyan.oauth2." + propertyName + "，无法完成OAuth2重定向");
        }
        return value;
    }

    /**
     * 构建回调URL
     * 根据配置的回调基础路径和提供商名称构建完整的回调URL
     */
    private String buildCallbackUrl(String provider) {
        String baseUrl = oAuth2Properties.getCallbackBaseUrl();
        if (StringUtils.isEmpty(baseUrl)) {
            throw new IllegalArgumentException("OAuth2回调地址基础路径未配置，请在配置文件中设置 zhiyan.oauth2.callback-base-url");
        }

        // 移除末尾的斜杠
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        // 构建完整的回调URL
        // 例如：http://localhost:8091/zhiyan/auth/oauth2/callback/github
        return baseUrl + "/zhiyan/auth/oauth2/callback/" + provider;
    }

    /**
     * Sentinel限流处理 - 获取授权URL
     */
    public R<AuthorizationResult> authorizeBlockHandler(String provider, com.alibaba.csp.sentinel.slots.block.BlockException ex) {
        log.warn("获取OAuth2授权URL被限流 - 提供商: {}", provider);
        return R.fail(429, "请求过于频繁，请稍后再试");
    }

    /**
     * Sentinel限流处理 - 回调
     */
    public ResponseEntity<?> callbackBlockHandler(String provider, String code, String state, com.alibaba.csp.sentinel.slots.block.BlockException ex) {
        log.warn("OAuth2回调被限流 - 提供商: {}", provider);
        return redirectToErrorPage("请求过于频繁，请稍后再试");
    }

    /**
     * Sentinel限流处理 - 绑定账号
     */
    public R<OAuth2LoginResponse> bindBlockHandler(OAuth2BindAccountBody bindBody, com.alibaba.csp.sentinel.slots.block.BlockException ex) {
        log.warn("OAuth2绑定账号被限流 - 提供商: {}", bindBody.getProvider());
        return R.fail(429, "请求过于频繁，请稍后再试");
    }

    /**
     * Sentinel限流处理 - 补充信息
     */
    public R<OAuth2LoginResponse> supplementBlockHandler(OAuth2SupplementInfoBody supplementBody, com.alibaba.csp.sentinel.slots.block.BlockException ex) {
        log.warn("OAuth2补充信息被限流 - 提供商: {}", supplementBody.getProvider());
        return R.fail(429, "请求过于频繁，请稍后再试");
    }
}

