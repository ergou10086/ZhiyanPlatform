package hbnu.project.zhiyanproject.service.impl;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyanproject.client.AuthServiceClient;
import hbnu.project.zhiyanproject.model.dto.UserDTO;
import hbnu.project.zhiyanproject.service.UserCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户信息缓存服务实现
 * 使用Spring Cache + Redis缓存用户信息
 *
 * @author Tokito
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCacheServiceImpl implements UserCacheService {

    private final AuthServiceClient authServiceClient;

    @Override
    @Cacheable(value = "user:info", key = "#userId", unless = "#result == null || #result.code != 200")
    public R<UserDTO> getUserById(Long userId) {
        log.debug("从认证服务查询用户信息: userId={}", userId);
        return authServiceClient.getUserById(userId);
    }

    @Override
    @Cacheable(value = "user:batch", key = "#userIds.hashCode()", unless = "#result == null || #result.code != 200")
    public R<List<UserDTO>> getUsersByIds(List<Long> userIds) {
        log.debug("从认证服务批量查询用户信息: userIds={}", userIds);
        return authServiceClient.getUsersByIds(userIds);
    }

    @Override
    @CacheEvict(value = "user:info", key = "#userId")
    public void evictUserCache(Long userId) {
        log.debug("清除用户缓存: userId={}", userId);
    }

    @Override
    @CacheEvict(value = {"user:info", "user:batch"}, allEntries = true)
    public void evictAllUserCache() {
        log.debug("清除所有用户缓存");
    }
}

