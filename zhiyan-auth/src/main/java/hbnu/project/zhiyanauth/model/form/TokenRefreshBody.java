package hbnu.project.zhiyanauth.model.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 令牌刷新响应体
 *
 * @author yxy
 */
@Data
public class TokenRefreshBody {

    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;


}
