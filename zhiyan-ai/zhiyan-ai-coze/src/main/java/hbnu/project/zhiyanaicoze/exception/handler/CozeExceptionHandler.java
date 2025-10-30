package hbnu.project.zhiyanaicoze.exception.handler;

import hbnu.project.zhiyanaicoze.exception.CozeApiException;
import hbnu.project.zhiyancommonbasic.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Coze 异常处理器
 *
 * @author ErgouTree
 */
@Slf4j
@RestControllerAdvice
public class CozeExceptionHandler {

    /**
     * 处理 Coze API 异常
     */
    @ExceptionHandler(CozeApiException.class)
    public R<Void> handleCozeApiException(CozeApiException e) {
        log.error("Coze API 异常", e);
        return R.fail(e.getMessage());
    }

    /**
     * 处理安全异常（未登录）
     */
    @ExceptionHandler(SecurityException.class)
    public R<Void> handleSecurityException(SecurityException e) {
        log.warn("安全异常: {}", e.getMessage());
        return R.fail(R.UNAUTHORIZED, e.getMessage());
    }
}
