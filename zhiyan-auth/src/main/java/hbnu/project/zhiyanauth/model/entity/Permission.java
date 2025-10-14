package hbnu.project.zhiyanauth.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import hbnu.project.zhiyancommonbasic.annotation.LongToString;
import hbnu.project.zhiyancommonbasic.utils.id.SnowflakeIdUtil;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

/**
 * 权限实体类
 *
 * @author ErgouTree
 */
@Entity
@Table(name = "permissions")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class) // 启用 JPA 审计
public class Permission extends BaseAuditEntity{

    /**
     * 雪花id
     */
    @Id
    @LongToString
    @Column(name = "id", nullable = false, columnDefinition = "BIGINT COMMENT '权限唯一标识'")
    private Long id;

    /**
     * 权限名称
     */
    @Column(name = "name", nullable = false, unique = true, length = 100,
            columnDefinition = "VARCHAR(100) COMMENT '权限名称'")
    private String name;

    /**
     * 权限描述
     */
    @Column(name = "description", columnDefinition = "TEXT COMMENT '权限描述'")
    private String description;


    /**
     * 角色权限关联（一对多）
     */
    @JsonIgnore
    @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RolePermission> rolePermissions;


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
