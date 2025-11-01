package hbnu.project.zhiyansentineldashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 降级规则DTO
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "熔断降级规则")
public class DegradeRuleDTO {

    @Schema(description = "规则ID", example = "1")
    private Long id;

    @Schema(description = "应用名称", example = "zhiyan-auth")
    private String app;

    @Schema(description = "资源名称", example = "/api/auth/login")
    private String resource;

    @Schema(description = "限流应用", example = "default")
    private String limitApp;

    @Schema(description = "熔断策略（0-慢调用比例 1-异常比例 2-异常数）", example = "0")
    private Integer grade;

    @Schema(description = "熔断阈值", example = "1.0")
    private Double count;

    @Schema(description = "熔断时长（秒）", example = "10")
    private Integer timeWindow;

    @Schema(description = "最小请求数", example = "5")
    private Integer minRequestAmount;

    @Schema(description = "慢调用RT（毫秒）", example = "500")
    private Integer slowRatioThreshold;

    @Schema(description = "统计窗口时长（毫秒）", example = "1000")
    private Integer statIntervalMs;

    @Schema(description = "创建时间")
    private Long gmtCreate;

    @Schema(description = "修改时间")
    private Long gmtModified;
}

