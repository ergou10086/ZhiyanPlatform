package hbnu.project.zhiyancommonbasic.exception.gateway;

import hbnu.project.zhiyancommonbasic.exception.GatewayException;
import org.springframework.http.HttpStatus;

/**
 * 网关服务不可用异常
 * 用于处理后端服务不可用的情况
 *
 * @author ErgouTree
 */
public class GatewayServiceUnavailableException extends GatewayException {
    private static final long serialVersionUID = 1L;

    public GatewayServiceUnavailableException() {
        super("服务暂时不可用", HttpStatus.SERVICE_UNAVAILABLE, 503);
    }

    public GatewayServiceUnavailableException(String message) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE, 503);
    }

    public GatewayServiceUnavailableException(String message, Throwable cause) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE, 503, cause);
    }

    public GatewayServiceUnavailableException(String message, Integer code) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE, code);
    }
}
