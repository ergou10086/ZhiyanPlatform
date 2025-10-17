package hbnu.project.zhiyanproject.model.entity;

import com.fasterxml.jackson.annotation.JsonRawValue;
import hbnu.project.zhiyancommonbasic.annotation.LongToString;
import hbnu.project.zhiyancommonbasic.domain.BaseAuditEntity;
import hbnu.project.zhiyancommonbasic.utils.id.SnowflakeIdUtil;
import hbnu.project.zhiyanproject.model.enums.TaskPriority;
import hbnu.project.zhiyanproject.model.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;

import java.time.LocalDate;

/**
 * 任务实体类
 *
 * @author ErgouTree
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tasks", indexes = {
        @Index(name = "idx_created_by", columnList = "created_by")
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Tasks extends BaseAuditEntity {

    /**
     * 雪花id
     */
    @Id
    @LongToString
    @Column(name = "id", nullable = false, columnDefinition = "BIGINT COMMENT '任务唯一标识（雪花ID）'")
    private Long id;

    /**
     * 所属项目ID
     */
    @LongToString
    @Column(name = "project_id", nullable = false, columnDefinition = "BIGINT COMMENT '所属项目ID（本服务内关联projects表）'")
    private Long projectId;

    /**
     * 关联的项目实体（外键关联）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "tasks_ibfk_1"))
    private Project project;

    /**
     * 任务标题
     */
    @Column(name = "title", nullable = false, length = 200, columnDefinition = "VARCHAR(200) COMMENT '任务标题'")
    private String title;

    /**
     * 任务描述
     */
    @Column(name = "description", columnDefinition = "TEXT COMMENT '任务描述'")
    private String description;

    /**
     * 工时
     */
    @Column(name = "worktime", columnDefinition = "工时")
    private String worktime;

    /**
     * 任务状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "ENUM('TODO','IN_PROGRESS','BLOCKED','DONE') DEFAULT 'TODO' COMMENT '任务状态（待办/进行中/阻塞/已完成）'")
    private TaskStatus status = TaskStatus.TODO;

    /**
     * 任务优先级
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", columnDefinition = "ENUM('HIGH','MEDIUM','LOW') DEFAULT 'MEDIUM' COMMENT '任务优先级（高/中/低）'")
    private TaskPriority priority = TaskPriority.MEDIUM;

    /**
     * 负责人ID（JSON格式存储多个负责人ID）
     */
    @JsonRawValue
    @Column(name = "assignee_id", nullable = false, columnDefinition = "JSON COMMENT '负责人ID（逻辑关联用户服务的用户ID，JSON类型存储多个负责人ID）'")
    private String assigneeId;

    /**
     * 任务截止日期
     */
    @Column(name = "due_date", columnDefinition = "DATE COMMENT '任务截止日期'")
    private LocalDate dueDate;

    /**
     * 创建人ID（逻辑关联用户服务的用户ID）
     */
    @CreatedBy
    @LongToString
    @Column(name = "created_by", nullable = false, columnDefinition = "BIGINT COMMENT '创建人ID（逻辑关联用户服务的用户ID）'")
    private Long createdBy;

    /**
     * 是否已删除（软删除标记）
     */
    @Column(name = "is_deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE COMMENT '是否已删除'")
    private Boolean isDeleted = false;

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