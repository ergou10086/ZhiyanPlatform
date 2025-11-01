package hbnu.project.zhiyanskywalking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 服务拓扑图DTO
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "服务拓扑图")
public class ServiceTopology {

    @Schema(description = "节点列表")
    private List<TopologyNode> nodes;

    @Schema(description = "调用关系列表")
    private List<TopologyCall> calls;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "拓扑节点")
    public static class TopologyNode {

        @Schema(description = "节点ID", example = "zhiyan-auth")
        private String id;

        @Schema(description = "节点名称", example = "智研认证服务")
        private String name;

        @Schema(description = "节点类型", example = "SpringBoot")
        private String type;

        @Schema(description = "是否正常", example = "true")
        private Boolean isReal;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "拓扑调用关系")
    public static class TopologyCall {

        @Schema(description = "源服务", example = "User")
        private String source;

        @Schema(description = "目标服务", example = "zhiyan-auth")
        private String target;

        @Schema(description = "调用次数", example = "100")
        private Long callCount;

        @Schema(description = "检测点", example = "HTTP")
        private String detectPoint;
    }
}

