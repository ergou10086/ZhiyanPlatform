package hbnu.project.zhiyanauth.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息响应体
 * 用于返回用户的详细信息，包括基本信息、角色和权限
 *
 * @author Tokito
 * @version 2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户信息响应体")
public class UserInfoResponse {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1977989681735929856")
    private Long id;

    /**
     * 用户邮箱
     */
    @Schema(description = "用户邮箱", example = "user@example.com")
    private String email;

    /**
     * 用户姓名
     */
    @Schema(description = "用户姓名", example = "张三")
    private String name;

    /**
     * 头像URL
     */
    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatarUrl;

    /**
     * 职称
     */
    @Schema(description = "职称", example = "高级工程师")
    private String title;

    /**
     * 所属机构
     */
    @Schema(description = "所属机构", example = "某某大学")
    private String institution;

    /**
     * 用户状态
     */
    @Schema(description = "用户状态", example = "ACTIVE", allowableValues = {"ACTIVE", "LOCKED"})
    private String status;

    /**
     * 用户角色列表
     */
    @Schema(description = "用户角色列表", example = "[\"USER\", \"RESEARCHER\"]")
    private List<String> roles;

    /**
     * 用户权限列表
     */
    @Schema(description = "用户权限列表", example = "[\"profile:manage\", \"project:view\"]")
    private List<String> permissions;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2024-10-14T10:30:00")
    private LocalDateTime createdAt;

    /**
     * 最后更新时间
     */
    @Schema(description = "最后更新时间", example = "2024-10-14T15:45:00")
    private LocalDateTime updatedAt;
}