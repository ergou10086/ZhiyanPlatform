package hbnu.project.zhiyanaidify.exception.handler;

import hbnu.project.zhiyanaidify.exception.DifyApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Dify异常处理器
 *
 * @author ErgouTree
 */
@Slf4j
@RestControllerAdvice
public class DifyExceptionHandler {

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex
    ) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "参数校验失败");
        response.put("errors", errors);

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理 Dify API 异常
     */
    @ExceptionHandler(DifyApiException.class)
    public ResponseEntity<Map<String, Object>> handleDifyApiException(DifyApiException ex) {
        log.error("Dify API 异常: ", ex);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "AI 服务调用失败: " + ex.getMessage());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        log.error("系统异常: ", ex);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "系统异常: " + ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
