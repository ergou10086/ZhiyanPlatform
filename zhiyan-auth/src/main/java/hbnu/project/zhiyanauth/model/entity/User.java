package hbnu.project.zhiyanauth.model.entity;

import hbnu.project.zhiyancommonbasic.annotation.LongToString;
import hbnu.project.zhiyancommonbasic.utils.id.SnowflakeIdUtil;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.List;


/**
 * 用户实体类
 *
 * @author ErgouTree
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseAuditEntity{

    /**
     * 雪花id
     */
    @Id
    @LongToString
    @Column(name = "id", nullable = false, columnDefinition = "BIGINT COMMENT '用户唯一标识（雪花ID）'")
    private Long id;

    /**
     *  用户邮箱（登录账号）
     */
    @Column(name = "email", nullable = false, unique = true,
            columnDefinition = "VARCHAR(255) COMMENT '用户邮箱（登录账号）'")
    private String email;

    /**
     * 用户密码哈希值
     */
    @Column(name = "password_hash", nullable = false,
            columnDefinition = "VARCHAR(255) COMMENT '密码哈希值（加密存储）'")
    private String passwordHash;

    /**
     * 用户名
     */
    @Column(name = "name", nullable = false, length = 100,
            columnDefinition = "VARCHAR(100) COMMENT '用户姓名'")
    private String name;

    /**
     * 头像URL
     */
    @Column(name = "avatar_url", length = 500,
            columnDefinition = "VARCHAR(500) COMMENT '用户头像URL'")
    private String avatarUrl;

    /**
     * 用户职称/职位
     */
    @Column(name = "title", length = 100,
            columnDefinition = "VARCHAR(100) COMMENT '用户职称/职位'")
    private String title;

    /**
     * 所属机构
     */
    @Column(name = "institution", length = 200,
            columnDefinition = "VARCHAR(200) COMMENT '用户所属机构'")
    private String institution;

    /**
     * 账号是否锁定
     */
    @Column(name = "is_locked", nullable = false,
            columnDefinition = "BOOLEAN DEFAULT FALSE COMMENT '是否锁定（禁止登录）'")
    private Boolean isLocked = false;

    /**
     * 软删除标记
     */
    @Column(name = "is_deleted", nullable = false,
            columnDefinition = "BOOLEAN DEFAULT FALSE COMMENT '软删除标记'")
    private Boolean isDeleted = false;

    /**
     * 用户角色关联（一对多）
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserRole> userRoles;

    /**
     * 在持久化之前生成雪花ID
     */
    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = SnowflakeIdUtil.nextId();
        }
    }
}