package hbnu.project.zhiyansentineldashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流控规则DTO
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "流控规则")
public class FlowRuleDTO {

    @Schema(description = "规则ID", example = "1")
    private Long id;

    @Schema(description = "应用名称", example = "zhiyan-auth")
    private String app;

    @Schema(description = "资源名称", example = "/api/auth/login")
    private String resource;

    @Schema(description = "限流阈值类型（0-线程数 1-QPS）", example = "1")
    private Integer grade;

    @Schema(description = "限流阈值", example = "100")
    private Double count;

    @Schema(description = "流控针对的调用来源", example = "default")
    private String limitApp;

    @Schema(description = "调用关系限流策略（0-直接 1-关联 2-链路）", example = "0")
    private Integer strategy;

    @Schema(description = "关联资源、入口资源", example = "")
    private String refResource;

    @Schema(description = "流控效果（0-快速失败 1-Warm Up 2-排队等待）", example = "0")
    private Integer controlBehavior;

    @Schema(description = "预热时长（秒）", example = "10")
    private Integer warmUpPeriodSec;

    @Schema(description = "排队等待超时时间（毫秒）", example = "500")
    private Integer maxQueueingTimeMs;

    @Schema(description = "是否集群模式", example = "false")
    private Boolean clusterMode;

    @Schema(description = "创建时间")
    private Long gmtCreate;

    @Schema(description = "修改时间")
    private Long gmtModified;
}

