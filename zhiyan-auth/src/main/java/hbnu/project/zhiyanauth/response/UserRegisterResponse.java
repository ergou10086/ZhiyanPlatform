package hbnu.project.zhiyanauth.response;

import lombok.Builder;
import lombok.Data;

/**
 * 用户注册响应
 */
@Data
@Builder
public class UserRegisterResponse {
    private Long userId;
    private String email;
    private String name;
    private String title;
    private String institution;
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private String tokenType;
    private String passwordStrength; // 新增密码强度信息
}