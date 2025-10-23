package hbnu.project.zhiyanproject.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 用户角色信息 DTO
 * 用于返回用户在项目中的角色和权限信息
 *
 * @author AI Assistant
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户角色信息")
public class RoleInfoDTO {

    @Schema(description = "角色代码", example = "OWNER")
    private String roleCode;

    @Schema(description = "角色名称", example = "项目拥有者")
    private String roleName;

    @Schema(description = "角色描述", example = "拥有项目的全部权限")
    private String roleDescription;

    @Schema(description = "权限代码集合")
    private Set<String> permissions;
}

