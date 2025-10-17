package hbnu.project.zhiyangateway.filter;

import hbnu.project.zhiyancommonbasic.constants.SecurityConstants;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import hbnu.project.zhiyancommonbasic.utils.ip.IpMatchUtils;
import hbnu.project.zhiyangateway.config.AccessControlConfig;
import hbnu.project.zhiyangateway.model.AccessControlRule;
import hbnu.project.zhiyangateway.utils.WebFluxUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 访问控制过滤器
 * 提供基于角色、IP、HTTP方法的访问控制
 *
 * @author ErgouTree
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccessControlFilter implements GlobalFilter, Ordered {

    private final AccessControlConfig accessControlConfig;
    // ant路径匹配
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 如果访问控制未启用，直接放行
        if (!Boolean.TRUE.equals(accessControlConfig.getEnabled())) {
            return chain.filter(exchange);
        }

        // 解析请求
        ServerHttpRequest request = exchange.getRequest();
        String path = WebFluxUtils.getOriginalRequestUrl(exchange);
        HttpMethod method = request.getMethod();
        String clientIp = getClientIp(request);

        // 查找匹配的规则（按优先级）
        for (AccessControlRule rule : accessControlConfig.getEnabledRules()) {
            if (!pathMatcher.match(rule.getPathPattern(), path)) {
                continue; // 路径不匹配，检查下一条规则
            }

            log.debug("路径 {} 匹配规则: {}", path, rule.getPathPattern());

            // 1. 检查是否仅限内部服务调用
            if (Boolean.TRUE.equals(rule.getInternalOnly())) {
                if (!isInternalRequest(request)) {
                    log.warn("拒绝外部访问内部接口 - 路径: {}, IP: {}", path, clientIp);
                    return forbiddenResponse(exchange, rule.getDenyMessage());
                }
            }

            // 2. 检查HTTP方法
            if (rule.getAllowedMethods() != null && !rule.getAllowedMethods().isEmpty()) {
                if (!rule.getAllowedMethods().contains(method)) {
                    log.warn("HTTP方法不允许 - 路径: {}, 方法: {}, 允许: {}",
                            path, method, rule.getAllowedMethods());
                    return methodNotAllowedResponse(exchange,
                            "不允许的HTTP方法: " + method + "，仅允许: " + rule.getAllowedMethods());
                }
            }

            // 3. 检查IP白名单
            if (rule.getAllowedIps() != null && !rule.getAllowedIps().isEmpty()) {
                if (!IpMatchUtils.matches(clientIp, rule.getAllowedIps())) {
                    log.warn("IP不在白名单 - 路径: {}, IP: {}, 允许: {}",
                            path, clientIp, rule.getAllowedIps());
                    return forbiddenResponse(exchange, "您的IP地址无权访问此接口");
                }
            }

            // 4. 检查角色权限
            if (rule.getAllowedRoles() != null && !rule.getAllowedRoles().isEmpty()) {
                Set<String> userRoles = getUserRoles(request);

                if (userRoles.isEmpty()) {
                    log.warn("未提供角色信息 - 路径: {}", path);
                    return forbiddenResponse(exchange, "需要登录才能访问");
                }

                boolean hasPermission = userRoles.stream()
                        .anyMatch(role -> rule.getAllowedRoles().contains(role));

                if (!hasPermission) {
                    log.warn("角色权限不足 - 路径: {}, 用户角色: {}, 需要角色: {}",
                            path, userRoles, rule.getAllowedRoles());
                    return forbiddenResponse(exchange,
                            rule.getDenyMessage() != null ? rule.getDenyMessage() : "权限不足");
                }
            }

            // 所有检查通过
            log.debug("访问控制检查通过 - 路径: {}, IP: {}", path, clientIp);
            break; // 匹配到规则后停止检查
        }

        return chain.filter(exchange);
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(ServerHttpRequest request) {
        // 优先从X-Forwarded-For获取
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (StringUtils.isNotBlank(xForwardedFor) && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        // 从X-Real-IP获取
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (StringUtils.isNotBlank(xRealIp) && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        // 从Remote Address获取
        String remoteAddr = request.getRemoteAddress() != null ?
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown";

        // IPv6本地地址转换
        return "0:0:0:0:0:0:0:1".equals(remoteAddr) ? "127.0.0.1" : remoteAddr;
    }

    /**
     * 检查是否为内部请求
     */
    private boolean isInternalRequest(ServerHttpRequest request) {
        String fromSource = request.getHeaders().getFirst(SecurityConstants.FROM_SOURCE);
        return SecurityConstants.INNER.equals(fromSource);
    }

    /**
     * 获取用户角色
     * 从请求头中获取（由认证过滤器设置）
     */
    private Set<String> getUserRoles(ServerHttpRequest request) {
        String rolesHeader = request.getHeaders().getFirst("X-User-Roles");
        if (StringUtils.isBlank(rolesHeader)) {
            return new HashSet<>();
        }

        // 角色以逗号分隔
        return new HashSet<>(Arrays.asList(rolesHeader.split(",")));
    }

    /**
     * 返回403 Forbidden响应
     */
    private Mono<Void> forbiddenResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        return WebFluxUtils.webFluxResponseWriter(
                response,
                HttpStatus.FORBIDDEN,
                StringUtils.isNotBlank(message) ? message : "访问被拒绝",
                R.FORBIDDEN
        );
    }

    /**
     * 返回405 Method Not Allowed响应
     */
    private Mono<Void> methodNotAllowedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        return WebFluxUtils.webFluxResponseWriter(
                response,
                HttpStatus.METHOD_NOT_ALLOWED,
                message,
                405
        );
    }

    @Override
    public int getOrder() {
        // 在用户认证过滤器之后执行
        return Ordered.HIGHEST_PRECEDENCE + 2;
    }
}
