package hbnu.project.zhiyanproject.model.form;

import hbnu.project.zhiyanproject.model.enums.TaskPriority;
import hbnu.project.zhiyanproject.model.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 更新任务请求体
 *
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新任务请求")
public class UpdateTaskRequest {

    @Schema(description = "任务标题")
    private String title;

    @Schema(description = "任务描述")
    private String description;

    @Schema(description = "任务状态")
    private TaskStatus status;

    @Schema(description = "优先级")
    private TaskPriority priority;

    @Schema(description = "执行者ID列表")
    private List<Long> assigneeIds;

    @Schema(description = "截止日期")
    private LocalDate dueDate;

    @Schema(description = "预估工时（单位：小时，支持小数）")
    private java.math.BigDecimal worktime;
}

