package hbnu.project.zhiyanproject.model.entity;

import hbnu.project.zhiyancommonbasic.annotation.LongToString;
import hbnu.project.zhiyanproject.model.enums.AssignType;
import hbnu.project.zhiyanproject.model.enums.RoleType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * 任务用户关联实体类
 * 用于记录任务的执行者、分配历史、接取记录
 *
 * @author System
 */
@Entity
@Table(name = "task_user")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskUser {

    /**
     * 关联记录ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @LongToString
    @Column(name = "id")
    private Long id;

    /**
     * 任务ID
     */
    @LongToString
    @Column(name = "task_id", nullable = false)
    private Long taskId;

    /**
     * 项目ID（冗余字段，提高查询性能）
     */
    @LongToString
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    /**
     * 用户ID（执行者ID）
     */
    @LongToString
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 分配类型：ASSIGNED-被管理员分配，CLAIMED-用户主动接取
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "assign_type", nullable = false)
    private AssignType assignType;

    /**
     * 分配人ID（如果是CLAIMED则为user_id本身）
     */
    @LongToString
    @Column(name = "assigned_by", nullable = false)
    private Long assignedBy;

    /**
     * 分配/接取时间
     */
    @Column(name = "assigned_at")
    private Instant assignedAt;

    /**
     * 是否有效（TRUE-有效执行者，FALSE-已移除）
     */
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * 移除时间（仅当is_active=FALSE时有值）
     */
    @Column(name = "removed_at")
    private Instant removedAt;

    /**
     * 移除操作人ID（仅当is_active=FALSE时有值）
     */
    @LongToString
    @Column(name = "removed_by")
    private Long removedBy;

    /**
     * 角色类型：EXECUTOR-执行者，FOLLOWER-关注者，REVIEWER-审核者
     * 注意：前端暂未实现，暂时不使用，默认都是执行者
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "role_type")
    private RoleType roleType = RoleType.EXECUTOR;  // 暂时不使用，保留字段

    /**
     * 备注信息（可记录分配原因等）
     */
    @Column(name = "notes", length = 500)
    private String notes;

    /**
     * 记录创建时间
     */
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    /**
     * 记录更新时间
     */
    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * 在持久化之前设置时间戳
     */
    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
        if (this.assignedAt == null) {
            this.assignedAt = now;
        }
    }

    /**
     * 在更新之前设置更新时间
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}

