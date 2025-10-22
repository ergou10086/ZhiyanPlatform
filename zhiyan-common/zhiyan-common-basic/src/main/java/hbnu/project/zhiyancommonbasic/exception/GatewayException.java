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
     * 错误码（数字类型）
     */
    private Integer errorCode;

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
        this.errorCode = 500;
    }

    /**
     * 带消息的构造函数
     *
     * @param message 错误消息
     */
    public GatewayException(String message) {
        super(message);
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.errorCode = 500;
    }

    /**
     * 带消息和HTTP状态码的构造函数
     *
     * @param message    错误消息
     * @param httpStatus HTTP状态码
     */
    public GatewayException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = httpStatus.value();
    }

    /**
     * 带消息、状态码和错误码的构造函数
     *
     * @param message    错误消息
     * @param httpStatus HTTP状态码
     * @param errorCode       业务错误码
     */
    public GatewayException(String message, HttpStatus httpStatus, Integer errorCode) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    /**
     * 带消息和异常原因的构造函数
     *
     * @param message 错误消息
     * @param cause   异常原因
     */
    public GatewayException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.errorCode = 500;
    }

    /**
     * 完整构造函数
     *
     * @param message    错误消息
     * @param httpStatus HTTP状态码
     * @param errorCode       业务错误码
     * @param cause      异常原因
     */
    public GatewayException(String message, HttpStatus httpStatus, Integer errorCode, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    // Getters and Setters

    public GatewayException setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public GatewayException setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public GatewayException setMessage(String message) {
        return this;
    }

    public GatewayException setDetailMessage(String detailMessage) {
        this.detailMessage = detailMessage;
        return this;
    }
}