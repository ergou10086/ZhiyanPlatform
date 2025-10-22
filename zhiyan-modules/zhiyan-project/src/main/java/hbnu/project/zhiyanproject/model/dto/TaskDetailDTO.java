package hbnu.project.zhiyanproject.model.dto;

import hbnu.project.zhiyanproject.model.enums.TaskPriority;
import hbnu.project.zhiyanproject.model.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务详细信息DTO
 * 包含任务信息和关联的用户信息
 *
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDetailDTO {

    /**
     * 任务ID
     */
    private String id;

    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 任务标题
     */
    private String title;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 任务状态
     */
    private TaskStatus status;

    /**
     * 状态显示名称
     */
    private String statusName;

    /**
     * 优先级
     */
    private TaskPriority priority;

    /**
     * 优先级显示名称
     */
    private String priorityName;

    /**
     * 执行者列表
     */
    private List<TaskAssigneeDTO> assignees;

    /**
     * 截止日期
     */
    private LocalDate dueDate;

    /**
     * 预估工时（单位：小时）
     */
    private java.math.BigDecimal worktime;

    /**
     * 是否逾期
     */
    private Boolean isOverdue;

    /**
     * 创建人ID
     */
    private String createdBy;

    /**
     * 创建人姓名
     */
    private String creatorName;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 任务执行者DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskAssigneeDTO {
        /**
         * 用户ID
         */
        private String userId;

        /**
         * 用户姓名
         */
        private String userName;

        /**
         * 用户邮箱
         */
        private String email;

        /**
         * 头像URL
         */
        private String avatarUrl;
    }
}

