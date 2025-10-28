package hbnu.project.zhiyanproject.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户信息数据传输对象
 * 用于Feign客户端接收Auth服务返回的用户信息
 *
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

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
     * 用户职称/职位
     */
    private String title;

    /**
     * 所属机构
     */
    private String institution;

    /**
     * 账号是否锁定
     */
    private Boolean isLocked;

    /**
     * 用户状态
     */
    private String status;

    /**
     * 用户角色列表
     */
    private List<String> roles;

    /**
     * 用户权限列表
     */
    private List<String> permissions;
}

