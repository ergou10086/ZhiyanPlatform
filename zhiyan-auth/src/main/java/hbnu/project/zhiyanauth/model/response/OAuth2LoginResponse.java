package hbnu.project.zhiyanauth.model.response;

import hbnu.project.zhiyancommonoauth.model.dto.OAuth2UserInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OAuth2登录响应
 * 根据不同的登录状态返回不同的响应信息
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OAuth2登录响应")
public class OAuth2LoginResponse {

    /**
     * 登录状态
     * SUCCESS - 登录成功
     * NEED_BIND - 需要绑定已有账号
     * NEED_SUPPLEMENT - 需要补充信息（邮箱、密码）创建账号
     */
    @Schema(description = "登录状态", example = "SUCCESS", required = true)
    private OAuth2LoginStatus status;

    /**
     * 登录成功时的响应（status=SUCCESS时使用）
     */
    @Schema(description = "登录成功响应")
    private UserLoginResponse loginResponse;

    /**
     * OAuth2用户信息（status=NEED_BIND或NEED_SUPPLEMENT时使用）
     */
    @Schema(description = "OAuth2用户信息")
    private OAuth2UserInfo oauth2UserInfo;

    /**
     * 提示信息
     */
    @Schema(description = "提示信息", example = "请绑定已有账号或创建新账号")
    private String message;

    /**
     * 登录状态枚举
     */
    public enum OAuth2LoginStatus {
        /**
         * 登录成功
         */
        SUCCESS,

        /**
         * 需要绑定已有账号
         * 当OAuth2邮箱匹配到已有账号，但需要验证密码绑定时
         */
        NEED_BIND,

        /**
         * 需要补充信息创建账号
         * 当OAuth2信息不足（如缺少邮箱）时，需要用户补充邮箱和密码
         */
        NEED_SUPPLEMENT
    }

    /**
     * 创建登录成功响应
     */
    public static OAuth2LoginResponse success(UserLoginResponse loginResponse) {
        return OAuth2LoginResponse.builder()
                .status(OAuth2LoginStatus.SUCCESS)
                .loginResponse(loginResponse)
                .message("登录成功")
                .build();
    }

    /**
     * 创建需要绑定账号的响应
     * 当OAuth2邮箱未匹配到账号时，引导用户绑定已有账号
     */
    public static OAuth2LoginResponse needBind(OAuth2UserInfo oauth2UserInfo, String email) {
        return OAuth2LoginResponse.builder()
                .status(OAuth2LoginStatus.NEED_BIND)
                .oauth2UserInfo(oauth2UserInfo)
                .message("检测到邮箱 " + email + " 未注册，请绑定已有账号或创建新账号")
                .build();
    }

    /**
     * 创建需要补充信息的响应
     */
    public static OAuth2LoginResponse needSupplement(OAuth2UserInfo oauth2UserInfo) {
        String message = "请补充必要信息创建账号";
        if (oauth2UserInfo.getEmail() == null || oauth2UserInfo.getEmail().isEmpty()) {
            message = "请补充邮箱和密码创建账号";
        }
        return OAuth2LoginResponse.builder()
                .status(OAuth2LoginStatus.NEED_SUPPLEMENT)
                .oauth2UserInfo(oauth2UserInfo)
                .message(message)
                .build();
    }
}

