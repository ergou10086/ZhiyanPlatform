package hbnu.project.zhiyanauth.service;

public interface CustomRememberMeService {

    String createRememberMeToken(Long userId);
    
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
