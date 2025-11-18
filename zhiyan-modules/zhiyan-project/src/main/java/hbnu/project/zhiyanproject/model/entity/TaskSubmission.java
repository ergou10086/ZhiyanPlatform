package hbnu.project.zhiyanproject.model.entity;

import hbnu.project.zhiyancommonbasic.annotation.LongToString;
import hbnu.project.zhiyancommonbasic.utils.id.SnowflakeIdUtil;
import hbnu.project.zhiyanproject.model.enums.ReviewStatus;
import hbnu.project.zhiyanproject.model.enums.SubmissionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 任务提交记录实体类
 * 用于记录任务的提交、审核、退回历史
 *
 * @author Tokito
 */
@Entity
@Table(name = "task_submission")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskSubmission {

    /**
     * 提交记录ID
     */
    @Id
    @LongToString
    @Column(name = "id", nullable = false)
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
     * 提交人ID（执行者ID）
     */
    @LongToString
    @Column(name = "submitter_id", nullable = false)
    private Long submitterId;

    /**
     * 提交类型：COMPLETE-完成提交，PARTIAL-阶段性提交，MILESTONE-里程碑提交
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "submission_type", nullable = false)
    private SubmissionType submissionType = SubmissionType.COMPLETE;

    /**
     * 提交说明（必填，描述完成情况）
     */
    @Column(name = "submission_content", nullable = false, columnDefinition = "TEXT")
    private String submissionContent;

    /**
     * 附件URL列表（可选，JSON数组格式）
     */
    @Column(name = "attachment_urls", columnDefinition = "JSON")
    private String attachmentUrls;

    /**
     * 提交时间
     */
    @Column(name = "submission_time")
    private Instant submissionTime;

    /**
     * 审核状态：PENDING-待审核，APPROVED-已批准，REJECTED-已拒绝，REVOKED-已撤回
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", nullable = false)
    private ReviewStatus reviewStatus = ReviewStatus.PENDING;

    /**
     * 审核人ID（项目负责人或任务创建者）
     */
    @LongToString
    @Column(name = "reviewer_id")
    private Long reviewerId;

    /**
     * 审核意见（审核人填写）
     */
    @Column(name = "review_comment", columnDefinition = "TEXT")
    private String reviewComment;

    /**
     * 审核时间
     */
    @Column(name = "review_time")
    private Instant reviewTime;

    /**
     * 实际工时（单位：小时，提交时填写）
     */
    @Column(name = "actual_worktime", precision = 10, scale = 2)
    private BigDecimal actualWorktime;

    /**
     * 提交版本号（同一任务可以多次提交，版本号递增）
     */
    @Column(name = "version", nullable = false)
    private Integer version = 1;

    /**
     * 是否为最终提交（TRUE-任务完成的最终提交）
     */
    @Column(name = "is_final", nullable = false)
    private Boolean isFinal = false;

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
     * 是否已删除（软删除标记）
     */
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    /**
     * 在持久化之前生成雪花ID和设置时间戳
     */
    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = SnowflakeIdUtil.nextId();
        }
        Instant now = Instant.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
        if (this.submissionTime == null) {
            this.submissionTime = now;
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