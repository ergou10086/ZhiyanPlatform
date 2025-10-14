package hbnu.project.zhiyanauth.model.response;

import hbnu.project.zhiyanauth.model.dto.UserDTO;
import lombok.Builder;
import lombok.Data;

/**
 * 用户登录响应
 * @author yxy
 */
@Data
@Builder
public class UserLoginResponse {
    /**
     * 用户信息
     */
    private UserDTO user;
    /**
     * 访问令牌
     */
    private String accessToken;
    /**
     * 刷新令牌
     */
    private String refreshToken;
    /**
     * 令牌过期时间（秒）
     */
    private Long expiresIn;
    /**
     * 令牌类型
     */
    private String tokenType;
    /**
     * 是否记住我
     */
    private Boolean rememberMe; // 新增记住我状态

    /**
     * 记住我令牌
     */
    private String rememberMeToken;
}
