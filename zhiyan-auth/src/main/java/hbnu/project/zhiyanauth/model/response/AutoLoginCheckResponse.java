package hbnu.project.zhiyanauth.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 自动登录检查响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoLoginCheckResponse {
    
    /**
     * 是否可以自动登录
     */
    private Boolean canAutoLogin;
    
    /**
     * 用户ID（如果可以自动登录）
     */
    private Long userId;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 创建无token的响应
     */
    public static AutoLoginCheckResponse noToken() {
        return AutoLoginCheckResponse.builder()
                .canAutoLogin(false)
                .message("无有效的RememberMe token")
                .build();
    }
    
    /**
     * 创建有效token的响应
     */
    public static AutoLoginCheckResponse valid(Long userId) {
        return AutoLoginCheckResponse.builder()
                .canAutoLogin(true)
                .userId(userId)
                .message("存在有效的RememberMe token")
                .build();
    }
    
    /**
     * 创建无效token的响应
     */
    public static AutoLoginCheckResponse invalid() {
        return AutoLoginCheckResponse.builder()
                .canAutoLogin(false)
                .message("RememberMe token已过期")
                .build();
    }
}
