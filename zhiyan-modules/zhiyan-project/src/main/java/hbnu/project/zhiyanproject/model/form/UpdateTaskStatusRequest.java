package hbnu.project.zhiyanproject.model.form;

import hbnu.project.zhiyanproject.model.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新任务状态请求体
 *
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新任务状态请求")
public class UpdateTaskStatusRequest {

    @NotNull(message = "状态不能为空")
    @Schema(description = "任务状态", required = true)
    private TaskStatus status;
}

