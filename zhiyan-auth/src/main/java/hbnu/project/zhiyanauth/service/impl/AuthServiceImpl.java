package hbnu.project.zhiyanauth.service.impl;

import hbnu.project.zhiyanauth.model.dto.TokenDTO;
import hbnu.project.zhiyanauth.model.dto.UserDTO;
import hbnu.project.zhiyanauth.model.entity.User;
import hbnu.project.zhiyanauth.model.enums.UserStatus;
import hbnu.project.zhiyanauth.model.enums.VerificationCodeType;
import hbnu.project.zhiyanauth.model.form.LoginBody;
import hbnu.project.zhiyanauth.model.form.RegisterBody;
import hbnu.project.zhiyanauth.model.form.ResetPasswordBody;
import hbnu.project.zhiyanauth.model.form.VerificationCodeBody;
import hbnu.project.zhiyanauth.repository.UserRepository;
import hbnu.project.zhiyanauth.model.response.TokenValidateResponse;
import hbnu.project.zhiyanauth.model.response.UserLoginResponse;
import hbnu.project.zhiyanauth.model.response.UserRegisterResponse;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
            // 1. 邮箱唯一性校验
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return R.fail("该邮箱已被注册");
            }

            // 2. 密码一致性校验
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                return R.fail("两次输入的密码不一致");
            }

            // 3. 密码强度校验（只校验合法性，不在接口里暴露强度分数）
            if (!PasswordUtils.isValidPassword(request.getPassword())) {
                return R.fail("密码必须为6-16位字母和数字组合");
            }

            // 4. 校验验证码
            if (!verificationCodeService.validateCode(
                    request.getEmail(), request.getVerificationCode(), VerificationCodeType.REGISTER
            ).getData()) {
                return R.fail("验证码错误或已过期");
            }

            // 5. 创建用户
            User user = User.builder()
                    .email(request.getEmail())
                    .passwordHash(passwordEncoder.encode(request.getPassword()))
                    .name(request.getName())
                    .title(request.getTitle())
                    .institution(request.getInstitution())
                    .status(UserStatus.ACTIVE)
                    .isDeleted(false)
                    .isLocked(false)
                    .build();

            User savedUser = userRepository.save(user);

            // 6. 生成JWT令牌（注册后自动登录）
            boolean rememberMe = false; // 注册时固定为 falsefalse;
            TokenDTO tokenDTO = generateTokens(savedUser.getId(), rememberMe);

            // 7. 构建完整的响应（包含登录令牌）
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
                    .rememberMe(rememberMe)
                    .build();

            log.info("用户注册成功并自动登录: 用户ID={}, 邮箱={}", savedUser.getId(), savedUser.getEmail());
            return R.ok(response);

        } catch (Exception e) {
            log.error("用户注册失败 - 邮箱: {}, 错误: {}", request.getEmail(), e.getMessage());
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


            // 1. 检查用户状态（用枚举）
            if (user.getStatus() != UserStatus.ACTIVE) {
                return R.fail("账户不可用：" + user.getStatus().getDescription());
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

            boolean exists = userRepository.findByEmail(email).isPresent();
            return exists ? R.fail("邮箱已被注册") : R.ok(true, "邮箱可用");

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

            log.info("=== 生成的Token信息 ===");
            log.info("用户ID: {}", userId);
            log.info("访问Token: {}", accessToken);
            log.info("刷新Token: {}", refreshToken);
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
     * 刷新令牌
     * 根据传入的refreshToken，生成新的访问令牌 Access Token
     */
    @Override
    public TokenDTO refreshToken(String refreshToken) {
        try {
            // 1. 校验 Refresh Token 格式和签名
            if (!jwtUtils.validateToken(refreshToken)) {
                throw new RuntimeException("无效的Refresh Token");
            }

            // 2. 校验是否在黑名单
            if (isTokenBlacklisted(refreshToken)) {
                throw new RuntimeException("Refresh Token已失效");
            }

            // 3. 解析用户ID
            String userIdStr = jwtUtils.parseToken(refreshToken);
            if (userIdStr == null) {
                throw new RuntimeException("无法解析用户ID");
            }
            Long userId = Long.valueOf(userIdStr);

            // 4. 判断 Refresh Token 是否过期
            Long remainingTime = jwtUtils.getRemainingTime(refreshToken);
            if (remainingTime == null || remainingTime <= 0) {
                throw new RuntimeException("Refresh Token已过期");
            }

            // 5. 生成新的Access Token
            int accessTokenExpireMinutes = TokenConstants.DEFAULT_ACCESS_TOKEN_EXPIRE_MINUTES;
            String newAccessToken = jwtUtils.createToken(userIdStr, accessTokenExpireMinutes);

            // 存储新的Access Token到Redis
            String tokenKey = CacheConstants.USER_TOKEN_PREFIX + userId;
            long cacheTimeSeconds = (long) accessTokenExpireMinutes * 60;
            redisService.setCacheObject(tokenKey, newAccessToken, cacheTimeSeconds, TimeUnit.SECONDS);

            // 6. 返回新的TokenDTO（Refresh Token一般不变）
            TokenDTO tokenDTO = new TokenDTO();
            tokenDTO.setAccessToken(newAccessToken);
            tokenDTO.setRefreshToken(refreshToken); // 保持不变
            tokenDTO.setTokenType(TokenConstants.TOKEN_TYPE_BEARER);
            tokenDTO.setExpiresIn(cacheTimeSeconds);

            log.info("刷新Token成功 - 用户ID: {}", userId);
            return tokenDTO;

        } catch (Exception e) {
            log.error("刷新Token失败 - 错误: {}", e.getMessage(), e);
            throw new RuntimeException("刷新Token失败");
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
     * 将指定 token 加入黑名单，并清除用户的缓存
     */
    @Override
    public void blacklistToken(String token, Long userId) {
        if (StringUtils.isBlank(token) || userId == null) {
            log.warn("加入黑名单失败 - token 或 userId 为空");
            return;
        }

        try {
            // 计算 token 剩余有效时间
            Long remainingTime = jwtUtils.getRemainingTime(token);
            if (remainingTime != null && remainingTime > 0) {
                String blacklistKey = CacheConstants.TOKEN_BLACKLIST_PREFIX + token;
                redisService.setCacheObject(blacklistKey, userId.toString(), remainingTime, TimeUnit.SECONDS);
                log.debug("Token 已加入黑名单 - 用户ID: {}, 剩余有效期: {} 秒", userId, remainingTime);
            }

            // 清除用户的 token 缓存（保证后续不会再查到）
            String userTokenKey = CacheConstants.USER_TOKEN_PREFIX + userId;
            redisService.deleteObject(userTokenKey);

            log.info("Token 已失效并加入黑名单 - 用户ID: {}", userId);

        } catch (Exception e) {
            log.error("加入 token 黑名单失败 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
        }
    }



    /**
     * 检查 token 是否在黑名单中
     */
    @Override
    public boolean isTokenBlacklisted(String token) {
        if (StringUtils.isBlank(token)) {
            return false;
        }
        try {
            String blacklistKey = CacheConstants.TOKEN_BLACKLIST_PREFIX + token;
            return Boolean.TRUE.equals(redisService.hasKey(blacklistKey));
        } catch (Exception e) {
            log.warn("检查 token 黑名单状态失败 - token: {}, 错误: {}", token, e.getMessage());
            return false;
        }
    }


    /**
     * 验证令牌并返回详细验证结果
     */
    @Override
    public TokenValidateResponse validateTokenWithDetails(String token) {
        TokenValidateResponse response = new TokenValidateResponse();

        try {
            // 移除Bearer前缀（如果服务层被直接调用，可能没有前缀）
            String cleanToken = removeBearerPrefix(token);

            // 1. 检查Token是否在黑名单中
            if (isTokenBlacklisted(cleanToken)) {
                response.setIsValid(false);
                response.setMessage("令牌已失效");
                log.debug("令牌验证失败 - 在黑名单中");
                return response;
            }

            // 2. 校验JWT Token签名和有效期
            String userId = validateToken(cleanToken);
            if (userId == null) {
                response.setIsValid(false);
                response.setMessage("令牌无效或已过期");
                log.debug("令牌验证失败 - JWT验证不通过");
                return response;
            }

            // 3. 获取令牌剩余时间
            Long remainingTime = jwtUtils.getRemainingTime(cleanToken);

            // 4. 构建成功响应
            response.setIsValid(true);
            response.setUserId(userId);
            response.setRemainingTime(remainingTime);
            response.setMessage("令牌有效");

            log.debug("令牌验证成功 - 用户ID: {}, 剩余时间: {}秒", userId, remainingTime);

        } catch (Exception e) {
            log.error("令牌验证异常", e);
            response.setIsValid(false);
            response.setMessage("令牌验证异常");
        }

        return response;
    }

    /**
     * 去掉 Bearer 前缀（如果有的话）
     */
    private String removeBearerPrefix(String token) {
        if (token == null) {
            return null;
        }
        if (token.startsWith("Bearer ")) {
            return token.substring(7).trim();
        }
        return token.trim();
    }


    /**
     * 忘记密码 - 发送重置密码验证码
     * */
    @Override
    public R<Void> forgotPassword(String email) {
        log.info("处理忘记密码请求 - 邮箱: {}", email);

        try {
            // 1. 检查邮箱是否存在
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return R.fail("该邮箱未注册");
            }

            // 2. 生成并发送重置密码验证码
            return verificationCodeService.generateAndSendCode(email, VerificationCodeType.RESET_PASSWORD);



        } catch (Exception e) {
            log.error("忘记密码处理失败 - 邮箱: {}, 错误: {}", email, e.getMessage(), e);
            return R.fail("发送重置验证码失败，请稍后重试");
        }
    }

    /**
     * 重置密码
     */
    @Override
    @Transactional
    public R<Void> resetPassword(ResetPasswordBody request) {
        log.info("处理重置密码请求 - 邮箱: {}", request.getEmail());

        try {
            // 1. 查找用户
            Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
            if (userOpt.isEmpty()) {
                return R.fail("该邮箱未注册");
            }
            User user = userOpt.get();

            // 1. 校验验证码
            R<Boolean> validResult = verificationCodeService.validateCode(
                    request.getEmail(), request.getVerificationCode(), VerificationCodeType.RESET_PASSWORD
            );
            if (!R.isSuccess(validResult) || Boolean.FALSE.equals(validResult.getData())) {
                return R.fail("验证码错误或已过期");
            }

            // 2. 校验两次密码一致性
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return R.fail("两次输入的密码不一致");
            }

            // 3. 校验密码强度
            if (!PasswordUtils.isValidPassword(request.getNewPassword())) {
                return R.fail("密码必须为6-16位字母和数字组合");
            }

            // 5. 更新用户密码
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            // 6. 将旧 token 加入黑名单并清理
            String userTokenKey = CacheConstants.USER_TOKEN_PREFIX + user.getId();
            String oldToken = redisService.getCacheObject(userTokenKey);
            if (oldToken != null) {
                blacklistToken(oldToken, user.getId());
            }

            log.info("用户密码重置成功 - 用户ID: {}, 邮箱: {}", user.getId(), user.getEmail());
            return R.ok(null, "密码重置成功，请重新登录");

        } catch (Exception e) {
            log.error("重置密码失败 - 邮箱: {}, 错误: {}", request.getEmail(), e.getMessage(), e);
            return R.fail("密码重置失败，请稍后重试");
        }
    }

    @Override
    public R<Void> logout(String tokenHeader) {
        if (StringUtils.isBlank(tokenHeader) ||
                !tokenHeader.startsWith(TokenConstants.TOKEN_TYPE_BEARER + " ")) {
            return R.fail("无效的Authorization头");
        }

        // 去掉 "Bearer " 前缀
        String token = tokenHeader.substring((TokenConstants.TOKEN_TYPE_BEARER + " ").length());

        try {
            // 1. 验证 token
            String userIdStr = validateToken(token);
            if (userIdStr == null) {
                return R.fail("无效的Token");
            }
            Long userId = Long.valueOf(userIdStr);

            // 2. 将 token 加入黑名单并清理缓存
            blacklistToken(token, userId);

            log.info("用户登出成功 - 用户ID: {}", userId);
            return R.ok(null,"登出成功");  //

        } catch (Exception e) {
            log.error("用户登出失败 - 错误: {}", e.getMessage(), e);
            return R.fail("登出失败");
        }
    }


}
