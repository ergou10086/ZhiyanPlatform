package hbnu.project.zhiyancommonbasic.exception.gateway;

import hbnu.project.zhiyancommonbasic.exception.GatewayException;
import org.springframework.http.HttpStatus;

/**
 * 网关认证异常
 * 用于处理用户认证失败的情况
 *
 * @author ErgouTree
 */
public class GatewayAuthenticationException extends GatewayException {
    private static final long serialVersionUID = 1L;

    public GatewayAuthenticationException() {
        super("认证失败", HttpStatus.UNAUTHORIZED, 401);
    }

    public GatewayAuthenticationException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, 401);
    }

    public GatewayAuthenticationException(String message, Throwable cause) {
        super(message, HttpStatus.UNAUTHORIZED, 401, cause);
    }

    public GatewayAuthenticationException(String message, Integer code) {
        super(message, HttpStatus.UNAUTHORIZED, code);
    }
}
