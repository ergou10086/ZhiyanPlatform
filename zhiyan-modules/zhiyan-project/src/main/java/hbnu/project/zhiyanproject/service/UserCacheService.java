package hbnu.project.zhiyanproject.service;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyanproject.model.dto.UserDTO;

import java.util.List;

/**
 * 用户信息缓存服务
 * 用于缓存从认证服务获取的用户信息，减少跨服务调用
 *
 * @author Tokito
 */
public interface UserCacheService {

    /**
     * 根据用户ID查询用户信息（带缓存）
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    R<UserDTO> getUserById(Long userId);

    /**
     * 批量查询用户信息（带缓存）
     *
     * @param userIds 用户ID列表
     * @return 用户信息列表
     */
    R<List<UserDTO>> getUsersByIds(List<Long> userIds);

    /**
     * 清除指定用户的缓存
     *
     * @param userId 用户ID
     */
    void evictUserCache(Long userId);

    /**
     * 清除所有用户缓存
     */
    void evictAllUserCache();
}

