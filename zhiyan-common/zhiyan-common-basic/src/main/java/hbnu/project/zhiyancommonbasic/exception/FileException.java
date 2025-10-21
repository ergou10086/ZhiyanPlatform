package hbnu.project.zhiyancommonbasic.exception;

import hbnu.project.zhiyancommonbasic.exception.base.BaseException;

/**
 * 文件操作异常
 * 用于处理文件上传、下载、删除等操作中的异常
 *
 * @author ErgouTree
 */
public class FileException extends BaseException {
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误信息
     */
    private String message;

    /**
     * 默认构造函数
     */
    public FileException() {
        super("文件操作异常");
        this.code = 5000;
    }

    /**
     * 带消息的构造函数
     *
     * @param message 错误消息
     */
    public FileException(String message) {
        super(message);
        this.message = message;
        this.code = 5000;
    }

    /**
     * 带消息和错误码的构造函数
     *
     * @param message 错误消息
     * @param code    错误码
     */
    public FileException(String message, Integer code) {
        super(message);
        this.message = message;
        this.code = code;
    }

    /**
     * 带消息和异常原因的构造函数
     *
     * @param message 错误消息
     * @param cause   异常原因
     */
    public FileException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.code = 5000;
    }

    /**
     * 完整构造函数
     *
     * @param message 错误消息
     * @param code    错误码
     * @param cause   异常原因
     */
    public FileException(String message, Integer code, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.code = code;
    }

    // Getters and Setters

    public String getCode() {
        return code;
    }

    public FileException setCode(Integer code) {
        this.code = code;
        return this;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public FileException setMessage(String message) {
        this.message = message;
        return this;
    }
}
