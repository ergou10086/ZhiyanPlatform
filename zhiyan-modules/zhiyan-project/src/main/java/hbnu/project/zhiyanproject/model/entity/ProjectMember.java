package hbnu.project.zhiyanproject.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import hbnu.project.zhiyancommonbasic.annotation.LongToString;
import hbnu.project.zhiyancommonbasic.utils.id.SnowflakeIdUtil;
import hbnu.project.zhiyanproject.model.enums.ProjectMemberRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 项目成员关系实体类
 *
 * @author Tokito
 */
@Entity
@Table(name = "project_members",
        uniqueConstraints = {
                @UniqueConstraint(name = "project_id", columnNames = {"project_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_project", columnList = "project_id"),
                @Index(name = "idx_user", columnList = "user_id")
        })
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMember {

    /**
     * 雪花id
     */
    @Id
    @LongToString
    @Column(name = "id", nullable = false, columnDefinition = "BIGINT COMMENT '成员记录唯一标识（雪花ID）'")
    private Long id;

    /**
     * 项目ID（逻辑关联projects表）
     */
    @LongToString
    @Column(name = "project_id", nullable = false, columnDefinition = "BIGINT COMMENT '项目ID（逻辑关联projects表）'")
    private Long projectId;

    /**
     * 关联的项目实体（外键关联）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    @JsonIgnore
    private Project project;

    /**
     * 用户ID（逻辑关联users表）
     */
    @LongToString
    @Column(name = "user_id", nullable = false, columnDefinition = "BIGINT COMMENT '用户ID（逻辑关联users表）'")
    private Long userId;

    /**
     * 项目内角色
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "project_role", nullable = false, columnDefinition = "ENUM('OWNER','MEMBER') COMMENT '项目内角色（创建者/普通成员）'")
    private ProjectMemberRole projectRole;


    /**
     * 权限覆盖（JSON格式，用于临时修改成员在项目内的权限）
     */
    @JsonRawValue
    @Column(name = "permissions_override", columnDefinition = "JSON COMMENT '权限覆盖（JSON格式，用于临时修改成员在项目内的权限）'")
    private String permissionsOverride;

    /**
     * 加入项目时间
     */
    @Column(name = "joined_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '加入项目时间'")
    private LocalDateTime joinedAt;

    /**
     * 在保存前设置加入时间和生成雪花ID
     */
    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = SnowflakeIdUtil.nextId();
        }
        if (joinedAt == null) {
            joinedAt = LocalDateTime.now();
        }
    }
}