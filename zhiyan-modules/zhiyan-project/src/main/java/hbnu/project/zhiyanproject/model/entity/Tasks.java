package hbnu.project.zhiyanproject.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import hbnu.project.zhiyancommonbasic.annotation.LongToString;
import hbnu.project.zhiyancommonbasic.domain.BaseAuditEntity;
import hbnu.project.zhiyancommonbasic.utils.id.SnowflakeIdUtil;
import hbnu.project.zhiyanproject.model.enums.TaskPriority;
import hbnu.project.zhiyanproject.model.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;

import java.time.LocalDate;

/**
 * 任务实体类
 *
 * @author ErgouTree
 */
@Entity
@Table(name = "tasks")
@Setter
@Getter
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
     * 注意：此字段仅供JPA内部查询使用，不序列化到JSON响应中
     */
    @JsonIgnore  // ✅ 防止序列化时触发懒加载导致no Session异常
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
     * 预估工时（单位：小时）
     */
    @Column(name = "worktime", columnDefinition = "DECIMAL(10,2) COMMENT '预估工时（单位：小时，支持小数，例如2.5表示2.5小时）'")
    private java.math.BigDecimal worktime;

    /**
     * 任务状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "ENUM('TODO','IN_PROGRESS','BLOCKED','PENDING_REVIEW','DONE') DEFAULT 'TODO' COMMENT '任务状态（待办/进行中/阻塞/待审核/已完成）'")
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
    @Column(name = "assignee_id", nullable = false, columnDefinition = "JSON COMMENT '负责人ID（逻辑关联用户服务的用户ID，可为空表示未分配）JSON类型存储多个负责人ID'")
    private String assigneeId;

    /**
     * 任务截止日期
     */
    @Column(name = "due_date", columnDefinition = "DATE COMMENT '任务截止日期'")
    private LocalDate dueDate;

    /**
     * 任务需要人数，默认单人任务
     */
    @Column(name = "required_people", columnDefinition = "INT DEFAULT 1 COMMENT '任务需要人数'")
    private Integer requiredPeople = 1;

    /**
     * 是否已删除（软删除标记）
     */
    @Column(name = "is_deleted", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0 COMMENT '是否已删除（软删除标记，FALSE为未删除，TRUE为已删除）'")
    private Boolean isDeleted = false;

    /**
     * 是否为里程碑任务
     */
    @Column(name = "is_milestone", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0 COMMENT '是否为里程碑任务（FALSE为普通任务，TRUE为里程碑任务）'")
    private Boolean isMilestone = false;

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