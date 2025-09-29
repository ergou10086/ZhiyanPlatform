package hbnu.project.zhiyanauth.service.impl;

import hbnu.project.zhiyanauth.model.dto.TokenDTO;
import hbnu.project.zhiyanauth.model.enums.VerificationCodeType;
import hbnu.project.zhiyanauth.model.form.VerificationCodeBody;
import hbnu.project.zhiyanauth.repository.UserRepository;
import hbnu.project.zhiyanauth.service.AuthService;
import hbnu.project.zhiyanauth.service.VerificationCodeService;
import hbnu.project.zhiyancommonbasic.constants.CacheConstants;
import hbnu.project.zhiyancommonbasic.constants.TokenConstants;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonbasic.utils.JwtUtils;
import hbnu.project.zhiyancommonredis.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
            return verificationCodeService.validateCode(email, code, codeType);
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
