package hbnu.project.zhiyanauth.model.entity;

import hbnu.project.zhiyancommonbasic.annotation.LongToString;
import hbnu.project.zhiyancommonbasic.utils.id.SnowflakeIdUtil;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色实体类
 *
 * @author ErgouTree
 */
@Entity
@Table(name = "roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role extends BaseAuditEntity{

    /**
     * 雪花id
     */
    @Id
    @LongToString
    @Column(name = "id", nullable = false, columnDefinition = "BIGINT COMMENT '角色唯一标识（雪花ID）'")
    private Long id;

    /**
     * 角色名称（
     */
    @Column(name = "name", nullable = false, unique = true, length = 50,
            columnDefinition = "VARCHAR(50) COMMENT '角色名称（如：ADMIN、USER）'")
    private String name;

    /**
     * 角色描述
     */
    @Column(name = "description", columnDefinition = "TEXT COMMENT '角色描述（如：系统管理员、普通用户）'")
    private String description;

    /**
     * 角色类型 - 使用字符串存储
     */
    @Column(name = "role_type", nullable = false, length = 20,
            columnDefinition = "VARCHAR(20) COMMENT '角色类型（SYSTEM/PROJECT）'")
    private String roleType;

    /**
     * 项目ID（项目角色时使用）
     */
    @Column(name = "project_id",
            columnDefinition = "BIGINT COMMENT '项目ID（项目角色时使用）'")
    private Long projectId;

    /**
     * 是否为系统默认角色
     */
    @Column(name = "is_system_default", nullable = false,
            columnDefinition = "BOOLEAN DEFAULT FALSE COMMENT '是否为系统默认角色'")
    private Boolean isSystemDefault = false;

    /**
     * 数据创建人（由审计自动填充）
     */
    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    /**
     * 角色用户关联（一对多）
     */
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserRole> userRoles;

    /**
     * 角色权限关联（一对多）
     */
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
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
