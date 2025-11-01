package hbnu.project.zhiyanskywalking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Span 信息DTO
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Span 信息（单次调用）")
public class SpanInfo {

    @Schema(description = "Span ID", example = "1")
    private String spanId;

    @Schema(description = "父 Span ID", example = "0")
    private String parentSpanId;

    @Schema(description = "服务名称", example = "zhiyan-auth")
    private String serviceName;

    @Schema(description = "端点名称", example = "/api/auth/login")
    private String endpointName;

    @Schema(description = "Span 类型", example = "Entry")
    private String type;

    @Schema(description = "开始时间（时间戳）", example = "1730361600000")
    private Long startTime;

    @Schema(description = "结束时间（时间戳）", example = "1730361600123")
    private Long endTime;

    @Schema(description = "持续时间（毫秒）", example = "123")
    private Long duration;

    @Schema(description = "是否错误", example = "false")
    private Boolean isError;

    @Schema(description = "标签信息")
    private Map<String, String> tags;

    @Schema(description = "日志信息")
    private String logs;
}

