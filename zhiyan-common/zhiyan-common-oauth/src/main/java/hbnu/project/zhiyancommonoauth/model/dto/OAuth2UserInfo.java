package hbnu.project.zhiyancommonoauth.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * OAuth2用户信息DTO
 * 统一不同OAuth2提供商的用户信息格式
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2UserInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * OAuth2提供商名称（如：github）
     */
    private String provider;

    /**
     * 提供商中的用户ID
     */
    private String providerUserId;

    /**
     * 用户名（登录名）
     */
    private String username;

    /**
     * 昵称/显示名称
     */
    private String nickname;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 访问令牌（临时存储，用于获取额外信息）
     */
    private transient String accessToken;
}
