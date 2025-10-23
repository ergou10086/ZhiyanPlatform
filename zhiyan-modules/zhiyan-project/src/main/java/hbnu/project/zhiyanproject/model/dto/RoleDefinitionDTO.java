package hbnu.project.zhiyanproject.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 角色定义 DTO
 * 用于返回项目角色的完整定义信息（从枚举中获取）
 *
 * @author AI Assistant
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "项目角色定义")
public class RoleDefinitionDTO {

    @Schema(description = "角色代码", example = "OWNER")
    private String code;

    @Schema(description = "角色名称", example = "项目拥有者")
    private String name;

    @Schema(description = "角色描述", example = "拥有项目的全部权限")
    private String description;

    @Schema(description = "权限代码列表")
    private List<String> permissions;
}

