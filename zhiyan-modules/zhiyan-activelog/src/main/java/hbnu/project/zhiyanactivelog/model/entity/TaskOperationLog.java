package hbnu.project.zhiyanactivelog.model.entity;

import hbnu.project.zhiyanactivelog.model.enums.OperationResult;
import hbnu.project.zhiyanactivelog.model.enums.TaskOperationType;
import hbnu.project.zhiyancommonbasic.annotation.LongToString;
import hbnu.project.zhiyancommonbasic.utils.id.SnowflakeIdUtil;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 任务操作日志实体类
 *
 * @author ErgouTree
 */
@Entity
@Table(name = "task_operation_log", indexes = {
        @Index(name = "idx_project_id", columnList = "project_id"),
        @Index(name = "idx_task_id", columnList = "task_id"),
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_operation_type", columnList = "operation_type"),
        @Index(name = "idx_operation_time", columnList = "operation_time"),
        @Index(name = "idx_project_task_time", columnList = "project_id, task_id, operation_time"),
        @Index(name = "idx_project_user_time", columnList = "project_id, user_id, operation_time")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TaskOperationLog {

    /**
     * 日志ID（雪花ID）
     */
    @Id
    @LongToString
    @Column(name = "id", nullable = false, columnDefinition = "BIGINT COMMENT '日志ID（雪花ID）'")
    private Long id;

    /**
     * 项目ID
     */
    @LongToString
    @Column(name = "project_id", nullable = false, columnDefinition = "BIGINT COMMENT '项目ID'")
    private Long projectId;

    /**
     * 任务ID（可为空，如批量操作）
     */
    @LongToString
    @Column(name = "task_id", columnDefinition = "BIGINT COMMENT '任务ID（可为空，如批量操作）'")
    private Long taskId;

    /**
     * 任务标题（冗余字段，便于查询）
     */
    @Column(name = "task_title", length = 500, columnDefinition = "VARCHAR(500) COMMENT '任务标题（冗余字段，便于查询）'")
    private String taskTitle;

    /**
     * 操作用户ID
     */
    @LongToString
    @Column(name = "user_id", nullable = false, columnDefinition = "BIGINT COMMENT '操作用户ID'")
    private Long userId;

    /**
     * 用户名（冗余字段，便于查询）
     */
    @Column(name = "username", length = 100, columnDefinition = "VARCHAR(100) COMMENT '用户名（冗余字段，便于查询）'")
    private String username;

    /**
     * 操作类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20) NOT NULL COMMENT '操作类型'")
    private TaskOperationType operationType;

    /**
     * 操作模块
     */
    @Column(name = "operation_module", nullable = false, length = 50, columnDefinition = "VARCHAR(50) NOT NULL DEFAULT '任务管理' COMMENT '操作模块'")
    private String operationModule = "任务管理";

    /**
     * 操作描述
     */
    @Column(name = "operation_desc", length = 500, columnDefinition = "VARCHAR(500) COMMENT '操作描述'")
    private String operationDesc;

    /**
     * 请求参数（JSON格式）
     */
    @Column(name = "request_params", columnDefinition = "JSON COMMENT '请求参数（JSON格式）'")
    private String requestParams;

    /**
     * 操作结果
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "operation_result", nullable = false, length = 20, columnDefinition = "VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' COMMENT '操作结果：SUCCESS-成功, FAILED-失败'")
    private OperationResult operationResult = OperationResult.SUCCESS;

    /**
     * 错误信息（操作失败时记录）
     */
    @Column(name = "error_message", columnDefinition = "TEXT COMMENT '错误信息（操作失败时记录）'")
    private String errorMessage;

    /**
     * 操作IP地址
     */
    @Column(name = "ip_address", length = 50, columnDefinition = "VARCHAR(50) COMMENT '操作IP地址'")
    private String ipAddress;

    /**
     * 用户代理
     */
    @Column(name = "user_agent", length = 500, columnDefinition = "VARCHAR(500) COMMENT '用户代理'")
    private String userAgent;

    /**
     * 操作时间
     */
    @Column(name = "operation_time", nullable = false, columnDefinition = "DATETIME NOT NULL COMMENT '操作时间'")
    private LocalDateTime operationTime;

    /**
     * 在持久化之前生成雪花ID
     */
    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = SnowflakeIdUtil.nextId();
        }
        if (this.operationTime == null) {
            this.operationTime = LocalDateTime.now();
        }
    }
}
