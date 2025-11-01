package hbnu.project.zhiyansentineldashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 监控指标DTO
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "实时监控指标")
public class MetricDTO {

    @Schema(description = "应用名称", example = "zhiyan-auth")
    private String app;

    @Schema(description = "资源名称", example = "/api/auth/login")
    private String resource;

    @Schema(description = "时间戳", example = "1730361600000")
    private Long timestamp;

    @Schema(description = "通过QPS", example = "100")
    private Long passQps;

    @Schema(description = "成功QPS", example = "98")
    private Long successQps;

    @Schema(description = "限流QPS", example = "2")
    private Long blockQps;

    @Schema(description = "异常QPS", example = "0")
    private Long exceptionQps;

    @Schema(description = "平均RT（毫秒）", example = "50")
    private Double rt;

    @Schema(description = "并发线程数", example = "10")
    private Long occupiedPassQps;

    @Schema(description = "分类", example = "1")
    private Integer classification;
}

