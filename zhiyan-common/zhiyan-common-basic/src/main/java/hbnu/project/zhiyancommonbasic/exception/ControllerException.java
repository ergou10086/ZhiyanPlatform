package hbnu.project.zhiyancommonbasic.exception;

import hbnu.project.zhiyancommonbasic.exception.base.BaseException;

/**
 * 控制器层异常
 * 用于在Controller层标记业务异常
 *
 * @author ErgouTree
 */
public class ControllerException extends BaseException {

    private static final long serialVersionUID = 1L;

    // 模块名称
    private static final String MODULE = "auth-controller";

    public ControllerException(String code, Object[] args, String defaultMessage) {
        super(MODULE, code, args, defaultMessage);
    }

    public ControllerException(String code, Object[] args) {
        super(MODULE, code, args);
    }

    public ControllerException(String code, String defaultMessage) {
        super(MODULE, code, null, defaultMessage);
    }

    public ControllerException(String defaultMessage) {
        super(MODULE, defaultMessage);
    }

    public ControllerException(String message, Throwable e) {
        super(message, e);
    }
}