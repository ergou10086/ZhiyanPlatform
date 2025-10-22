package hbnu.project.zhiyangateway.model;

import lombok.Data;

/**
 * Token 验证响应
 *
 * @author ErgouTree
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
     * 用户角色（逗号分隔）
     */
    private String roles;

    /**
     * Token 是否有效
     */
    private Boolean isValid;

    /**
     * 验证消息
     */
    private String message;

    /**
     * Token 剩余有效时间（秒）
     */
    private Long remainingTime;

    /**
     * 创建无效响应
     */
    public static TokenValidateResponse invalid(String message) {
        TokenValidateResponse response = new TokenValidateResponse();
        response.setIsValid(false);
        response.setMessage(message);
        return response;
    }

    /**
     * 创建有效响应
     */
    public static TokenValidateResponse valid(String userId, String username, Long remainingTime) {
        TokenValidateResponse response = new TokenValidateResponse();
        response.setIsValid(true);
        response.setUserId(userId);
        response.setUsername(username);
        response.setRemainingTime(remainingTime);
        response.setMessage("Token验证成功");
        return response;
    }
}