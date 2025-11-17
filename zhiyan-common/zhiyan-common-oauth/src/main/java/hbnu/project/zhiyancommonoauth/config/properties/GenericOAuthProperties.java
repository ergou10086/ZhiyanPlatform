package hbnu.project.zhiyancommonoauth.config.properties;

import lombok.Data;

/**
 * 通用 OAuth2 提供商的配置属性模板
 * 适用于非特定平台（如自建OAuth2服务、小众平台）的配置
 *
 * @author ErgouTree
 */
@Data
public class GenericOAuthProperties {
    /**
     * 是否启用当前提供商
     */
    private boolean enabled = false;

    /**
     * 提供商的 Client ID
     */
    private String clientId;

    /**
     * 提供商的 Client Secret
     */
    private String clientSecret;

    /**
     * 授权范围（scope）
     */
    private String scope;

    /**
     * 授权服务器的授权端点URL
     */
    private String authorizationUri;

    /**
     * 授权服务器的令牌端点URL
     */
    private String tokenUri;

    /**
     * 提供商的用户信息API端点URL
     */
    private String userInfoUri;
}
