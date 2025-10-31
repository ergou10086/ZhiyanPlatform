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
 * 任务数据传输对象
 *
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "任务信息")
public class TaskDTO {

    /**
     * 任务ID
     */
    @Schema(description = "任务ID", example = "1977989681735929856")
    private String id;

    /**
     * 项目ID
     */
    @Schema(description = "所属项目ID", example = "1977989681735929856")
    private String projectId;

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
     * 任务优先级
     */
    @Schema(description = "任务优先级", example = "HIGH")
    private TaskPriority priority;

    /**
     * 负责人ID列表
     */
    @Schema(description = "执行者ID列表", example = "[\"1977989681735929856\", \"1977989681735929857\"]")
    private List<String> assigneeIds;

    /**
     * 任务截止日期
     */
    @Schema(description = "任务截止日期", example = "2025-11-30")
    private LocalDate dueDate;

    /**
     * 预估工时（单位：小时）
     */
    @Schema(description = "预估工时（单位：小时）", example = "8.5")
    private java.math.BigDecimal worktime;

    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID", example = "1977989681735929856")
    private String createdBy;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-10-01T10:00:00")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Schema(description = "最后更新时间", example = "2025-10-31T15:30:00")
    private LocalDateTime updatedAt;
}
