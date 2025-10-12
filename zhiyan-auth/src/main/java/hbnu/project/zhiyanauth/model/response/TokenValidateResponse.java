package hbnu.project.zhiyanauth.model.response;


import lombok.Data;
/**
 * 令牌验证响应体
 *
 * @author yxy
 */
@Data
public class TokenValidateResponse {
    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 令牌是否有效
     */
    private Boolean isValid;

    /**
     * 验证消息
     */
    private String message;

    /**
     * 令牌剩余有效时间（秒）
     */
    private Long remainingTime;
}
