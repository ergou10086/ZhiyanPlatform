package hbnu.project.zhiyanai.exception;

/**
 * Dify API 异常
 *
 * @author ErgouTree
 */
public class DifyApiException extends RuntimeException {

    public DifyApiException(String message) {
        super(message);
    }

    public DifyApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
