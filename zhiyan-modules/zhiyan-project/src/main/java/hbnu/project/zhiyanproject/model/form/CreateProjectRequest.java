package hbnu.project.zhiyanproject.model.form;

import hbnu.project.zhiyanproject.model.enums.ProjectVisibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 创建项目请求体
 *
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建项目请求")
public class CreateProjectRequest {

    @NotBlank(message = "项目名称不能为空")
    @Schema(description = "项目名称", required = true, example = "AI智能分析平台")
    private String name;

    @Schema(description = "项目描述", example = "基于深度学习的数据分析平台")
    private String description;

    @Schema(description = "可见性（PUBLIC/PRIVATE/TEAM），默认PUBLIC", example = "PUBLIC")
    private ProjectVisibility visibility;

    @Schema(description = "开始日期", example = "2025-01-01")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @Schema(description = "结束日期", example = "2025-12-31")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
}

