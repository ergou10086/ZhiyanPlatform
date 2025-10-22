package hbnu.project.zhiyancommonbasic.exception;

import hbnu.project.zhiyancommonbasic.exception.base.BaseException;

import java.io.Serial;

/**
 * 文件操作异常
 * 用于处理文件上传、下载、删除等操作中的异常
 *
 * @author ErgouTree
 */
public class FileException extends BaseException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 错误码（数字类型）
     */
    private Integer errorCode;

    /**
     * 默认构造函数
     */
    public FileException() {
        super("文件操作异常");
        this.errorCode = 5000;
    }

    /**
     * 带消息的构造函数
     *
     * @param message 错误消息
     */
    public FileException(String message) {
        super(message);
        this.errorCode = 5000;
    }

    /**
     * 带消息和错误码的构造函数
     *
     * @param message 错误消息
     * @param errorCode    错误码
     */
    public FileException(String message, Integer errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 带消息和异常原因的构造函数
     *
     * @param message 错误消息
     * @param cause   异常原因
     */
    public FileException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = 5000;
    }

    /**
     * 完整构造函数
     *
     * @param message 错误消息
     * @param errorCode    错误码
     * @param cause   异常原因
     */
    public FileException(String message, Integer errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    // Getters and Setters

    public Integer getErrorCode() {
        return errorCode;
    }

    public FileException setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public FileException setMessage(String message) {
        return this;
    }
}