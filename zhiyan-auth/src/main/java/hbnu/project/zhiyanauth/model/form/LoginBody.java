package hbnu.project.zhiyanauth.model.form;

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
public class LoginBody {
    
    /**
     * 登录邮箱（替代原用户名，与产品设计保持一致）
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 用户密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * "记住我"选项（新增，支持长效会话）
     */
    private Boolean rememberMe = false;

    /**
     * 验证码（可选，用于二次验证）
     */
    private String verificationCode;
}
