package hbnu.project.zhiyanauth.service;

import hbnu.project.zhiyanauth.model.response.UserLoginResponse;
import hbnu.project.zhiyancommonoauth.model.dto.OAuth2UserInfo;
import hbnu.project.zhiyancommonbasic.domain.R;

/**
 * OAuth2第三方登录服务接口
 * 处理OAuth2登录、用户绑定等业务逻辑
 *
 * @author ErgouTree
 */
public interface OAuth2Service {

    /**
     * 处理OAuth2登录/注册
     * 如果用户已存在（通过邮箱匹配），则直接登录
     * 如果用户不存在，则自动注册并登录
     *
     * @param oauth2UserInfo OAuth2用户信息
     * @return 登录结果（包含JWT Token和用户信息）
     */
    R<UserLoginResponse> handleOAuth2Login(OAuth2UserInfo oauth2UserInfo);
}

