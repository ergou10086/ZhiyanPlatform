package hbnu.project.zhiyannacos.info;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 配置信息DTO
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "配置信息")
public class ConfigInfo {

    @Schema(description = "配置ID", example = "zhiyan-auth-prod.yaml")
    private String dataId;

    @Schema(description = "分组", example = "DEFAULT_GROUP")
    private String group;

    @Schema(description = "配置内容")
    private String content;

    @Schema(description = "MD5值")
    private String md5;

    @Schema(description = "配置类型", example = "yaml")
    private String type;
}
