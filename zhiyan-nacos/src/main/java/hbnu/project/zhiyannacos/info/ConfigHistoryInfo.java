package hbnu.project.zhiyannacos.info;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 配置历史信息DTO
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "配置历史信息")
public class ConfigHistoryInfo {

    @Schema(description = "历史ID")
    private Long id;

    @Schema(description = "配置ID")
    private String dataId;

    @Schema(description = "分组")
    private String group;

    @Schema(description = "配置内容")
    private String content;

    @Schema(description = "操作类型", example = "UPDATE")
    private String opType;

    @Schema(description = "操作人")
    private String operator;

    @Schema(description = "操作时间")
    private LocalDateTime createTime;
}
