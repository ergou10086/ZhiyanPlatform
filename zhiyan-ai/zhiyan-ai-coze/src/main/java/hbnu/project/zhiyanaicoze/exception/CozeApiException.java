package hbnu.project.zhiyanaicoze.exception;

import hbnu.project.zhiyancommonbasic.exception.base.BaseException;

/**
 * Coze API 异常
 *
 * @author ErgouTree
 */
public class CozeApiException extends BaseException {

    public CozeApiException(String message) {
        super(message);
    }

    public CozeApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public CozeApiException(Throwable cause) {
        super(String.valueOf(cause));
    }
}
