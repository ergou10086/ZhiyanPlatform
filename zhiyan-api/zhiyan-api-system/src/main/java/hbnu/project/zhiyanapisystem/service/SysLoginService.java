package hbnu.project.zhiyanapisystem.service;

import hbnu.project.zhiyanauth.model.dto.TokenDTO;
import hbnu.project.zhiyanauth.model.dto.UserDTO;
import hbnu.project.zhiyanauth.model.form.LoginBody;
import hbnu.project.zhiyanauth.model.form.RegisterBody;
import hbnu.project.zhiyancommonbasic.domain.R;

/**
 * 登录注册核心业务接口
 * 定义用户登录、注册、登出、Token刷新等认证相关操作
 *
 * @author ErgouTree
 */
public interface SysLoginService {

    /**
     * 用户登录
     * 根据邮箱和密码验证用户身份，生成登录Token并返回用户信息
     *
     * @param loginBody 登录表单数据（包含邮箱、密码、记住我标识）
     * @return R<TokenDTO> - 登录成功返回 Token 信息（含 accessToken、refreshToken、用户DTO）；失败返回错误提示
     */
    R<TokenDTO> login(LoginBody loginBody);

    /**
     * 用户登出
     * 将当前用户的登录Token加入黑名单，使其失效
     *
     * @param userId 用户ID（登出操作的主体）
     * @param token  当前用户的有效登录Token（需加入黑名单）
     * @return R<Void> - 登出成功返回"登出成功"；失败返回错误提示
     */
    R<Void> logout(Long userId, String token);

    /**
     * 用户注册
     * 校验验证码、邮箱唯一性，创建用户并分配默认角色（普通用户）
     *
     * @param registerBody 注册表单数据（包含邮箱、密码、确认密码、验证码）
     * @return R<UserDTO> - 注册成功返回用户基础信息（脱敏后）；失败返回错误提示（如验证码无效、邮箱已注册）
     */
    R<UserDTO> register(RegisterBody registerBody);

    /**
     * 刷新Token
     * 基于有效的 refreshToken 生成新的 accessToken 和 refreshToken（避免用户重复登录）
     *
     * @param refreshToken 有效的刷新令牌（登录时返回的 refreshToken）
     * @return R<TokenDTO> - 刷新成功返回新的 Token 信息；失败返回错误提示（如 refreshToken 无效/过期）
     */
    R<TokenDTO> refreshToken(String refreshToken);
}
