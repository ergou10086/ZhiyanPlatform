package hbnu.project.zhiyanproject.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 权限定义 DTO
 * 用于返回项目权限的定义信息（从枚举中获取）
 *
 * @author AI Assistant
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "项目权限定义")
public class PermissionDefinitionDTO {

    @Schema(description = "权限代码", example = "project:manage")
    private String code;

    @Schema(description = "权限描述", example = "管理项目基本信息")
    private String description;
}

