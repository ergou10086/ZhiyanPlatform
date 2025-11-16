package hbnu.project.zhiyancommonoauth.config.properties;

import lombok.Data;

/**
 * GitHub 平台的 OAuth2 配置属性
 *
 * @author ErgouTree
 */
@Data
public class GitHubOAuthProperties {

    /**
     * 是否启用GitHub登录
     */
    private boolean enabled = false;

    /**
     * GitHub 应用的 Client ID（从GitHub开发者平台获取）
     */
    private String clientId;

    /**
     * GitHub 应用的 Client Secret（从GitHub开发者平台获取）
     */
    private String clientSecret;

    /**
     * 授权范围（scope）
     * 参考GitHub文档：https://docs.github.com/en/developers/apps/building-oauth-apps/scopes-for-oauth-apps
     * 默认：read:user（读取用户基本信息）、user:email（读取用户邮箱）
     */
    private String scope = "read:user,user:email";

    /**
     * GitHub 授权服务器的授权端点URL
     * 固定值：https://github.com/login/oauth/authorize
     */
    private String authorizationUri = "https://github.com/login/oauth/authorize";

    /**
     * GitHub 授权服务器的令牌端点URL（用于通过授权码换令牌）
     * 固定值：https://github.com/login/oauth/access_token
     */
    private String tokenUri = "https://github.com/login/oauth/access_token";

    /**
     * GitHub 用户信息API端点URL（用于获取用户基本信息）
     * 固定值：https://api.github.com/user
     */
    private String userInfoUri = "https://api.github.com/user";

    /**
     * GitHub 用户邮箱API端点URL（需要user:email权限）
     * 固定值：https://api.github.com/user/emails
     */
    private String userEmailUri = "https://api.github.com/user/emails";
}
