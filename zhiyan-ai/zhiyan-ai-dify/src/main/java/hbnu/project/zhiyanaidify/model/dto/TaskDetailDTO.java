package hbnu.project.zhiyanaidify.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务详情DTO（AI 模块本地拷贝）
 * 与 zhiyan-project 模块的 TaskDetailDTO 在字段名/JSON 结构上保持兼容，
 * 但类型尽量使用基础类型，避免直接依赖后端枚举。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDetailDTO {

    /** 任务ID */
    private String id;

    /** 项目ID */
    private String projectId;

    /** 项目名称 */
    private String projectName;

    /** 任务标题 */
    private String title;

    /** 任务描述 */
    private String description;

    /** 状态枚举名，例如 IN_PROGRESS/DONE */
    private String status;

    /** 状态显示名称，例如 进行中/已完成 */
    private String statusName;

    /** 优先级枚举名，例如 HIGH/MEDIUM/LOW */
    private String priority;

    /** 优先级显示名称 */
    private String priorityName;

    /** 执行者列表（只保留AI可能用到的字段） */
    private List<TaskAssigneeDTO> assignees;

    /** 截止日期 */
    private LocalDate dueDate;

    /** 预估工时（字符串或数字都能被Jackson转换） */
    private String worktime;

    /** 是否逾期 */
    private Boolean isOverdue;

    /** 创建人ID */
    private String createdBy;

    /** 创建人姓名 */
    private String creatorName;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskAssigneeDTO {
        private String userId;
        private String userName;
        private String email;
        private String avatarUrl;
        private String assignType;
    }
}
