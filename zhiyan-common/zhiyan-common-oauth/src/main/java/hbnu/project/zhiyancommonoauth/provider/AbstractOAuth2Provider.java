package hbnu.project.zhiyancommonoauth.provider;

import hbnu.project.zhiyancommonoauth.config.properties.OAuth2Properties;
import hbnu.project.zhiyancommonoauth.exception.OAuth2Exception;
import hbnu.project.zhiyancommonoauth.model.dto.OAuth2UserInfo;
import hbnu.project.zhiyancommonbasic.utils.JsonUtils;
import hbnu.project.zhiyancommonbasic.utils.StringUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;

/**
 * OAuth2提供商抽象基类
 * 实现通用的OAuth2流程逻辑
 *
 * @author ErgouTree
 * @rewrite yui
 */
@Slf4j
public abstract class AbstractOAuth2Provider implements OAuth2Provider {

    protected final OAuth2Properties properties;

    protected final RestTemplate restTemplate;

    protected AbstractOAuth2Provider(OAuth2Properties properties, RestTemplate restTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate != null ? restTemplate : createRestTemplate();
    }

    /**
     * 创建RestTemplate实例
     */
    protected RestTemplate createRestTemplate() {
        return new RestTemplate();
    }

    /**
     * 生成授权的URL（通用实现）
     */
    @Override
    public String getAuthorizationUrl(String state, String redirectUri) {
        String baseUrl = getAuthorizationUri();
        String clientId = getClientId();
        String scope = getScope();

        if (StringUtils.isEmpty(baseUrl) || StringUtils.isEmpty(clientId)) {
            throw new OAuth2Exception("OAuth2配置不完整：缺少authorizationUri或clientId");
        }

        StringBuilder url = new StringBuilder(baseUrl);
        url.append("?client_id=").append(clientId);
        url.append("&redirect_uri=").append(redirectUri);
        url.append("&state=").append(state);
        url.append("&response_type=code");

        if (StringUtils.isNotEmpty(scope)) {
            url.append("&scope=").append(scope);
        }

        return url.toString();
    }


    /**
     * 通过授权码获取访问令牌（通用实现）
     */
    @Override
    public String getAccessToken(String code, String redirectUri) {
        String tokenUri = getTokenUri();
        String clientId = getClientId();
        String clientSecret = getClientSecret();

        if (StringUtils.isEmpty(tokenUri) || StringUtils.isEmpty(clientId) || StringUtils.isEmpty(clientSecret)) {
            throw new OAuth2Exception("OAuth2配置不完整：缺少tokenUri、clientId或clientSecret");
        }

        try {
            // 构建请求参数
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("code", code);
            params.add("redirect_uri", redirectUri);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            // HTML 表单默认的提交格式，提交键值对
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            // 期望收到JSON
            headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                    tokenUri,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if(response.getStatusCode().is2xxSuccessful()){
                String body = response.getBody();
                log.debug("获取访问令牌响应: {}", body);

                // 解析响应（可能是JSON或URL编码格式）
                return parseAccessToken(body);
            } else {
                throw new OAuth2Exception("获取访问令牌失败: " + response.getStatusCode());
            }
        }catch (OAuth2Exception e){
            log.error("获取访问令牌异常", e);
            throw new OAuth2Exception("获取访问令牌失败: " + e.getMessage(), e);
        }
    }


    /**
     * 解析访问令牌（子类可重写以支持不同的响应格式）
     */
    protected String parseAccessToken(String responseBody) {
        try{
            // 尝试解析为JSON
            Map<String, Object> json = JsonUtils.parseMap(responseBody);
            String accessToken = (String) Objects.requireNonNull(json).get("access_token");
            if (StringUtils.isNotEmpty(accessToken)) {
                return accessToken;
            }
        }catch (OAuth2Exception e){
            log.debug("响应不是JSON格式，尝试URL编码格式解析");
        }

        // 尝试解析URL编码格式（如：access_token=xxx&token_type=bearer）
        String[] pairs = responseBody.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2 && "access_token".equals(keyValue[0])) {
                return keyValue[1];
            }
        }

        throw new OAuth2Exception("无法从响应中解析访问令牌: " + responseBody);
    }


    /**
     * 通过访问令牌获取用户信息（通用实现）
     */
    @Override
    public OAuth2UserInfo getUserInfo(String accessToken) {
        String userInfoUri = getUserInfoUri();

        if (StringUtils.isEmpty(userInfoUri)) {
            throw new OAuth2Exception("OAuth2配置不完整：缺少userInfoUri");
        }

        try{
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<Void> request = new HttpEntity<>(headers);

            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                    userInfoUri,
                    HttpMethod.GET,
                    request,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String body = response.getBody();
                log.debug("获取用户信息响应: {}", body);

                // 解析用户信息
                return parseUserInfo(body);
            } else {
                throw new OAuth2Exception("获取用户信息失败: " + response.getStatusCode());
            }
        }catch (OAuth2Exception e){
            log.error("获取用户信息异常", e);
            throw new OAuth2Exception("获取用户信息失败: " + e.getMessage(), e);
        }
    }


    /**
     * 解析用户信息（子类必须实现）
     */
    protected abstract OAuth2UserInfo parseUserInfo(String responseBody);


    // ==================== 抽象方法（子类必须实现） ====================

    /**
     * 获取Client ID
     */
    protected abstract String getClientId();

    /**
     * 获取Client Secret
     */
    protected abstract String getClientSecret();

    /**
     * 获取授权范围
     */
    protected abstract String getScope();

    /**
     * 获取授权URI
     */
    protected abstract String getAuthorizationUri();

    /**
     * 获取Token URI
     */
    protected abstract String getTokenUri();

    /**
     * 获取用户信息URI
     */
    protected abstract String getUserInfoUri();
}