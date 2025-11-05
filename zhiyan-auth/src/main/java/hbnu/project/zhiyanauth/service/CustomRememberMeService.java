package hbnu.project.zhiyanauth.service;

public interface CustomRememberMeService {

    String createRememberMeToken(Long userId);
    
    /**
     * 刷新RememberMe token过期时间
     * 在用户活跃时延长token的有效期
     * 
     * @param userId 用户ID
     */
    void refreshRememberMeToken(Long userId);
    
    /**
     * 删除指定的RememberMe token
     */
    void deleteRememberMeToken(String token);
    
    /**
     * 删除用户的RememberMe token
     */
    void deleteRememberMeToken(Long userId);
    
    /**
     * 清理过期的RememberMe token
     */
    void cleanExpiredTokens();
}
