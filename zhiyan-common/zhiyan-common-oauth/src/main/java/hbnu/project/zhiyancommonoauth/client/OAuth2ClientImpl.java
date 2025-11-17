package hbnu.project.zhiyancommonoauth.client;

import hbnu.project.zhiyancommonoauth.exception.OAuth2Exception;
import hbnu.project.zhiyancommonoauth.model.dto.AuthorizationResult;
import hbnu.project.zhiyancommonoauth.model.dto.OAuth2UserInfo;
import hbnu.project.zhiyancommonoauth.provider.OAuth2Provider;
import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import hbnu.project.zhiyancommonredis.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;

/**
 * OAuth2客户端实现类
 * 封装OAuth2登录流程的核心逻辑
 *
 * @author ErgouTree
 */
@Slf4j
@Service
public class OAuth2ClientImpl implements OAuth2Client {

    private final Map<String, OAuth2Provider> providers;

    /**
     * 构造函数注入providers List，并转换为Map
     */
    public OAuth2ClientImpl(List<OAuth2Provider> providers) {
        if (providers != null && !providers.isEmpty()) {
            this.providers = providers.stream()
                    .filter(OAuth2Provider::isEnabled)
                    .collect(Collectors.toMap(
                            OAuth2Provider::getProviderName,
                            provider -> provider,
                            (existing, replacement) -> existing
                    ));
        } else {
            this.providers = emptyMap();
        }
    }

    /**
     * State缓存前缀
     */
    private static final String STATE_CACHE_PREFIX = "oauth2:state:";

    /**
     * State过期时间（秒）
     */
    private static final long STATE_EXPIRE_SECONDS = 600L; // 10分钟

    /**
     * 生成授权URL
     */
    @Override
    public AuthorizationResult getAuthorizationUrl(String providerName, String redirectUri) {
        OAuth2Provider provider = getProvider(providerName);

        // 生成state（用于防止CSRF攻击）
        String state = generateState();

        // 生成授权URL
        String authorizationUrl = provider.getAuthorizationUrl(state, redirectUri);

        log.info("生成OAuth2授权URL - 提供商: {}, state: {}", providerName, state);

        return new AuthorizationResult(authorizationUrl, state);
    }

    /**
     * 通过授权码获取用户信息
     */
    @Override
    public OAuth2UserInfo getUserInfoByCode(String providerName, String code, String state, String redirectUri) {
        // 验证state
        validateState(state);

        OAuth2Provider provider = getProvider(providerName);

        // 获取访问令牌
        String accessToken = provider.getAccessToken(code, redirectUri);
        log.debug("获取访问令牌成功 - 提供商: {}", providerName);

        // 获取用户信息
        OAuth2UserInfo userInfo = provider.getUserInfo(accessToken);
        log.info("获取用户信息成功 - 提供商: {}, 用户ID: {}", providerName, userInfo.getProviderUserId());

        return userInfo;
    }

    /**
     * 获取OAuth2提供商
     */
    private OAuth2Provider getProvider(String providerName) {
        if (StringUtils.isEmpty(providerName)) {
            throw new OAuth2Exception("OAuth2提供商名称不能为空");
        }

        OAuth2Provider provider = providers.get(providerName.toLowerCase());
        if (provider == null) {
            throw new OAuth2Exception("不支持的OAuth2提供商: " + providerName);
        }

        if (!provider.isEnabled()) {
            throw new OAuth2Exception("OAuth2提供商未启用: " + providerName);
        }

        return provider;
    }

    /**
     * 生成state并缓存
     */
    private String generateState() {
        String state = UUID.randomUUID().toString().replace("-", "");
        String cacheKey = STATE_CACHE_PREFIX + state;

        // 缓存state（10分钟过期）
        RedisUtils.setCacheObject(cacheKey, System.currentTimeMillis(), Duration.ofSeconds(STATE_EXPIRE_SECONDS));

        return state;
    }

    /**
     * 验证state
     */
    private void validateState(String state) {
        if (StringUtils.isEmpty(state)) {
            throw new OAuth2Exception("State参数不能为空");
        }

        String cacheKey = STATE_CACHE_PREFIX + state;
        Object cached = RedisUtils.getCacheObject(cacheKey);

        if (cached == null) {
            throw new OAuth2Exception("State已过期或无效");
        }

        // 验证后删除state（一次性使用）
        RedisUtils.deleteObject(cacheKey);
    }
}
