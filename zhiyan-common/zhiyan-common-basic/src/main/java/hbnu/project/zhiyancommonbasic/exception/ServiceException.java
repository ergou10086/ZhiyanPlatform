package hbnu.project.zhiyancommonbasic.exception;

import hbnu.project.zhiyancommonbasic.exception.base.BaseException;

/**
 * 业务异常
 *
 * @author ErgouTree
 */
public class ServiceException extends BaseException {
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误提示
     */
    private String message;

    /**
     * 错误明细，内部调试错误
     */
    private String detailMessage;

    /**
     * 空构造方法，避免反序列化问题
     */
    public ServiceException() {
        super("业务异常");
    }

    public ServiceException(String message) {
        super(message);
        this.message = message;
    }

    public ServiceException(String message, Integer code) {
        super(message);
        this.message = message;
        this.code = code;
    }

    public ServiceException(String message, Throwable e) {
        super(message, e);
        this.message = message;
    }

    public ServiceException(String message, Integer code, Throwable e) {
        super(message, e);
        this.message = message;
        this.code = code;
    }

    public String getDetailMessage() {
        return detailMessage;
    }

    public ServiceException setDetailMessage(String detailMessage) {
        this.detailMessage = detailMessage;
        return this;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public ServiceException setMessage(String message) {
        this.message = message;
        return this;
    }

    public Integer getIntegerCode() {
        return code;
    }

    public ServiceException setCode(Integer code) {
        this.code = code;
        return this;
    }
}