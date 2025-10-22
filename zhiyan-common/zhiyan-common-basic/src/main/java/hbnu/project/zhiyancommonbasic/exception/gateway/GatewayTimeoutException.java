package hbnu.project.zhiyancommonbasic.exception.gateway;

import hbnu.project.zhiyancommonbasic.exception.GatewayException;
import org.springframework.http.HttpStatus;

/**
 * 网关超时异常
 * 用于处理请求超时的情况
 *
 * @author ErgouTree
 */
public class GatewayTimeoutException extends GatewayException {
    private static final long serialVersionUID = 1L;

    public GatewayTimeoutException() {
        super("网关请求超时", HttpStatus.GATEWAY_TIMEOUT, 504);
    }

    public GatewayTimeoutException(String message) {
        super(message, HttpStatus.GATEWAY_TIMEOUT, 504);
    }

    public GatewayTimeoutException(String message, Throwable cause) {
        super(message, HttpStatus.GATEWAY_TIMEOUT, 504, cause);
    }

    public GatewayTimeoutException(String message, Integer code) {
        super(message, HttpStatus.GATEWAY_TIMEOUT, code);
    }
}