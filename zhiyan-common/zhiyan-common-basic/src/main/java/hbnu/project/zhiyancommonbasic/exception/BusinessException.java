package hbnu.project.zhiyancommonbasic.exception;

import hbnu.project.zhiyancommonbasic.exception.base.BaseException;

import java.io.Serial;

/**
 * 业务异常
 * 用于Controller层和Service层的业务异常
 *
 * @author ErgouTree
 */
public class BusinessException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    public BusinessException(String module, String code, Object[] args, String defaultMessage) {
        super(module, code, args, defaultMessage);
    }

    public BusinessException(String module, String code, Object[] args) {
        super(module, code, args);
    }

    public BusinessException(String module, String defaultMessage) {
        super(module, defaultMessage);
    }

    public BusinessException(String defaultMessage) {
        super(defaultMessage);
    }

    public BusinessException(String message, Throwable e) {
        super(message, e);
    }

    /**
     * 创建认证模块异常
     */
    public static BusinessException auth(String code, String message) {
        return new BusinessException("auth", code, null, message);
    }

    /**
     * 创建用户模块异常
     */
    public static BusinessException user(String code, String message) {
        return new BusinessException("user", code, null, message);
    }

    /**
     * 创建头像模块异常
     */
    public static BusinessException avatar(String code, String message) {
        return new BusinessException("avatar", code, null, message);
    }
}
