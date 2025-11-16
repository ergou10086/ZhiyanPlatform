package hbnu.project.zhiyanauth.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import hbnu.project.common.log.annotation.AccessLog;
import hbnu.project.common.log.annotation.OperationLog;
import hbnu.project.common.log.annotation.OperationType;
import hbnu.project.zhiyancommonoauth.client.OAuth2Client;
import hbnu.project.zhiyancommonoauth.config.properties.OAuth2Properties;
import hbnu.project.zhiyancommonoauth.model.dto.AuthorizationResult;
import hbnu.project.zhiyancommonoauth.model.dto.OAuth2UserInfo;
import hbnu.project.zhiyanauth.model.response.UserLoginResponse;
import hbnu.project.zhiyanauth.service.OAuth2Service;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * OAuth2第三方登录控制器
 * 处理OAuth2授权和回调
 *
 * @author ErgouTree
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
     * 后端通过code获取用户信息，然后进行登录/注册处理
     *
     * @param provider 提供商名称
     * @param code     授权码
     * @param state    状态参数（用于防CSRF攻击）
     * @return 登录结果（包含JWT Token和用户信息）
     */
    @GetMapping("/callback/{provider}")
    @Operation(summary = "OAuth2回调", description = "第三方平台授权成功后的回调接口，自动完成登录/注册")
    @OperationLog(
            module = "OAuth2认证",
            type = OperationType.LOGIN,
            description = "OAuth2第三方登录",
            recordParams = false,  // 不记录code等敏感参数
            recordResult = false   // 不记录token等敏感结果
    )
    @SentinelResource(value = "oauth2:callback", blockHandler = "callbackBlockHandler")
    public R<UserLoginResponse> callback(
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

            // 3. 处理登录/注册
            R<UserLoginResponse> loginResult = oAuth2Service.handleOAuth2Login(userInfo);

            if (R.isSuccess(loginResult)) {
                log.info("OAuth2登录成功 - 提供商: {}, 用户ID: {}", provider, userInfo.getProviderUserId());
                return loginResult;
            } else {
                log.warn("OAuth2登录失败 - 提供商: {}, 错误: {}", provider, loginResult.getMsg());
                return loginResult;
            }

        } catch (Exception e) {
            log.error("OAuth2回调处理失败 - 提供商: {}, 错误: {}", provider, e.getMessage(), e);
            return R.fail("登录失败: " + e.getMessage());
        }
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
    public R<UserLoginResponse> callbackBlockHandler(String provider, String code, String state, com.alibaba.csp.sentinel.slots.block.BlockException ex) {
        log.warn("OAuth2回调被限流 - 提供商: {}", provider);
        return R.fail(429, "请求过于频繁，请稍后再试");
    }
}

