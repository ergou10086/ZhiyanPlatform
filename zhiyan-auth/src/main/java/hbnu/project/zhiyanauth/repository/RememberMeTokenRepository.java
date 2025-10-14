package hbnu.project.zhiyanauth.repository;

import hbnu.project.zhiyanauth.model.entity.RememberMeToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RememberMeTokenRepository extends JpaRepository<RememberMeToken, Long> {
    
    /**
     * 根据token查找RememberMe token
     */
    Optional<RememberMeToken> findByToken(String token);
    
    /**
     * 根据用户ID删除RememberMe token
     */
    void deleteByUserId(Long userId);
    
    /**
     * 根据用户ID查找RememberMe token
     */
    Optional<RememberMeToken> findByUserId(Long userId);
    
    /**
     * 删除过期的RememberMe token
     */
    @Modifying
    @Query("DELETE FROM RememberMeToken r WHERE r.expiryTime < :expiryTime")
    int deleteByExpiryTimeBefore(@Param("expiryTime") LocalDateTime expiryTime);
}