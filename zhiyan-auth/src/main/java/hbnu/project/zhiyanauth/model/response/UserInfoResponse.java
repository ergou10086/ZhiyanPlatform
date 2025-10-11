package hbnu.project.zhiyanauth.model.response;

import lombok.Builder;
import lombok.Data;

/**
 * 用户信息响应体（简化版，用于角色管理中的用户列表）
 *
 * @author Tokito
 */
@Data
@Builder
public class UserInfoResponse {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户姓名
     */
    private String name;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 职称
     */
    private String title;

    /**
     * 所属机构
     */
    private String institution;

    /**
     * 用户状态
     */
    private String status;
}