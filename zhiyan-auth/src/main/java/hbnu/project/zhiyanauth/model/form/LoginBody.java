package hbnu.project.zhiyanauth.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录表单
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户登录请求体")
public class LoginBody {
    
    /**
     * 登录邮箱（替代原用户名，与产品设计保持一致）
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "登录邮箱", example = "user@example.com", required = true)
    private String email;

    /**
     * 用户密码
     */
    @NotBlank(message = "密码不能为空")
    @Schema(description = "用户密码", example = "password123", required = true)
    private String password;

    /**
     * "记住我"选项（新增，支持长效会话）
     */
    @Schema(description = "是否记住我", example = "false", defaultValue = "false")
    private Boolean rememberMe = false;

    /**
     * 验证码（可选，用于二次验证）
     */
    @Schema(description = "验证码（可选，用于二次验证）", example = "123456")
    private String verificationCode;
}
