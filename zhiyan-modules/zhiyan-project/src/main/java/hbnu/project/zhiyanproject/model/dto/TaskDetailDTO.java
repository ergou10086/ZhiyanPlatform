package hbnu.project.zhiyanproject.model.dto;

import hbnu.project.zhiyanproject.model.enums.TaskPriority;
import hbnu.project.zhiyanproject.model.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "任务详细信息")
public class TaskDetailDTO {

    /**
     * 任务ID
     */
    @Schema(description = "任务ID", example = "1977989681735929856")
    private String id;

    /**
     * 项目ID
     */
    @Schema(description = "项目ID", example = "1977989681735929856")
    private String projectId;

    /**
     * 项目名称
     */
    @Schema(description = "项目名称", example = "AI智能分析平台")
    private String projectName;

    /**
     * 任务标题
     */
    @Schema(description = "任务标题", example = "设计数据库模型")
    private String title;

    /**
     * 任务描述
     */
    @Schema(description = "任务描述", example = "设计用户表和项目表的关系模型")
    private String description;

    /**
     * 任务状态
     */
    @Schema(description = "任务状态", example = "IN_PROGRESS")
    private TaskStatus status;

    /**
     * 状态显示名称
     */
    @Schema(description = "状态显示名称", example = "进行中")
    private String statusName;

    /**
     * 优先级
     */
    @Schema(description = "任务优先级", example = "HIGH")
    private TaskPriority priority;

    /**
     * 优先级显示名称
     */
    @Schema(description = "优先级显示名称", example = "高")
    private String priorityName;

    /**
     * 执行者列表
     */
    @Schema(description = "任务执行者列表")
    private List<TaskAssigneeDTO> assignees;

    /**
     * 截止日期
     */
    @Schema(description = "任务截止日期", example = "2025-11-30")
    private LocalDate dueDate;

    /**
     * 预估工时（单位：小时）
     */
    @Schema(description = "预估工时（单位：小时）", example = "8.5")
    private java.math.BigDecimal worktime;

    /**
     * 是否逾期
     */
    @Schema(description = "是否逾期", example = "false")
    private Boolean isOverdue;

    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID", example = "1977989681735929856")
    private String createdBy;

    /**
     * 创建人姓名
     */
    @Schema(description = "创建人姓名", example = "张三")
    private String creatorName;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-10-01T10:00:00")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2025-10-31T15:30:00")
    private LocalDateTime updatedAt;

    /**
     * 任务执行者DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "任务执行者信息")
    public static class TaskAssigneeDTO {
        /**
         * 用户ID
         */
        @Schema(description = "用户ID", example = "1977989681735929856")
        private String userId;

        /**
         * 用户姓名
         */
        @Schema(description = "用户姓名", example = "张三")
        private String userName;

        /**
         * 用户邮箱
         */
        @Schema(description = "用户邮箱", example = "user@example.com")
        private String email;

        /**
         * 头像URL
         */
        @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
        private String avatarUrl;
    }
}

