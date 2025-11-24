package hbnu.project.zhiyancommonoauth.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * OAuth2 总配置属性
 * 关联所有第三方提供商的配置
 *
 * @author ErgouTree
 */
@Data
@ConfigurationProperties(prefix = "zhiyan.oauth2")
public class OAuth2Properties {

    /**
     * 是否启用OAuth2功能
     */
    private boolean enabled = false;

    /**
     * 回调地址基础路径（不包含具体路径）
     * 例如：http://localhost:8091
     */
    private String callbackBaseUrl;

    /**
     * 前端回调页面URL（OAuth2登录成功后重定向到的前端页面）
     * 例如：http://localhost:8080/oauth2/callback
     */
    private String frontendCallbackUrl;

    /**
     * GitHub OAuth2 配置（关联独立的GitHub配置类）
     */
    private GitHubOAuthProperties github = new GitHubOAuthProperties();

    // 后续新增其他提供商（如微信、Google），直接在此处添加对应的配置类对象
    // 例如：private WechatOAuthProperties wechat = new WechatOAuthProperties();
}