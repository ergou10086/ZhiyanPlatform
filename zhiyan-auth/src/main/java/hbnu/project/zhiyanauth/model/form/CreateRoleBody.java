package hbnu.project.zhiyanauth.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建角色请求体
 *
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建角色请求体")
public class CreateRoleBody {

    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    @Schema(description = "角色名称", example = "PROJECT_MANAGER", required = true)
    private String name;

    /**
     * 角色描述
     */
    @Size(max = 500, message = "角色描述长度不能超过500个字符")
    @Schema(description = "角色描述", example = "项目管理员角色")
    private String description;

    /**
     * 角色类型（SYSTEM/PROJECT）
     */
    @Schema(description = "角色类型", example = "SYSTEM", allowableValues = {"SYSTEM", "PROJECT"})
    private String roleType;

    /**
     * 项目ID（项目角色时必填）
     */
    @Schema(description = "项目ID（项目角色时必填）", example = "1977989681735929856")
    private Long projectId;

    /**
     * 权限ID列表（可选，创建角色时直接分配权限）
     */
    @Schema(description = "权限ID列表（可选，创建角色时直接分配权限）", example = "[1, 2, 3]")
    private List<Long> permissionIds;
}