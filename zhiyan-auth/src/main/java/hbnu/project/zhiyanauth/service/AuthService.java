package hbnu.project.zhiyanauth.service;

import hbnu.project.zhiyanauth.model.dto.TokenDTO;
import hbnu.project.zhiyanauth.model.form.LoginBody;
import hbnu.project.zhiyanauth.model.form.RegisterBody;
import hbnu.project.zhiyanauth.model.form.ResetPasswordBody;
import hbnu.project.zhiyanauth.model.form.VerificationCodeBody;
import hbnu.project.zhiyanauth.model.response.TokenValidateResponse;
import hbnu.project.zhiyanauth.model.response.UserLoginResponse;
import hbnu.project.zhiyanauth.model.response.UserRegisterResponse;
import hbnu.project.zhiyancommonbasic.domain.R;

/**
 * 认证服务接口
 * 处理认证相关的核心逻辑
 *
 * @author ErgouTree
 */
public interface AuthService {


    /**
     * 用户注册
     *
     * @param request 注册请求体
     * @return 注册结果
     */
    R<UserRegisterResponse> register(RegisterBody request);

    /**
     * 用户登录
     *
     * @param loginBody 登录请求体
     * @return 登录结果
     */
    R<UserLoginResponse> login(LoginBody loginBody);

    /**
     * 检查邮箱是否已存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    R<Boolean> checkEmail(String email);


    /**
     * 发送验证码
     *
     * @param verificationCodeBody 验证码请求体
     * @return 发送结果
     */
    R<Void> sendVerificationCode(VerificationCodeBody verificationCodeBody);

    /**
     * 验证验证码
     *
     * @param email 邮箱
     * @param code 验证码
     * @param type 验证码类型
     * @return 验证结果
     */
    R<Boolean> verifyCode(String email, String code, String type);

    /**
     * 生成JWT令牌
     *
     * @param userId 用户ID
     * @param rememberMe 是否记住我
     * @return token信息
     */
    TokenDTO generateTokens(Long userId, boolean rememberMe);

    /**
     * 刷新JWT令牌
     *
     * @param refreshToken 刷新令牌
     * @return 新的token信息
     */
    TokenDTO refreshToken(String refreshToken);
    /**
     * 验证JWT令牌
     *
     * @param token JWT令牌
     * @return 用户ID
     */
    String validateToken(String token);

    /**
     * 将token加入黑名单
     *
     * @param token JWT令牌
     * @param userId 用户ID
     */
    void blacklistToken(String token, Long userId);

    /**
     * 检查token是否在黑名单中
     *
     * @param token JWT令牌
     * @return 是否在黑名单中
     */
    boolean isTokenBlacklisted(String token);


    /**
     * 验证令牌并返回详细验证结果
     */
    TokenValidateResponse validateTokenWithDetails(String token);

    R<Void> resetPassword(ResetPasswordBody request);

    R<Void> forgotPassword(String email);

    R<Void> logout(String tokenHeader);
}
