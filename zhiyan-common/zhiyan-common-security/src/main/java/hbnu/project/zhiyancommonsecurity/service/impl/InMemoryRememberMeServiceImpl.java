package hbnu.project.zhiyancommonsecurity.service.impl;

import hbnu.project.zhiyancommonsecurity.service.RememberMeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于内存的 RememberMe 服务实现（默认实现）
 * 
 * 当 Redis 不可用时，使用此实现作为后备方案
 * 
 * 特性：
 * - 使用 ConcurrentHashMap 存储 token
 * - 线程安全
 * - 应用重启后数据丢失
 * 
 * 注意：
 * - 仅用于开发/测试环境或 Redis 不可用的情况
 * - 生产环境建议使用 RedisRememberMeServiceImpl
 * - 不支持分布式部署（多实例间 token 不共享）
 * 
 * @author AI Assistant
 * @version 1.0
 */
@Slf4j
@Service("inMemoryRememberMeService")
@ConditionalOnMissingBean(name = "customRememberMeServiceImpl")
public class InMemoryRememberMeServiceImpl implements RememberMeService {

    /**
     * 存储 token -> TokenInfo 的映射
     */
    private final Map<String, TokenInfo> tokenStore = new ConcurrentHashMap<>();
    
    /**
     * 存储 userId -> token 的映射（用于快速删除）
     */
    private final Map<Long, String> userTokenStore = new ConcurrentHashMap<>();

    /**
     * RememberMe Token 默认有效期（天）
     */
    private static final int REMEMBER_ME_DAYS = 30;

    /**
     * Token 信息内部类
     */
    private static class TokenInfo {
        final Long userId;
        final LocalDateTime expiryTime;

        TokenInfo(Long userId, LocalDateTime expiryTime) {
            this.userId = userId;
            this.expiryTime = expiryTime;
        }

        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }
    }

    public InMemoryRememberMeServiceImpl() {
        log.warn("使用内存 RememberMe 服务实现（开发/测试模式）");
        log.warn("生产环境请配置 Redis 以使用 RedisRememberMeServiceImpl");
    }

    /**
     * 创建 RememberMe Token
     * 
     * @param userId 用户ID
     * @return 生成的 Token 字符串
     */
    @Override
    public String createRememberMeToken(Long userId) {
        log.debug("为用户 {} 创建 RememberMe Token（内存存储）", userId);
        
        try {
            // 1. 删除该用户的旧 Token
            deleteRememberMeToken(userId);

            // 2. 生成新的 Token
            String token = UUID.randomUUID().toString().replace("-", "");

            // 3. 计算过期时间
            LocalDateTime expiryTime = LocalDateTime.now().plusDays(REMEMBER_ME_DAYS);

            // 4. 存储 token 信息
            TokenInfo tokenInfo = new TokenInfo(userId, expiryTime);
            tokenStore.put(token, tokenInfo);
            userTokenStore.put(userId, token);

            log.info("为用户 {} 创建 RememberMe Token 成功（内存存储），有效期: {} 天", userId, REMEMBER_ME_DAYS);
            return token;
            
        } catch (Exception e) {
            log.error("创建 RememberMe Token 失败 - userId: {}, 错误: {}", userId, e.getMessage(), e);
            throw new RuntimeException("创建 RememberMe Token 失败", e);
        }
    }

    /**
     * 验证 RememberMe Token
     * 
     * @param token RememberMe Token
     * @return Optional<Long> 如果 Token 有效，返回对应的用户ID；否则返回空
     */
    @Override
    public Optional<Long> validateRememberMeToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.debug("RememberMe Token 为空");
            return Optional.empty();
        }

        try {
            // 清理过期的 token
            cleanExpiredTokens();

            TokenInfo tokenInfo = tokenStore.get(token);

            if (tokenInfo == null) {
                log.debug("RememberMe Token 不存在: {}", maskToken(token));
                return Optional.empty();
            }

            if (tokenInfo.isExpired()) {
                log.debug("RememberMe Token 已过期: {}", maskToken(token));
                // 删除过期的 token
                deleteRememberMeToken(token);
                return Optional.empty();
            }

            log.debug("RememberMe Token 验证成功 - userId: {}", tokenInfo.userId);
            return Optional.of(tokenInfo.userId);
            
        } catch (Exception e) {
            log.error("验证 RememberMe Token 失败 - token: {}, 错误: {}", 
                maskToken(token), e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * 删除指定的 RememberMe Token
     * 
     * @param token RememberMe Token
     */
    @Override
    public void deleteRememberMeToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return;
        }

        try {
            TokenInfo tokenInfo = tokenStore.remove(token);
            
            if (tokenInfo != null) {
                userTokenStore.remove(tokenInfo.userId);
                log.debug("删除 RememberMe Token: {}", maskToken(token));
            }
                
        } catch (Exception e) {
            log.error("删除 RememberMe Token 失败 - token: {}, 错误: {}", 
                maskToken(token), e.getMessage(), e);
        }
    }

    /**
     * 删除用户的 RememberMe Token
     * 
     * @param userId 用户ID
     */
    @Override
    public void deleteRememberMeToken(Long userId) {
        if (userId == null) {
            return;
        }

        try {
            String token = userTokenStore.remove(userId);
            
            if (token != null) {
                tokenStore.remove(token);
                log.debug("删除用户 {} 的 RememberMe Token", userId);
            }
            
        } catch (Exception e) {
            log.error("删除用户 RememberMe Token 失败 - userId: {}, 错误: {}", 
                userId, e.getMessage(), e);
        }
    }

    /**
     * 清理过期的 Token
     * 定期清理，避免内存泄漏
     */
    private void cleanExpiredTokens() {
        try {
            tokenStore.entrySet().removeIf(entry -> {
                if (entry.getValue().isExpired()) {
                    userTokenStore.remove(entry.getValue().userId);
                    return true;
                }
                return false;
            });
        } catch (Exception e) {
            log.warn("清理过期 Token 失败: {}", e.getMessage());
        }
    }

    /**
     * 辅助方法：遮罩 Token，避免在日志中暴露完整 Token
     * 
     * @param token 原始 Token
     * @return 遮罩后的 Token
     */
    private String maskToken(String token) {
        if (token == null) {
            return "null";
        }
        int length = Math.min(token.length(), 10);
        return token.substring(0, length) + "...";
    }

    /**
     * 获取当前存储的 Token 数量（用于监控）
     * 
     * @return Token 数量
     */
    public int getTokenCount() {
        return tokenStore.size();
    }
}

