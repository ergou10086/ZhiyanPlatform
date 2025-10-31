package hbnu.project.zhiyanauth.model.form;

import hbnu.project.zhiyancommonsecurity.xss.Xss;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "用户注册请求体")
public class RegisterBody {

    /**
     * 用户名
     */
    @NotBlank(message = "姓名不能为空")
    @Size(max = 100, message = "姓名长度不能超过100个字符")
    @Xss(message = "姓名不能包含HTML标签或脚本")
    @Schema(description = "用户姓名", example = "张三", required = true)
    private String name;

    /**
     * 用户邮箱（登录账号）
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "用户邮箱（登录账号）", example = "user@example.com", required = true)
    private String email;

    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    @Size(min = 4, max = 6, message = "验证码长度应为4-6位")
    @Schema(description = "邮箱验证码", example = "123456", required = true)
    private String verificationCode;

    /**
     * 用户密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 16, message = "密码长度应为6-16位")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "密码只能包含字母和数字")
    @Schema(description = "用户密码（6-16位字母数字组合）", example = "password123", required = true)
    private String password;

    /**
     * 确认密码
     */
    @NotBlank(message = "确认密码不能为空")
    @Schema(description = "确认密码（需与密码一致）", example = "password123", required = true)
    private String confirmPassword;

    /**
     * 用户职称/职位（可选）
     */
    @Size(max = 100, message = "职称/职位长度不能超过100个字符")
    @Xss(message = "职称/职位不能包含HTML标签或脚本")
    @Schema(description = "用户职称/职位", example = "高级工程师")
    private String title;

    /**
     * 所属机构（可选）
     */
    @Size(max = 200, message = "所属机构长度不能超过200个字符")
    @Xss(message = "所属机构不能包含HTML标签或脚本")
    @Schema(description = "所属机构", example = "某某大学")
    private String institution;


}