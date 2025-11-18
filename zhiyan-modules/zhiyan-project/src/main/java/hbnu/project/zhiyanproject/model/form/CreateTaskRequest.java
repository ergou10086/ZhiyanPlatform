package hbnu.project.zhiyanproject.model.form;

import hbnu.project.zhiyanproject.model.enums.TaskPriority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 创建任务请求体
 *
 * @author Tokito
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建任务请求")
public class CreateTaskRequest {

    @NotNull(message = "项目ID不能为空")
    @Schema(description = "项目ID", required = true)
    private Long projectId;

    @NotBlank(message = "任务标题不能为空")
    @Schema(description = "任务标题", required = true, example = "设计数据库模型")
    private String title;

    @Schema(description = "任务描述")
    private String description;

    @Schema(description = "任务需要人数")
    @Min(value = 1, message = "任务需要人数至少为1")
    private Integer requiredPeople = 1;

    @Schema(description = "执行者ID列表（从项目成员中选择）")
    private List<Long> assigneeIds;

    @Schema(description = "优先级（HIGH/MEDIUM/LOW），默认MEDIUM")
    private TaskPriority priority;

    @Schema(description = "截止日期")
    private LocalDate dueDate;

    @Schema(description = "预估工时（单位：小时，支持小数）")
    private java.math.BigDecimal worktime;
}

