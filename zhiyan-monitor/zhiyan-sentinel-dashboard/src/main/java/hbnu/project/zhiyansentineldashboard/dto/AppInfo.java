package hbnu.project.zhiyansentineldashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 应用信息DTO
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "应用信息")
public class AppInfo {

    @Schema(description = "应用名称", example = "zhiyan-auth")
    private String app;

    @Schema(description = "应用类型", example = "0")
    private Integer appType;

    @Schema(description = "机器数量", example = "2")
    private Integer machineCount;

    @Schema(description = "健康机器数", example = "2")
    private Integer healthyMachineCount;

    @Schema(description = "最后心跳时间")
    private Long lastFetch;
}

