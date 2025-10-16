package hbnu.project.zhiyanauth.service.impl;

import hbnu.project.zhiyanauth.model.entity.RememberMeToken;
import hbnu.project.zhiyanauth.repository.RememberMeTokenRepository;
import hbnu.project.zhiyanauth.service.CustomRememberMeService;
import hbnu.project.zhiyancommonsecurity.service.RememberMeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * RememberMe服务实现类
 * 处理RememberMe token的创建、验证和刷新
 * @author yxy
 */
@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class CustomRememberMeServiceImpl implements RememberMeService, CustomRememberMeService {

    @Autowired
    private RememberMeTokenRepository rememberMeTokenRepository;


    private static final int REMEMBER_ME_DAYS = 30;

    /**
     * 创建RememberMe token
     */
    @Override
    @Transactional
    public String createRememberMeToken(Long userId) {
        log.debug("为用户 {} 创建RememberMe token", userId);
        
        // 删除旧 token
        rememberMeTokenRepository.deleteByUserId(userId);

        // 创建新 token
        String token = UUID.randomUUID().toString().replace("-", "");
        RememberMeToken entity = RememberMeToken.builder()
                .userId(userId)
                .token(token)
                .expiryTime(LocalDateTime.now().plusDays(REMEMBER_ME_DAYS))
                .createdTime(LocalDateTime.now())
                .build();

        rememberMeTokenRepository.save(entity);
        log.debug("为用户 {} 创建RememberMe token成功", userId);
        return token;
    }

    /**
     * 验证RememberMe token
     */
    @Override
    public Optional<Long> validateRememberMeToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return Optional.empty();
        }
        
        return rememberMeTokenRepository.findByToken(token)
                .filter(t -> {
                    boolean isValid = t.getExpiryTime().isAfter(LocalDateTime.now());
                    if (!isValid) {
                        log.debug("RememberMe token已过期: {}", token.substring(0, Math.min(token.length(), 10)) + "...");
                    }
                    return isValid;
                })
                .map(t -> {
                    log.debug("RememberMe token验证成功，用户ID: {}", t.getUserId());
                    return t.getUserId();
                });
    }

    /**
     * 刷新RememberMe token过期时间
     */
    @Transactional
    public void refreshRememberMeToken(Long userId) {
        rememberMeTokenRepository.findByUserId(userId).ifPresent(token -> {
            token.setExpiryTime(LocalDateTime.now().plusDays(REMEMBER_ME_DAYS));
            rememberMeTokenRepository.save(token);
            log.debug("刷新用户 {} 的RememberMe token过期时间", userId);
        });
    }

    /**
     * 删除用户的RememberMe token
     */
    @Transactional
    @Override
    public void deleteRememberMeToken(Long userId) {
        rememberMeTokenRepository.deleteByUserId(userId);
        log.debug("删除用户 {} 的RememberMe token", userId);
    }


    /**
     * 删除指定的RememberMe token
     */
    @Transactional
    @Override
    public void deleteRememberMeToken(String token) {
        rememberMeTokenRepository.findByToken(token).ifPresent(entity -> {
            rememberMeTokenRepository.delete(entity);
            log.debug("删除RememberMe token: {}", token.substring(0, Math.min(token.length(), 10)) + "...");
        });
    }

    /**
     * 清理过期的RememberMe token
     */
    @Transactional
    @Override
    public void cleanExpiredTokens() {
        int deletedCount = rememberMeTokenRepository.deleteByExpiryTimeBefore(LocalDateTime.now());
        if (deletedCount > 0) {
            log.info("清理了 {} 个过期的RememberMe token", deletedCount);
        }
    }
}
