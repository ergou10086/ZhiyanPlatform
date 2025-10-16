package hbnu.project.zhiyanproject.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import hbnu.project.zhiyancommonbasic.annotation.LongToString;
import hbnu.project.zhiyancommonbasic.domain.BaseAuditEntity;
import hbnu.project.zhiyancommonbasic.utils.id.SnowflakeIdUtil;
import hbnu.project.zhiyanproject.model.enums.ProjectMemberRole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;

import java.util.List;

/**
 * 项目角色实体类
 * 用于管理项目中的角色权限
 *
 * @author ErgouTree
 */
@Entity
@Table(name = "project_roles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_project_role_name", columnNames = {"project_id", "name"})
        },
        indexes = {
                @Index(name = "idx_project_id", columnList = "project_id"),
                @Index(name = "idx_role_type", columnList = "role_type")
        })
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRole extends BaseAuditEntity {

    /**
     * 雪花id
     */
    @Id
    @LongToString
    @Column(name = "id", nullable = false, columnDefinition = "BIGINT COMMENT '角色唯一标识（雪花ID）'")
    private Long id;

    /**
     * 角色名称
     */
    @Column(name = "name", nullable = false, length = 50,
            columnDefinition = "VARCHAR(50) COMMENT '角色名称（如：项目创建者、项目成员）'")
    private String name;

    /**
     * 角色描述
     */
    @Column(name = "description", columnDefinition = "TEXT COMMENT '角色描述'")
    private String description;

    /**
     * 角色类型 - 固定为PROJECT
     */
    @Column(name = "role_type", nullable = false, length = 20,
            columnDefinition = "VARCHAR(20) DEFAULT 'PROJECT' COMMENT '角色类型（固定为PROJECT）'")
    private String roleType = "PROJECT";

    /**
     * 项目ID（必填）
     */
    @LongToString
    @Column(name = "project_id", nullable = false,
            columnDefinition = "BIGINT COMMENT '项目ID'")
    private Long projectId;

    /**
     * 项目关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private Project project;

    /**
     * 角色枚举类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role_enum", nullable = false,
            columnDefinition = "ENUM('OWNER','MEMBER') COMMENT '角色枚举类型'")
    private ProjectMemberRole roleEnum;

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
     * 项目成员关联（一对多）
     */
    @JsonIgnore
    @OneToMany(mappedBy = "projectRole", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProjectMember> projectMembers;

    /**
     * 在持久化之前生成雪花ID和设置默认值
     */
    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = SnowflakeIdUtil.nextId();
        }
        if (this.roleType == null) {
            this.roleType = "PROJECT";
        }
    }

    /**
     * 检查是否拥有指定权限
     */
    public boolean hasPermission(String permissionCode) {
        if (roleEnum != null) {
            return roleEnum.hasPermission(permissionCode);
        }
        return false;
    }
}
