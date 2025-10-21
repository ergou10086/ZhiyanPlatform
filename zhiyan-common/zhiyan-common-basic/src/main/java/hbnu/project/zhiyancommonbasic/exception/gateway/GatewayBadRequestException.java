package hbnu.project.zhiyancommonbasic.exception.gateway;

import hbnu.project.zhiyancommonbasic.exception.GatewayException;
import org.springframework.http.HttpStatus;

/**
 * 网关错误请求异常
 * 用于处理客户端请求参数错误的情况
 *
 * @author ErgouTree
 */
public class GatewayBadRequestException extends GatewayException {
    private static final long serialVersionUID = 1L;

    public GatewayBadRequestException() {
        super("错误的请求", HttpStatus.BAD_REQUEST, 400);
    }

    public GatewayBadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST, 400);
    }

    public GatewayBadRequestException(String message, Throwable cause) {
        super(message, HttpStatus.BAD_REQUEST, 400, cause);
    }

    public GatewayBadRequestException(String message, Integer code) {
        super(message, HttpStatus.BAD_REQUEST, code);
    }
}
