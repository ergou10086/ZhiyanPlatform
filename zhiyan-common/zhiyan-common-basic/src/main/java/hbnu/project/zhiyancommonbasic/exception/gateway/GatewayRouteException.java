package hbnu.project.zhiyancommonbasic.exception.gateway;

import hbnu.project.zhiyancommonbasic.exception.GatewayException;
import org.springframework.http.HttpStatus;

/**
 * 网关路由异常
 * 用于处理路由不存在或路由配置错误的情况
 *
 * @author ErgouTree
 */
public class GatewayRouteException extends GatewayException {
    private static final long serialVersionUID = 1L;

    public GatewayRouteException() {
        super("路由不存在", HttpStatus.NOT_FOUND, 404);
    }

    public GatewayRouteException(String message) {
        super(message, HttpStatus.NOT_FOUND, 404);
    }

    public GatewayRouteException(String message, Throwable cause) {
        super(message, HttpStatus.NOT_FOUND, 404, cause);
    }

    public GatewayRouteException(String message, Integer code) {
        super(message, HttpStatus.NOT_FOUND, code);
    }
}
