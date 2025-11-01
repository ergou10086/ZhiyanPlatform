package hbnu.project.zhiyanskywalking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务信息DTO
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "服务信息")
public class ServiceInfo {

    @Schema(description = "服务ID", example = "zhiyan-auth")
    private String id;

    @Schema(description = "服务名称", example = "zhiyan-auth")
    private String name;

    @Schema(description = "服务所属层级", example = "SpringBoot")
    private String layer;

    @Schema(description = "是否正常", example = "true")
    private Boolean normal;
}

