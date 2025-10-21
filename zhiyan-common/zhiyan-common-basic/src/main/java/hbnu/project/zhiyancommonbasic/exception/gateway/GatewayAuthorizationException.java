package hbnu.project.zhiyancommonbasic.exception.gateway;

import hbnu.project.zhiyancommonbasic.exception.GatewayException;
import org.springframework.http.HttpStatus;

/**
 * 网关授权异常
 * 用于处理用户权限不足的情况
 *
 * @author ErgouTree
 */
public class GatewayAuthorizationException extends GatewayException {
    private static final long serialVersionUID = 1L;

    public GatewayAuthorizationException() {
        super("权限不足", HttpStatus.FORBIDDEN, 403);
    }

    public GatewayAuthorizationException(String message) {
        super(message, HttpStatus.FORBIDDEN, 403);
    }

    public GatewayAuthorizationException(String message, Throwable cause) {
        super(message, HttpStatus.FORBIDDEN, 403, cause);
    }

    public GatewayAuthorizationException(String message, Integer code) {
        super(message, HttpStatus.FORBIDDEN, code);
    }
}
