package hbnu.project.zhiyanauth.response;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TokenRefreshRespone {

    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;


}
