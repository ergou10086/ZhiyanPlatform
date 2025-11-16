package hbnu.project.zhiyancommonoauth.provider;

import hbnu.project.zhiyancommonoauth.config.properties.OAuth2Properties;
import hbnu.project.zhiyancommonoauth.exception.OAuth2Exception;
import hbnu.project.zhiyancommonoauth.model.dto.OAuth2UserInfo;
import hbnu.project.zhiyancommonbasic.utils.JsonUtils;
import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * GitHub OAuth2提供商实现
 *
 * @author ErgouTree
 */
@Slf4j
@Component
public class GithubOAuth2Provider extends AbstractOAuth2Provider {

    private static final String PROVIDER_NAME = "github";

    /**
     * 构造函数注入
     * OAuth2Properties 通过 @EnableConfigurationProperties 注册，只有一个Bean实例
     */
    public GithubOAuth2Provider(OAuth2Properties properties, RestTemplate restTemplate) {
        super(properties, restTemplate);
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public boolean isEnabled() {
        return properties.getGithub().isEnabled();
    }

    @Override
    protected String getClientId() {
        return properties.getGithub().getClientId();
    }

    @Override
    protected String getClientSecret() {
        return properties.getGithub().getClientSecret();
    }

    @Override
    protected String getScope() {
        return properties.getGithub().getScope();
    }

    @Override
    protected String getAuthorizationUri() {
        return properties.getGithub().getAuthorizationUri();
    }

    @Override
    protected String getTokenUri() {
        return properties.getGithub().getTokenUri();
    }

    @Override
    protected String getUserInfoUri() {
        return properties.getGithub().getUserInfoUri();
    }


    /**
     * 解析GitHub用户信息
     */
    @Override
    protected OAuth2UserInfo parseUserInfo(String responseBody) {
        try {
            Map<String, Object> userMap = JsonUtils.parseMap(responseBody);

            OAuth2UserInfo userInfo = OAuth2UserInfo.builder()
                    .provider(PROVIDER_NAME)
                    .providerUserId(String.valueOf(Objects.requireNonNull(userMap).get("id")))
                    .username((String) userMap.get("login"))
                    .nickname((String) userMap.get("name"))
                    .avatarUrl((String) userMap.get("avatar_url"))
                    .email((String) userMap.get("email"))
                    .build();

            // GitHub的email可能为null（如果用户设置了隐私），需要单独获取
            if (StringUtils.isEmpty(userInfo.getEmail())) {
                userInfo.setEmail(getUserEmail(userInfo.getAccessToken()));
            }

            return userInfo;
        } catch (Exception e) {
            log.error("解析GitHub用户信息失败", e);
            throw new OAuth2Exception("解析用户信息失败: " + e.getMessage(), e);
        }
    }


    /**
     * 获取GitHub用户邮箱（需要user:email权限）
     */
    private String getUserEmail(String accessToken) {
        try {
            String emailUri = properties.getGithub().getUserEmailUri();
            if (StringUtils.isEmpty(emailUri)) {
                return null;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    emailUri,
                    HttpMethod.GET,
                    request,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> emails = JsonUtils.parseMapList(response.getBody());
                // 优先返回主邮箱（primary=true且verified=true）
                for (Map<String, Object> email : emails) {
                    Boolean primary = (Boolean) email.get("primary");
                    Boolean verified = (Boolean) email.get("verified");
                    if (Boolean.TRUE.equals(primary) && Boolean.TRUE.equals(verified)) {
                        return (String) email.get("email");
                    }
                }
                // 如果没有主邮箱，返回第一个已验证的邮箱
                for (Map<String, Object> email : emails) {
                    Boolean verified = (Boolean) email.get("verified");
                    if (Boolean.TRUE.equals(verified)) {
                        return (String) email.get("email");
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取GitHub用户邮箱失败: {}", e.getMessage());
        }
        return null;
    }


    /**
     * 重写getUserInfo以支持邮箱获取
     */
    @Override
    public OAuth2UserInfo getUserInfo(String accessToken) {
        OAuth2UserInfo userInfo = super.getUserInfo(accessToken);
        // 设置accessToken以便后续获取邮箱
        userInfo.setAccessToken(accessToken);
        return userInfo;
    }
}
