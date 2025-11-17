package hbnu.project.zhiyanauth.service;

import hbnu.project.zhiyanauth.model.form.OAuth2BindAccountBody;
import hbnu.project.zhiyanauth.model.form.OAuth2SupplementInfoBody;
import hbnu.project.zhiyanauth.model.response.OAuth2LoginResponse;
import hbnu.project.zhiyancommonoauth.model.dto.OAuth2UserInfo;
import hbnu.project.zhiyancommonbasic.domain.R;

/**
 * OAuth2第三方登录服务接口
 * 处理OAuth2登录、用户绑定等业务逻辑
 *
 * @author ErgouTree
 * @rewrite yui
 */
public interface OAuth2Service {

    /**
     * 处理OAuth2登录
     * 策略：
     * 1. 如果邮箱匹配到已有账号，直接登录
     * 2. 如果邮箱匹配到已有账号但需要验证，返回需要绑定状态
     * 3. 如果邮箱未匹配且OAuth2信息不足，返回需要补充信息状态
     *
     * @param oauth2UserInfo OAuth2用户信息
     * @return 登录响应（可能包含登录成功、需要绑定、需要补充信息等状态）
     */
    R<OAuth2LoginResponse> handleOAuth2Login(OAuth2UserInfo oauth2UserInfo);

    /**
     * 绑定已有账号
     * 将OAuth2账号绑定到已有的本地账号（通过邮箱和密码验证）
     *
     * @param bindBody 绑定请求体
     * @return 登录结果
     */
    R<OAuth2LoginResponse> bindAccount(OAuth2BindAccountBody bindBody);

    /**
     * 补充信息创建账号
     * 当OAuth2信息不足时，用户补充必要信息（邮箱、密码）后创建账号
     *
     * @param supplementBody 补充信息请求体
     * @return 登录结果
     */
    R<OAuth2LoginResponse> supplementInfoAndCreateAccount(OAuth2SupplementInfoBody supplementBody);
}

