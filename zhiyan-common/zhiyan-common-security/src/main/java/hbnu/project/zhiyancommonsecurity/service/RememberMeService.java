package hbnu.project.zhiyancommonsecurity.service;

import java.util.Optional;

public interface RememberMeService {
    String createRememberMeToken(Long userId);

    Optional<Long> validateRememberMeToken(String token);
    
    /**
     * 删除指定的RememberMe token
     */
    void deleteRememberMeToken(String token);
    
    /**
     * 删除用户的RememberMe token
     */
    void deleteRememberMeToken(Long userId);
}
