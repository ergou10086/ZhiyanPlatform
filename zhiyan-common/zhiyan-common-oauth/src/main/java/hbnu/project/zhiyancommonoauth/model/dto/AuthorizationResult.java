package hbnu.project.zhiyancommonoauth.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * OAuth2 授权结果
 *
 * @author ErgouTree
 */
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class AuthorizationResult {

    /**
     * 第三方平台的授权URL（用户需跳转至此URL完成授权）
     */
    private String authorizationUrl;

    /**
     * 状态参数（用于防CSRF攻击，需在回调时验证）
     */
    private String state;
}
