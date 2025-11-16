package hbnu.project.zhiyanauth.model.form;

import hbnu.project.zhiyancommonoauth.model.dto.OAuth2UserInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OAuth2绑定已有账号请求体
 * 用于将OAuth2账号绑定到已有的本地账号
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OAuth2绑定已有账号请求体")
public class OAuth2BindAccountBody {

    /**
     * OAuth2提供商名称
     */
    @NotBlank(message = "提供商名称不能为空")
    @Schema(description = "OAuth2提供商名称", example = "github", required = true)
    private String provider;

    /**
     * OAuth2用户ID（提供商中的用户ID）
     */
    @NotBlank(message = "OAuth2用户ID不能为空")
    @Schema(description = "OAuth2用户ID", required = true)
    private String providerUserId;

    /**
     * OAuth2用户信息（可选，用于更新头像、昵称等）
     */
    @Schema(description = "OAuth2用户信息（可选）")
    private OAuth2UserInfo oauth2UserInfo;

    /**
     * 要绑定的邮箱（已有账号的邮箱）
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "要绑定的邮箱（已有账号的邮箱）", example = "user@example.com", required = true)
    private String email;

    /**
     * 账号密码（用于验证身份）
     */
    @NotBlank(message = "密码不能为空")
    @Schema(description = "账号密码（用于验证身份）", example = "password123", required = true)
    private String password;
}

