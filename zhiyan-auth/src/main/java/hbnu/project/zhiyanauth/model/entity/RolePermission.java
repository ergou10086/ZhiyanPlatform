package hbnu.project.zhiyanauth.model.entity;

import hbnu.project.zhiyancommonbasic.annotation.LongToString;
import hbnu.project.zhiyancommonbasic.utils.id.SnowflakeIdUtil;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 角色权限关联实体类
 *
 * @author ErgouTree
 */
@Entity
@Table(name = "role_permissions",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_ROLE_PERMISSION",
                columnNames = {"role_id", "permission_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolePermission {

    /**
     * 雪花id
     */
    @Id
    @LongToString
    @Column(name = "id", nullable = false, columnDefinition = "BIGINT COMMENT '关联记录唯一标识（雪花ID）'")
    private Long id;

    /**
     * 关联的角色对象
     * 多对一关系：多个角色权限关联记录可以对应同一个角色
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_ROLE_PERMISSION_ROLE"))
    private Role role;

    /**
     * 关联的权限对象
     * 多对一关系：多个角色权限关联记录可以对应同一个权限
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_ROLE_PERMISSION_PERMISSION"))
    private Permission permission;

    /**
     * 权限授予时间
     * 记录该角色被授予此权限的具体时间
     */
    @CreationTimestamp
    @Column(name = "granted_at", updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '权限授予时间'")
    private LocalDateTime grantedAt;

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