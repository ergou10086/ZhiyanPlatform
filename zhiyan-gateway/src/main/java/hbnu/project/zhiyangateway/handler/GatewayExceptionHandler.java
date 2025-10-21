package hbnu.project.zhiyangateway.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonbasic.enums.GatewayErrorCode;
import hbnu.project.zhiyancommonbasic.exception.GatewayException;
import hbnu.project.zhiyancommonbasic.exception.gateway.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.cloud.gateway.support.TimeoutException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.PrematureCloseException;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;

/**
 * 网关统一异常处理器
 * 捕获并处理网关层面的各种异常
 *
 * @author ErgouTree
 */
@Slf4j
@Order(-1)
@Configuration
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        // 如果响应已经提交，直接返回错误
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        // 构建错误响应
        ErrorResponse errorResponse = buildErrorResponse(exchange, ex);

        // 记录异常日志
        logException(exchange, ex, errorResponse);

        // 设置响应内容类型
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.setStatusCode(errorResponse.getHttpStatus());

        // 写入响应
        return response.writeWith(Mono.fromSupplier(() -> {
            try {
                R<Void> result = R.fail(errorResponse.getCode(), errorResponse.getMessage());
                String jsonResult = objectMapper.writeValueAsString(result);
                byte[] bytes = jsonResult.getBytes(StandardCharsets.UTF_8);
                return response.bufferFactory().wrap(bytes);
            } catch (JsonProcessingException e) {
                log.error("序列化错误响应失败", e);
                return response.bufferFactory().wrap(new byte[0]);
            }
        }));
    }

    /**
     * 构建错误响应
     */
    private ErrorResponse buildErrorResponse(ServerWebExchange exchange, Throwable ex) {
        String path = exchange.getRequest().getPath().value();

        // 1. 自定义网关异常
        if (ex instanceof GatewayAuthenticationException) {
            return new ErrorResponse(
                    GatewayErrorCode.UNAUTHORIZED.getCode(),
                    ex.getMessage(),
                    HttpStatus.UNAUTHORIZED,
                    path
            );
        }

        if (ex instanceof GatewayAuthorizationException) {
            return new ErrorResponse(
                    GatewayErrorCode.FORBIDDEN.getCode(),
                    ex.getMessage(),
                    HttpStatus.FORBIDDEN,
                    path
            );
        }

        if (ex instanceof GatewayRouteException) {
            return new ErrorResponse(
                    GatewayErrorCode.ROUTE_NOT_FOUND.getCode(),
                    ex.getMessage(),
                    HttpStatus.NOT_FOUND,
                    path
            );
        }

        if (ex instanceof GatewayRateLimitException) {
            return new ErrorResponse(
                    GatewayErrorCode.RATE_LIMIT_EXCEEDED.getCode(),
                    ex.getMessage(),
                    HttpStatus.TOO_MANY_REQUESTS,
                    path
            );
        }

        if (ex instanceof GatewayServiceUnavailableException) {
            return new ErrorResponse(
                    GatewayErrorCode.SERVICE_UNAVAILABLE.getCode(),
                    ex.getMessage(),
                    HttpStatus.SERVICE_UNAVAILABLE,
                    path
            );
        }

        if (ex instanceof GatewayTimeoutException) {
            return new ErrorResponse(
                    GatewayErrorCode.GATEWAY_TIMEOUT.getCode(),
                    ex.getMessage(),
                    HttpStatus.GATEWAY_TIMEOUT,
                    path
            );
        }

        if (ex instanceof GatewayBadRequestException) {
            return new ErrorResponse(
                    GatewayErrorCode.BAD_REQUEST.getCode(),
                    ex.getMessage(),
                    HttpStatus.BAD_REQUEST,
                    path
            );
        }

        // 通用网关异常
        if (ex instanceof GatewayException gatewayEx) {
            return new ErrorResponse(
                    gatewayEx.getCode(),
                    gatewayEx.getMessage(),
                    gatewayEx.getHttpStatus(),
                    path
            );
        }

        // 2. Spring Cloud Gateway 异常
        if (ex instanceof NotFoundException) {
            return new ErrorResponse(
                    GatewayErrorCode.SERVICE_NOT_FOUND.getCode(),
                    "服务未找到: " + ex.getMessage(),
                    HttpStatus.NOT_FOUND,
                    path
            );
        }

        if (ex instanceof TimeoutException) {
            return new ErrorResponse(
                    GatewayErrorCode.GATEWAY_TIMEOUT.getCode(),
                    "网关请求超时",
                    HttpStatus.GATEWAY_TIMEOUT,
                    path
            );
        }

        // 3. 网络连接异常
        if (ex instanceof ConnectException) {
            return new ErrorResponse(
                    GatewayErrorCode.SERVICE_UNAVAILABLE.getCode(),
                    "无法连接到后端服务",
                    HttpStatus.SERVICE_UNAVAILABLE,
                    path
            );
        }

        if (ex instanceof PrematureCloseException) {
            return new ErrorResponse(
                    GatewayErrorCode.SERVICE_UNAVAILABLE.getCode(),
                    "后端服务连接意外关闭",
                    HttpStatus.SERVICE_UNAVAILABLE,
                    path
            );
        }

        // 4. ResponseStatusException
        if (ex instanceof ResponseStatusException rse) {
            return new ErrorResponse(
                    rse.getStatusCode().value(),
                    rse.getReason() != null ? rse.getReason() : "请求异常",
                    (HttpStatus) rse.getStatusCode(),
                    path
            );
        }

        // 5. 默认异常
        return new ErrorResponse(
                GatewayErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                "内部服务器错误",
                HttpStatus.INTERNAL_SERVER_ERROR,
                path
        );
    }

    /**
     * 记录异常日志
     */
    private void logException(ServerWebExchange exchange, Throwable ex, ErrorResponse errorResponse) {
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();

        // 根据异常级别记录不同的日志
        if (errorResponse.getHttpStatus().is5xxServerError()) {
            log.error("[网关异常] 路径: {} {}, 错误码: {}, 错误信息: {}, 异常详情: ",
                    method, path, errorResponse.getCode(), errorResponse.getMessage(), ex);
        } else if (errorResponse.getHttpStatus().is4xxClientError()) {
            log.warn("[网关异常] 路径: {} {}, 错误码: {}, 错误信息: {}",
                    method, path, errorResponse.getCode(), errorResponse.getMessage());
        } else {
            log.info("[网关异常] 路径: {} {}, 错误码: {}, 错误信息: {}",
                    method, path, errorResponse.getCode(), errorResponse.getMessage());
        }
    }

    /**
     * 错误响应内部类
     */
    private static class ErrorResponse {
        private final Integer code;
        @Getter
        private final String message;
        @Getter
        private final HttpStatus httpStatus;
        @Getter
        private final String path;

        public ErrorResponse(Integer code, String message, HttpStatus httpStatus, String path) {
            this.code = code;
            this.message = message;
            this.httpStatus = httpStatus;
            this.path = path;
        }

        public int getCode() {
            return code;
        }

    }
}