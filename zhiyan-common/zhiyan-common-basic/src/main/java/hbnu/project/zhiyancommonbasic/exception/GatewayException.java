package hbnu.project.zhiyancommonbasic.exception;

import hbnu.project.zhiyancommonbasic.exception.base.BaseException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.Serial;

/**
 * 网关基础异常类
 * 所有网关异常的父类
 *
 * @author ErgouTree
 */
public class GatewayException extends BaseException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * HTTP状态码
     */
    @Getter
    private HttpStatus httpStatus;

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误信息
     */
    private String message;

    /**
     * 详细错误信息（用于调试）
     */
    @Getter
    private String detailMessage;

    /**
     * 默认构造函数
     */
    public GatewayException() {
        super("网关异常");
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.code = 500;
    }

    /**
     * 带消息的构造函数
     *
     * @param message 错误消息
     */
    public GatewayException(String message) {
        super(message);
        this.message = message;
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.code = 500;
    }

    /**
     * 带消息和HTTP状态码的构造函数
     *
     * @param message    错误消息
     * @param httpStatus HTTP状态码
     */
    public GatewayException(String message, HttpStatus httpStatus) {
        super(message);
        this.message = message;
        this.httpStatus = httpStatus;
        this.code = httpStatus.value();
    }

    /**
     * 带消息、状态码和错误码的构造函数
     *
     * @param message    错误消息
     * @param httpStatus HTTP状态码
     * @param code       业务错误码
     */
    public GatewayException(String message, HttpStatus httpStatus, Integer code) {
        super(message);
        this.message = message;
        this.httpStatus = httpStatus;
        this.code = code;
    }

    /**
     * 带消息和异常原因的构造函数
     *
     * @param message 错误消息
     * @param cause   异常原因
     */
    public GatewayException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.code = 500;
    }

    /**
     * 完整构造函数
     *
     * @param message    错误消息
     * @param httpStatus HTTP状态码
     * @param code       业务错误码
     * @param cause      异常原因
     */
    public GatewayException(String message, HttpStatus httpStatus, Integer code, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.httpStatus = httpStatus;
        this.code = code;
    }

    // Getters and Setters

    public GatewayException setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    public GatewayException setCode(Integer code) {
        this.code = code;
        return this;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public GatewayException setMessage(String message) {
        this.message = message;
        return this;
    }

    public GatewayException setDetailMessage(String detailMessage) {
        this.detailMessage = detailMessage;
        return this;
    }
}
