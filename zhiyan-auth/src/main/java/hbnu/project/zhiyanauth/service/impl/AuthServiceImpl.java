package hbnu.project.zhiyanauth.service.impl;

import hbnu.project.zhiyanauth.model.dto.TokenDTO;
import hbnu.project.zhiyanauth.model.dto.UserDTO;
import hbnu.project.zhiyanauth.model.entity.User;
import hbnu.project.zhiyanauth.model.enums.VerificationCodeType;
import hbnu.project.zhiyanauth.model.form.LoginBody;
import hbnu.project.zhiyanauth.model.form.RegisterBody;
import hbnu.project.zhiyanauth.model.form.VerificationCodeBody;
import hbnu.project.zhiyanauth.repository.UserRepository;
import hbnu.project.zhiyanauth.response.UserLoginResponse;
import hbnu.project.zhiyanauth.response.UserRegisterResponse;
import hbnu.project.zhiyanauth.service.AuthService;
import hbnu.project.zhiyanauth.service.VerificationCodeService;
import hbnu.project.zhiyancommonbasic.constants.CacheConstants;
import hbnu.project.zhiyancommonbasic.constants.TokenConstants;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonbasic.utils.JwtUtils;
import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import hbnu.project.zhiyancommonredis.service.RedisService;
import hbnu.project.zhiyancommonsecurity.utils.PasswordUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现类
 * 处理JWT令牌、验证码等认证相关核心逻辑
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final VerificationCodeService verificationCodeService;
    private final UserRepository userRepository;
    private final RedisService redisService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;


    /**
     * 密码编码器
     *
     * @return 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 用户注册
     *
     * @param request 注册请求体
     * @return 注册结果
     */
    @Override
    @Transactional
    public R<UserRegisterResponse> register(RegisterBody request) {
        log.info("处理用户注册: 邮箱={}, 姓名={}", request.getEmail(), request.getName());

        try {
            // 1. 检查密码和确认密码是否一致
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                return R.fail("密码和确认密码不一致");
            }

            // 2. 检查密码强度
            if (!PasswordUtils.isValidPassword(request.getPassword())) {
                return R.fail("密码必须为6-16位字母和数字组合");
            }

            int passwordStrength = PasswordUtils.validatePasswordStrength(request.getPassword());
            if (passwordStrength < 2) {
                log.warn("用户注册密码强度较弱 - 邮箱: {}, 强度等级: {}", request.getEmail(), passwordStrength);
            }

            // 3. 检查邮箱是否已存在
            Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
            if (existingUser.isPresent()) {
                return R.fail("该邮箱已被注册");
            }

            // 4. 验证验证码
            R<Boolean> verifyResult = verifyCode(
                    request.getEmail(),
                    request.getVerificationCode(),
                    VerificationCodeType.REGISTER.name()
            );

            if (R.isError(verifyResult) || !Boolean.TRUE.equals(verifyResult.getData())) {
                return R.fail(verifyResult.getMsg() != null ? verifyResult.getMsg() : "验证码错误或已过期");
            }

            // 5. 加密密码
            String encodedPassword = passwordEncoder.encode(request.getPassword());

            // 6. 创建用户记录
            User newUser = User.builder()
                    .email(request.getEmail())
                    .passwordHash(encodedPassword)
                    .name(request.getName())
                    .title(request.getTitle())
                    .institution(request.getInstitution())
                    .isLocked(false)
                    .isDeleted(false)
                    //.createdTime(LocalDateTime.now())
                    //.updatedTime(LocalDateTime.now())
                    //.lastLoginTime(LocalDateTime.now())
                    .build();

            User savedUser = userRepository.save(newUser);

            // 7. 生成令牌
            TokenDTO tokenDTO = generateTokens(savedUser.getId(), false);

            // 8. 构建响应
            UserRegisterResponse response = UserRegisterResponse.builder()
                    .userId(savedUser.getId())
                    .email(savedUser.getEmail())
                    .name(savedUser.getName())
                    .title(savedUser.getTitle())
                    .institution(savedUser.getInstitution())
                    .accessToken(tokenDTO.getAccessToken())
                    .refreshToken(tokenDTO.getRefreshToken())
                    .expiresIn(tokenDTO.getExpiresIn())
                    .tokenType(tokenDTO.getTokenType())
                    .passwordStrength(PasswordUtils.getPasswordStrengthDescription(request.getPassword()))
                    .build();

            log.info("用户注册成功: 用户ID={}, 邮箱={}, 密码强度={}",
                    savedUser.getId(), savedUser.getEmail(), passwordStrength);
            return R.ok(response);

        } catch (Exception e) {
            log.error("用户注册失败 - 邮箱: {}, 错误: {}", request.getEmail(), e.getMessage(), e);
            return R.fail("注册失败，请稍后重试");
        }
    }

    /**
     * 用户登录
     *
     * @param loginBody 登录请求体
     * @return 登录结果
     */
    @Override
    public R<UserLoginResponse> login(LoginBody loginBody) {
        log.info("处理用户登录: 邮箱={}", loginBody.getEmail());

        try {
            // 1. 根据邮箱查询用户
            Optional<User> userOptional = userRepository.findByEmail(loginBody.getEmail());
            if (userOptional.isEmpty()) {
                log.warn("登录失败: 用户不存在 - 邮箱: {}", loginBody.getEmail());
                return R.fail("邮箱或密码错误");
            }

            User user = userOptional.get();

            // 2. 检查用户状态
            if (Boolean.TRUE.equals(user.getIsDeleted())) {
                log.warn("登录失败: 用户已被删除 - 用户ID: {}, 邮箱: {}", user.getId(), user.getEmail());
                return R.fail("账户不存在");
            }

            if (Boolean.TRUE.equals(user.getIsLocked())) {
                log.warn("登录失败: 用户已被锁定 - 用户ID: {}, 邮箱: {}", user.getId(), user.getEmail());
                return R.fail("账户已被锁定，请联系管理员");
            }

            // 3. 校验密码
            if (!passwordEncoder.matches(loginBody.getPassword(), user.getPasswordHash())) {
                log.warn("登录失败: 密码错误 - 邮箱: {}", loginBody.getEmail());
                return R.fail("邮箱或密码错误");
            }

            // 4. 生成JWT令牌
            boolean rememberMe = loginBody.getRememberMe() != null ? loginBody.getRememberMe() : false;
            TokenDTO tokenDTO = generateTokens(user.getId(), rememberMe);

            // 5. 更新最后登录时间
            //user.setLastLoginTime(LocalDateTime.now());
            userRepository.save(user);

            // 6. 构建用户信息DTO
            UserDTO userDTO = UserDTO.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .avatarUrl(user.getAvatarUrl())
                    .title(user.getTitle())
                    .institution(user.getInstitution())
                    //.lastLoginTime(user.getLastLoginTime())
                   // .createdTime(user.getCreatedTime())
                    //.updatedTime(user.getUpdatedTime())
                    .build();

            // 7. 构建登录响应
            UserLoginResponse response = UserLoginResponse.builder()
                    .user(userDTO)
                    .accessToken(tokenDTO.getAccessToken())
                    .refreshToken(tokenDTO.getRefreshToken())
                    .expiresIn(tokenDTO.getExpiresIn())
                    .tokenType(tokenDTO.getTokenType())
                    .rememberMe(rememberMe)
                    .build();

            log.info("用户登录成功: 用户ID={}, 邮箱={}, 记住我={}",
                    user.getId(), user.getEmail(), rememberMe);
            return R.ok(response);

        } catch (Exception e) {
            log.error("用户登录失败 - 邮箱: {}, 错误: {}", loginBody.getEmail(), e.getMessage(), e);
            return R.fail("登录失败，请稍后重试");
        }
    }




    /**
     * 检查邮箱
     *
     * @param email 邮箱
     * @return 检查结果
     */
    @Override
    public R<Boolean> checkEmail(String email) {
        log.info("检查邮箱: 邮箱={}", email);

        try {
            // 校验邮箱格式
            if (!isValidEmail(email)) {
                return R.fail("邮箱格式不正确");
            }

            Optional<User> existingUser = userRepository.findByEmail(email);
            return R.ok(existingUser.isPresent(), existingUser.isPresent() ? "邮箱已被注册" : "邮箱可用");

        } catch (Exception e) {
            log.error("检查邮箱异常 - 邮箱: {}, 错误: {}", email, e.getMessage(), e);
            return R.fail("检查邮箱失败，请稍后重试");
        }
    }

    /**
     * 邮箱格式验证
     */
    private boolean isValidEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    /**
     * 发送验证码
     * 根据请求参数中的邮箱和验证码类型，生成并发送相应的验证码
     *
     * @param verificationCodeBody 包含邮箱和验证码类型的请求体
     * @return 操作结果，成功或失败信息
     */
    @Override
    public R<Void> sendVerificationCode(VerificationCodeBody verificationCodeBody) {
        try {
            VerificationCodeType type = VerificationCodeType.valueOf(verificationCodeBody.getType().toUpperCase());
            return verificationCodeService.generateAndSendCode(verificationCodeBody.getEmail(), type);
        } catch (Exception e) {
            log.error("发送验证码失败 - 邮箱: {}, 类型: {}, 错误: {}", 
                verificationCodeBody.getEmail(), verificationCodeBody.getType(), e.getMessage(), e);
            return R.fail("发送验证码失败，请稍后重试");
        }
    }


    /**
     * 验证验证码
     * 检查用户输入的验证码是否与系统生成的一致
     *
     * @param email 接收验证码的邮箱
     * @param code 用户输入的验证码
     * @param type 验证码类型（字符串形式）
     * @return 验证结果，true表示验证通过，false表示失败
     */


    @Override
    public R<Boolean> verifyCode(String email, String code, String type) {
        try {
            VerificationCodeType codeType = VerificationCodeType.valueOf(type.toUpperCase());
            R<Boolean> result = verificationCodeService.validateCode(email, code, codeType);

            // 如果验证失败，返回具体的错误信息
            if (R.isSuccess(result) && Boolean.FALSE.equals(result.getData())) {
                return R.fail(result.getMsg() != null ? result.getMsg() : "验证码错误或已过期");
            }

            return result;

        } catch (IllegalArgumentException e) {
            log.error("验证码类型错误 - 类型: {}, 错误: {}", type, e.getMessage());
            return R.fail("验证码类型错误");
        } catch (Exception e) {
            log.error("验证验证码失败 - 邮箱: {}, 类型: {}, 错误: {}", email, type, e.getMessage(), e);
            return R.fail("验证验证码失败，请稍后重试");
        }
    }


    /**
     * 生成JWT令牌对（访问令牌和刷新令牌）
     * 根据用户ID和"记住我"选项生成不同过期时间的令牌
     *
     * @param userId 用户ID
     * @param rememberMe 是否记住我（影响令牌过期时间）
     * @return 包含访问令牌、刷新令牌及相关信息的DTO对象
     */
    @Override
    public TokenDTO generateTokens(Long userId, boolean rememberMe) {
        try {
            // 根据记住我选项确定过期时间（分钟）
            // 访问令牌过期时间：默认较短，记住我时较长
            int accessTokenExpireMinutes = rememberMe ? 
                TokenConstants.REMEMBER_ME_REFRESH_TOKEN_EXPIRE_MINUTES : TokenConstants.DEFAULT_ACCESS_TOKEN_EXPIRE_MINUTES;
            int refreshTokenExpireMinutes = rememberMe ? 
                TokenConstants.REMEMBER_ME_REFRESH_TOKEN_EXPIRE_MINUTES : TokenConstants.DEFAULT_REFRESH_TOKEN_EXPIRE_MINUTES;
            
            // 生成访问令牌
            String accessToken = jwtUtils.createToken(userId.toString(), accessTokenExpireMinutes);

            // 生成刷新令牌（长期有效，用于获取新的访问令牌）
            String refreshToken = jwtUtils.createToken(userId.toString(), refreshTokenExpireMinutes);

            // 构建令牌DTO对象
            TokenDTO tokenDTO = new TokenDTO();
            tokenDTO.setAccessToken(accessToken);
            tokenDTO.setRefreshToken(refreshToken);
            tokenDTO.setTokenType(TokenConstants.TOKEN_TYPE_BEARER);
            // 转换为秒
            tokenDTO.setExpiresIn((long) accessTokenExpireMinutes * 60);

            // 将访问令牌存储到Redis，用于后续校验和管理
            String tokenKey = CacheConstants.USER_TOKEN_PREFIX + userId;
            long cacheTimeSeconds = (long) accessTokenExpireMinutes * 60;
            redisService.setCacheObject(tokenKey, accessToken, cacheTimeSeconds, TimeUnit.SECONDS);
            
            log.info("JWT令牌生成成功 - 用户ID: {}, 记住我: {}", userId, rememberMe);
            return tokenDTO;
            
        } catch (Exception e) {
            log.error("生成JWT令牌失败 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            throw new RuntimeException("生成令牌失败");
        }
    }


    /**
     * 验证JWT令牌的有效性
     * 检查令牌是否合法、未过期，并解析出用户ID
     *
     * @param token 待验证的JWT令牌
     * @return 验证通过返回用户ID，否则返回null
     */
    @Override
    public String validateToken(String token) {
        try {
            // 先通过JWT工具类验证令牌格式和签名
            if (!jwtUtils.validateToken(token)) {
                return null;
            }

            // 解析令牌获取用户ID字符串
            return jwtUtils.parseToken(token);
            
        } catch (Exception e) {
            log.debug("JWT令牌验证失败 - token: {}, 错误: {}", token, e.getMessage());
            return null;
        }
    }


    /**
     * 将token加入黑名单
     */
    @Override
    public void blacklistToken(String token, Long userId) {
        try {
            // 获取token的剩余有效时间
            Long remainingTime = jwtUtils.getRemainingTime(token);
            if (remainingTime != null && remainingTime > 0) {
                String blacklistKey = CacheConstants.TOKEN_BLACKLIST_PREFIX + token;
                redisService.setCacheObject(blacklistKey, userId.toString(), remainingTime, TimeUnit.SECONDS);
            }
            
            // 同时清除用户的token缓存
            String userTokenKey = CacheConstants.USER_TOKEN_PREFIX + userId;
            redisService.deleteObject(userTokenKey);
            
            log.info("Token已加入黑名单 - 用户ID: {}", userId);
            
        } catch (Exception e) {
            log.error("加入token黑名单失败 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
        }
    }


    /**
     * 检查token是否在黑名单中
     */
    @Override
    public boolean isTokenBlacklisted(String token) {
        try {
            String blacklistKey = CacheConstants.TOKEN_BLACKLIST_PREFIX + token;
            return redisService.hasKey(blacklistKey);
        } catch (Exception e) {
            log.debug("检查token黑名单状态失败 - token: {}, 错误: {}", token, e.getMessage());
            return false;
        }
    }
}
