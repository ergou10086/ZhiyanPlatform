package hbnu.project.zhiyansentineldashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统规则DTO
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "系统保护规则")
public class SystemRuleDTO {

    @Schema(description = "规则ID", example = "1")
    private Long id;

    @Schema(description = "应用名称", example = "zhiyan-auth")
    private String app;

    @Schema(description = "Load 阈值", example = "-1")
    private Double highestSystemLoad;

    @Schema(description = "平均RT阈值（毫秒）", example = "1000")
    private Long avgRt;

    @Schema(description = "最大线程数", example = "100")
    private Long maxThread;

    @Schema(description = "入口QPS阈值", example = "1000")
    private Double qps;

    @Schema(description = "CPU使用率阈值（0-1）", example = "0.8")
    private Double highestCpuUsage;

    @Schema(description = "创建时间")
    private Long gmtCreate;

    @Schema(description = "修改时间")
    private Long gmtModified;
}

