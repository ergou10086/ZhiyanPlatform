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
 * 任务数据传输对象
 *
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {

    /**
     * 任务ID
     */
    private String id;

    /**
     * 项目ID
     */
    private String projectId;

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
     * 任务优先级
     */
    private TaskPriority priority;

    /**
     * 负责人ID列表
     */
    private List<String> assigneeIds;

    /**
     * 任务截止日期
     */
    private LocalDate dueDate;

    /**
     * 预估工时（单位：小时）
     */
    private java.math.BigDecimal worktime;

    /**
     * 创建人ID
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
