package hbnu.project.zhiyanskywalking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务指标DTO
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "服务性能指标")
public class ServiceMetric {

    @Schema(description = "服务名称", example = "zhiyan-auth")
    private String serviceName;

    @Schema(description = "成功率（%）", example = "99.5")
    private Double successRate;

    @Schema(description = "平均响应时间（毫秒）", example = "123")
    private Long avgResponseTime;

    @Schema(description = "每分钟请求数", example = "1000")
    private Long cpm;

    @Schema(description = "Apdex 分数", example = "0.95")
    private Double apdex;

    @Schema(description = "错误数", example = "5")
    private Long errorCount;

    @Schema(description = "时间戳", example = "1730361600000")
    private Long timestamp;
}

