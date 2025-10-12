package hbnu.project.zhiyanauth.model.form;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 忘记密码表单
 *
 * @author yxy
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordBody {
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

}
