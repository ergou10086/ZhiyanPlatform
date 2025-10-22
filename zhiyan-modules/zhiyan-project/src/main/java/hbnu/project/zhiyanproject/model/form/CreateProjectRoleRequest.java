package hbnu.project.zhiyanproject.model.form;

import hbnu.project.zhiyanproject.model.enums.ProjectMemberRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建项目角色请求体
 *
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建项目角色请求")
public class CreateProjectRoleRequest {

    @NotNull(message = "角色枚举不能为空")
    @Schema(description = "角色枚举", required = true)
    private ProjectMemberRole roleEnum;

    @Schema(description = "自定义角色名称")
    private String customRoleName;
}

