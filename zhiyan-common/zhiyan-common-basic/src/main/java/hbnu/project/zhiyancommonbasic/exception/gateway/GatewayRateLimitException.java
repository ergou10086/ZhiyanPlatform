package hbnu.project.zhiyancommonbasic.exception.gateway;

import hbnu.project.zhiyancommonbasic.exception.GatewayException;
import org.springframework.http.HttpStatus;

/**
 * 网关限流异常
 * 用于处理请求频率超过限制的情况
 *
 * @author ErgouTree
 */
public class GatewayRateLimitException extends GatewayException {
    private static final long serialVersionUID = 1L;

    public GatewayRateLimitException() {
        super("请求过于频繁，请稍后再试", HttpStatus.TOO_MANY_REQUESTS, 429);
    }

    public GatewayRateLimitException(String message) {
        super(message, HttpStatus.TOO_MANY_REQUESTS, 429);
    }

    public GatewayRateLimitException(String message, Throwable cause) {
        super(message, HttpStatus.TOO_MANY_REQUESTS, 429, cause);
    }

    public GatewayRateLimitException(String message, Integer code) {
        super(message, HttpStatus.TOO_MANY_REQUESTS, code);
    }
}