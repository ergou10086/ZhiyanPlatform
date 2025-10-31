package hbnu.project.zhiyanauth.model.form;

import hbnu.project.zhiyancommonsecurity.xss.Xss;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户资料更新表单
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户资料更新请求体")
public class UserProfileUpdateBody {

    /**
     * 用户姓名
     */
    @Size(max = 100, message = "姓名长度不能超过100个字符")
    @Xss(message = "姓名不能包含HTML标签或脚本")
    @Schema(description = "用户姓名", example = "张三")
    private String name;

    /**
     * 用户职称/职位
     */
    @Size(max = 100, message = "职称/职位长度不能超过100个字符")
    @Xss(message = "职称/职位不能包含HTML标签或脚本")
    @Schema(description = "用户职称/职位", example = "高级工程师")
    private String title;

    /**
     * 所属机构
     */
    @Size(max = 200, message = "所属机构长度不能超过200个字符")
    @Xss(message = "所属机构不能包含HTML标签或脚本")
    @Schema(description = "所属机构", example = "某某大学")
    private String institution;

    /**
     * 头像URL
     */
    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatarUrl;
}
