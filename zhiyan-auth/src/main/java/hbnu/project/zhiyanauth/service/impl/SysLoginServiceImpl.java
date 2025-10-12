package hbnu.project.zhiyanauth.service.impl;


import hbnu.project.zhiyanapisystem.service.SysLoginService;
import hbnu.project.zhiyanauth.model.dto.TokenDTO;
import hbnu.project.zhiyanauth.model.dto.UserDTO;
import hbnu.project.zhiyanauth.model.entity.User;
import hbnu.project.zhiyanauth.model.enums.VerificationCodeType;
import hbnu.project.zhiyanauth.model.form.LoginBody;
import hbnu.project.zhiyanauth.model.form.RegisterBody;
import hbnu.project.zhiyanauth.repository.RoleRepository;
import hbnu.project.zhiyanauth.repository.UserRepository;
import hbnu.project.zhiyanauth.repository.UserRoleRepository;
import hbnu.project.zhiyanauth.service.AuthService;
import hbnu.project.zhiyanauth.service.VerificationCodeService;
import hbnu.project.zhiyancommonbasic.constants.GeneralConstants;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonbasic.utils.StringUtils;

import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 登录和注册服务实现类
 * 整合了用户登录、注册等核心认证功能
 * 
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysLoginServiceImpl implements SysLoginService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final VerificationCodeService verificationCodeService;
    private final AuthService authService;
    private final MapperManager mapperManager;

    /**
     * 用户登录
     * 
     * @param loginBody 登录表单数据
     * @return 登录结果，包含用户信息和Token
     */
    public R<TokenDTO> login(LoginBody loginBody) {
        try {
            // 基础参数校验
            if (loginBody == null || StringUtils.isBlank(loginBody.getEmail()) || StringUtils.isBlank(loginBody.getPassword())) {
                log.warn("用户登录失败 - 参数为空");
                return R.fail("邮箱和密码不能为空");
            }

            // 查找用户
            Optional<User> optionalUser = userRepository.findByEmailAndIsDeletedFalse(loginBody.getEmail());
            if (optionalUser.isEmpty()) {
                log.warn("用户登录失败 - 用户不存在: {}", loginBody.getEmail());
                return R.fail("邮箱或密码错误");
            }

            User user = optionalUser.get();

            // 检查用户状态
            if (user.getIsLocked()) {
                log.warn("用户登录失败 - 账号被锁定: {}", loginBody.getEmail());
                return R.fail("账号已被锁定，请联系管理员");
            }

            // 验证密码
            if (!SecurityUtils.matchesPassword(loginBody.getPassword(), user.getPasswordHash())) {
                log.warn("用户登录失败 - 密码错误: {}", loginBody.getEmail());
                return R.fail("邮箱或密码错误");
            }

            // 生成Token
            boolean rememberMe = loginBody.getRememberMe() != null && loginBody.getRememberMe();
            TokenDTO tokenDTO = authService.generateTokens(user.getId(), rememberMe);

            // 设置用户信息
            UserDTO userDTO = mapperManager.convertToUserDTO(user);
            tokenDTO.setUser(userDTO);

            log.info("用户登录成功 - 邮箱: {}, 用户ID: {}", loginBody.getEmail(), user.getId());
            return R.ok(tokenDTO, "登录成功");

        } catch (Exception e) {
            log.error("用户登录异常 - 邮箱: {}, 错误: {}", loginBody.getEmail(), e.getMessage(), e);
            return R.fail("登录失败，请稍后重试");
        }
    }


    /**
     * 用户登出
     * 
     * @param userId 用户ID
     * @param token 当前token
     * @return 登出结果
     */
    public R<Void> logout(Long userId, String token) {
        try {
            // 将token加入黑名单
            authService.blacklistToken(token, userId);

            log.info("用户登出成功 - 用户ID: {}", userId);
            return R.ok(null, "登出成功");
        } catch (Exception e) {
            log.error("用户登出异常 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return R.fail("登出失败");
        }
    }


    /**
     * 用户注册
     * 
     * @param registerBody 注册表单数据
     * @return 注册结果
     */
    @Transactional
    public R<UserDTO> register(RegisterBody registerBody) {
        try {
            // 1. 验证码校验
            R<Boolean> codeValidation = verificationCodeService.validateCode(
                    registerBody.getEmail(),
                    registerBody.getVerificationCode(),
                    VerificationCodeType.REGISTER
            );

            if (!codeValidation.getData() || !GeneralConstants.SUCCESS.equals(codeValidation.getCode())) {
                log.warn("用户注册失败 - 验证码校验失败: {}", registerBody.getEmail());
                return R.fail("验证码验证失败");
            }

            // 2. 检查邮箱是否已存在
            if (userRepository.existsByEmail(registerBody.getEmail())) {
                log.warn("用户注册失败 - 邮箱已存在: {}", registerBody.getEmail());
                return R.fail("该邮箱已被注册");
            }

            // 3. 密码确认校验
            if (!registerBody.getPassword().equals(registerBody.getConfirmPassword())) {
                return R.fail("两次输入的密码不一致");
            }

            // 4. 创建用户实体
            String passwordHash = SecurityUtils.encryptPassword(registerBody.getPassword());
            User user = mapperManager.convertFromRegisterBody(registerBody, passwordHash);

            // 5. 保存用户
            user = userRepository.save(user);

            // 6. 为新用户分配默认角色（普通用户）
            assignDefaultRole(user.getId());

            // 7. 构建返回的用户DTO
            UserDTO userDTO = mapperManager.convertToUserDTO(user);

            log.info("用户注册成功 - 邮箱: {}, 用户ID: {}", registerBody.getEmail(), user.getId());
            return R.ok(userDTO, "注册成功");

        } catch (Exception e) {
            log.error("用户注册异常 - 邮箱: {}, 错误: {}", registerBody.getEmail(), e.getMessage(), e);
            return R.fail("注册失败，请稍后重试");
        }
    }


    /**
     * 刷新Token
     * 
     * @param refreshToken 刷新token
     * @return 新的token信息
     */
    public R<TokenDTO> refreshToken(String refreshToken) {
        try {
            // 检查refreshToken是否在黑名单中
            if (authService.isTokenBlacklisted(refreshToken)) {
                log.warn("Token刷新失败 - refreshToken已在黑名单中");
                return R.fail("刷新令牌已失效，请重新登录");
            }

            // 验证refreshToken并获取用户ID
            String userId = authService.validateToken(refreshToken);
            if (userId == null) {
                return R.fail("刷新令牌无效或已过期");
            }

            // 检查用户是否存在且未被删除
            Optional<User> optionalUser = userRepository.findByIdAndIsDeletedFalse(Long.valueOf(userId));
            if (optionalUser.isEmpty()) {
                return R.fail("用户不存在");
            }

            User user = optionalUser.get();
            if (user.getIsLocked()) {
                return R.fail("账号已被锁定");
            }

            // 将旧的refreshToken加入黑名单
            authService.blacklistToken(refreshToken, Long.valueOf(userId));

            // 生成新的Token
            TokenDTO tokenDTO = authService.generateTokens(Long.valueOf(userId), true);

            // 设置用户信息
            UserDTO userDTO = mapperManager.convertToUserDTO(user);
            tokenDTO.setUser(userDTO);

            log.info("Token刷新成功 - 用户ID: {}", userId);
            return R.ok(tokenDTO, "Token刷新成功");

        } catch (Exception e) {
            log.error("Token刷新异常 - 错误: {}", e.getMessage(), e);
            return R.fail("Token刷新失败");
        }
    }


    /**
     * 为注册新用户分配默认系统角色——普通用户
     * 
     * @param userId 用户ID
     */
    private void assignDefaultRole(Long userId) {
        try {

        } catch (Exception e) {
            log.error("分配默认角色失败 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
        }
    }
}
