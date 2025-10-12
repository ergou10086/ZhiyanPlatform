package hbnu.project.zhiyanauth.model.response;

import hbnu.project.zhiyanauth.model.dto.UserDTO;
import lombok.Builder;
import lombok.Data;

/**
 * 用户登录响应
 */
@Data
@Builder
public class UserLoginResponse {
    private UserDTO user;
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private String tokenType;
    private Boolean rememberMe; // 新增记住我状态
}