package hbnu.project.zhiyanauth.model.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户注册数据传输对象
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterBody {

    /**
     * 用户名
     */
    @NotBlank(message = "姓名不能为空")
    @Size(max = 100, message = "姓名长度不能超过100个字符")
    private String name;

    /**
     * 用户邮箱（登录账号）
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    @Size(min = 4, max = 6, message = "验证码长度应为4-6位")
    private String verificationCode;

    /**
     * 用户密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 16, message = "密码长度应为6-16位")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "密码只能包含字母和数字")
    private String password;

    /**
     * 确认密码
     */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    /**
     * 用户职称/职位（可选）
     */
    @Size(max = 100, message = "职称/职位长度不能超过100个字符")
    private String title;

    /**
     * 所属机构（可选）
     */
    @Size(max = 200, message = "所属机构长度不能超过200个字符")
    private String institution;
}