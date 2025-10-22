package hbnu.project.zhiyancommonbasic.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 网关错误码枚举
 * 定义所有网关相关的错误码
 *
 * @author ErgouTree
 */
@Getter
@AllArgsConstructor
public enum GatewayErrorCode {

    // ========== 认证相关错误 4001-4099 ==========
    UNAUTHORIZED(4001, "未认证，请先登录", HttpStatus.UNAUTHORIZED),
    TOKEN_MISSING(4002, "缺少认证令牌", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID(4003, "认证令牌无效", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(4004, "认证令牌已过期", HttpStatus.UNAUTHORIZED),
    TOKEN_MALFORMED(4005, "认证令牌格式错误", HttpStatus.UNAUTHORIZED),

    // ========== 授权相关错误 4031-4099 ==========
    FORBIDDEN(4031, "权限不足", HttpStatus.FORBIDDEN),
    ACCESS_DENIED(4032, "访问被拒绝", HttpStatus.FORBIDDEN),
    ROLE_INSUFFICIENT(4033, "角色权限不足", HttpStatus.FORBIDDEN),

    // ========== 路由相关错误 4041-4099 ==========
    ROUTE_NOT_FOUND(4041, "路由不存在", HttpStatus.NOT_FOUND),
    SERVICE_NOT_FOUND(4042, "服务未找到", HttpStatus.NOT_FOUND),
    ENDPOINT_NOT_FOUND(4043, "接口端点不存在", HttpStatus.NOT_FOUND),

    // ========== 请求相关错误 4001-4099 ==========
    BAD_REQUEST(4000, "错误的请求", HttpStatus.BAD_REQUEST),
    INVALID_PARAMETER(4006, "请求参数无效", HttpStatus.BAD_REQUEST),
    MISSING_PARAMETER(4007, "缺少必需参数", HttpStatus.BAD_REQUEST),
    SQL_INJECTION_DETECTED(4008, "检测到SQL注入攻击", HttpStatus.BAD_REQUEST),
    XSS_ATTACK_DETECTED(4009, "检测到XSS攻击", HttpStatus.BAD_REQUEST),

    // ========== 限流相关错误 4291-4299 ==========
    RATE_LIMIT_EXCEEDED(4291, "请求频率超过限制", HttpStatus.TOO_MANY_REQUESTS),
    CONCURRENT_LIMIT_EXCEEDED(4292, "并发请求数超过限制", HttpStatus.TOO_MANY_REQUESTS),
    QUOTA_EXCEEDED(4293, "请求配额已用尽", HttpStatus.TOO_MANY_REQUESTS),

    // ========== 服务器相关错误 5001-5099 ==========
    INTERNAL_SERVER_ERROR(5000, "内部服务器错误", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_UNAVAILABLE(5031, "服务暂时不可用", HttpStatus.SERVICE_UNAVAILABLE),
    SERVICE_CIRCUIT_BREAKER(5032, "服务熔断", HttpStatus.SERVICE_UNAVAILABLE),
    SERVICE_DEGRADED(5033, "服务降级", HttpStatus.SERVICE_UNAVAILABLE),

    // ========== 超时相关错误 5041-5049 ==========
    GATEWAY_TIMEOUT(5041, "网关请求超时", HttpStatus.GATEWAY_TIMEOUT),
    UPSTREAM_TIMEOUT(5042, "上游服务响应超时", HttpStatus.GATEWAY_TIMEOUT),
    CONNECTION_TIMEOUT(5043, "连接超时", HttpStatus.GATEWAY_TIMEOUT),

    // ========== 其他错误 ==========
    UNKNOWN_ERROR(9999, "未知错误", HttpStatus.INTERNAL_SERVER_ERROR);

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误信息
     */
    private final String message;

    /**
     * HTTP状态码
     */
    private final HttpStatus httpStatus;

    /**
     * 根据错误码获取枚举
     *
     * @param code 错误码
     * @return 对应的枚举，如果未找到返回UNKNOWN_ERROR
     */
    public static GatewayErrorCode fromCode(Integer code) {
        for (GatewayErrorCode errorCode : values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode;
            }
        }
        return UNKNOWN_ERROR;
    }
}
