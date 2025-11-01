package hbnu.project.zhiyanskywalking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 追踪信息DTO
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分布式追踪信息")
public class TraceInfo {

    @Schema(description = "追踪ID", example = "3f2f2f2f2f2f2f2f")
    private String traceId;

    @Schema(description = "服务名称", example = "zhiyan-auth")
    private String serviceName;

    @Schema(description = "端点名称", example = "/api/auth/login")
    private String endpointName;

    @Schema(description = "持续时间（毫秒）", example = "123")
    private Long duration;

    @Schema(description = "开始时间（时间戳）", example = "1730361600000")
    private Long startTime;

    @Schema(description = "是否错误", example = "false")
    private Boolean isError;

    @Schema(description = "Span 列表")
    private List<SpanInfo> spans;
}

