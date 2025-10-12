package hbnu.project.zhiyanauth.model.response;

import lombok.Builder;
import lombok.Data;

/**
 * 用户注册响应
 * @author Tokito
 */
@Data
@Builder
public class UserRegisterResponse {
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 用户邮箱
     */
    private String email;
    /**
     * 用户姓名
     */
    private String name;
    /**
     * 系统角色
     */
    private String title;
    /**
     * 所属机构
     */
    private String institution;
    /**
     * 访问令牌
     */
    private String accessToken;
    /**
     * 刷新令牌
     */
    private String refreshToken;
    /**
     * 过期时间（秒）
     */
    private Long expiresIn;
    /**
     * 令牌类型
     */
    private String tokenType;
    /**
     * 密码强度
     */
    private String passwordStrength; // 新增密码强度信息
    /**
     * 记住我状态
     */
    private Boolean rememberMe;
}