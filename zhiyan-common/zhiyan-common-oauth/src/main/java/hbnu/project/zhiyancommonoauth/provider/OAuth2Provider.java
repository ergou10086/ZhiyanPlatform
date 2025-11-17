package hbnu.project.zhiyancommonoauth.provider;

import hbnu.project.zhiyancommonoauth.model.dto.OAuth2UserInfo;

/**
 * OAuth2提供商接口
 * 定义OAuth2提供商的通用行为
 *
 * @author ErgouTree
 */
public interface OAuth2Provider {

    /**
     * 获取提供商名称
     *
     * @return 提供商名称（如：github）
     */
    String getProviderName();


    /**
     * 生成授权URL
     *
     * @param state 状态参数（用于防止CSRF攻击）
     * @param redirectUri 回调地址
     * @return 授权URL
     */
    String getAuthorizationUrl(String state, String redirectUri);


    /**
     * 通过授权码获取访问令牌
     *
     * @param code 授权码
     * @param redirectUri 回调地址
     * @return 访问令牌
     */
    String getAccessToken(String code, String redirectUri);

    /**
     * 通过访问令牌获取用户信息
     *
     * @param accessToken 访问令牌
     * @return 用户信息
     */
    OAuth2UserInfo getUserInfo(String accessToken);

    /**
     * 是否启用
     *
     * @return true-启用，false-禁用
     */
    boolean isEnabled();
}
