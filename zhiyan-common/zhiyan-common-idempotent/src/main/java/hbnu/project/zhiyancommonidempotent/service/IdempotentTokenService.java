package hbnu.project.zhiyancommonidempotent.service;

import cn.hutool.core.lang.UUID;
import hbnu.project.zhiyancommonbasic.constants.GlobalConstants;
import hbnu.project.zhiyancommonbasic.exception.ServiceException;
import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import hbnu.project.zhiyancommonredis.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 幂等Token服务
 * 提供Token的生成、验证和消费功能
 *
 * @author yui
 * @rewrite ErgouTree, asddjv
 */
@Slf4j
@Service
public class IdempotentTokenService {

    /**
     * Token前缀
     */
    private static final String IDEMPOTENT_TOKEN_KEY = GlobalConstants.GLOBAL_REDIS_KEY + "idempotent:token:";

    /**
     * 默认过期时间（秒）
     */
    private static final long DEFAULT_TIMEOUT = 300L;

    /**
     * 生成幂等Token
     *
     * @return Token字符串
     */
    public String generateToken() {
        return generateToken(DEFAULT_TIMEOUT);
    }

    /**
     * 生成幂等Token并指定过期时间
     *
     * @param timeout 过期时间（秒）
     * @return Token字符串
     */
    public String generateToken(long timeout) {
        // 生成唯一Token
        String token = UUID.randomUUID().toString(true);
        String redisKey = IDEMPOTENT_TOKEN_KEY + token;

        // 存储到Redis，值为生成时间戳
        RedisUtils.setCacheObject(redisKey, System.currentTimeMillis(), Duration.ofSeconds(timeout));

        log.debug("生成幂等Token: {}, 过期时间: {}秒", token, timeout);
        return token;
    }

    /**
     * 验证Token是否存在（不消费）
     *
     * @param token Token字符串
     * @return true-存在，false-不存在或已过期
     */
    public boolean validateToken(String token) {
        if (StringUtils.isBlank(token)) {
            return false;
        }

        String redisKey = IDEMPOTENT_TOKEN_KEY + token;
        return RedisUtils.hasKey(redisKey);
    }

    /**
     * 验证并消费Token（原子操作）
     * 验证成功后会立即删除Token，确保只能使用一次
     *
     * @param token Token字符串
     * @return true-验证成功并消费，false-Token不存在或已被消费
     */
    public boolean validateAndConsumeToken(String token) {
        if (StringUtils.isBlank(token)) {
            log.warn("Token为空，验证失败");
            return false;
        }

        String redisKey = IDEMPOTENT_TOKEN_KEY + token;
        
        // 使用Redis的原子删除操作，确保只有一个请求能成功删除
        boolean deleted = RedisUtils.deleteObject(redisKey);

        if (deleted) {
            log.debug("Token验证成功并已消费: {}", token);
        } else {
            log.warn("Token验证失败或已被消费: {}", token);
        }

        return deleted;
    }

    /**
     * 验证Token，失败则抛出异常
     *
     * @param token Token字符串
     * @param errorMessage 错误提示信息
     * @throws ServiceException Token验证失败时抛出
     */
    public void validateTokenOrThrow(String token, String errorMessage) {
        if (!validateToken(token)) {
            throw new ServiceException(errorMessage);
        }
    }

    /**
     * 验证并消费Token，失败则抛出异常
     *
     * @param token Token字符串
     * @param errorMessage 错误提示信息
     * @throws ServiceException Token验证或消费失败时抛出
     */
    public void validateAndConsumeTokenOrThrow(String token, String errorMessage) {
        if (!validateAndConsumeToken(token)) {
            throw new ServiceException(errorMessage);
        }
    }

    /**
     * 手动删除Token（取消操作时使用）
     *
     * @param token Token字符串
     * @return true-删除成功，false-Token不存在
     */
    public boolean removeToken(String token) {
        if (StringUtils.isBlank(token)) {
            return false;
        }

        String redisKey = IDEMPOTENT_TOKEN_KEY + token;
        return RedisUtils.deleteObject(redisKey);
    }
}

