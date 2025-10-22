package hbnu.project.zhiyangateway.filter;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonbasic.exception.gateway.GatewayAuthenticationException;
import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import hbnu.project.zhiyangateway.client.AuthServiceClient;
import hbnu.project.zhiyangateway.model.TokenValidateResponse;
import hbnu.project.zhiyangateway.utils.WebFluxUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * 用户认证过滤器
 * 通过远程调用 Auth 服务验证用户登录状态
 * 未认证请求直接拦截
 *
 * @author ErgouTree
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserAuthenticationFilter implements GlobalFilter, Ordered {

    private final AuthServiceClient authServiceClient;

    /**
     * 不需要认证的路径列表
     */
    private static final List<String> SKIP_AUTH_PATHS = Arrays.asList(
            "/auth/login",
            "/auth/register",
            "/auth/refresh-token",
            "/auth/logout",
            "/auth/auto-login-check",
            "/auth/clear-remember-me",
            "/auth/verify-code",
            "/auth/forgot-password",
            "/auth/reset-password",
            "/actuator",
            "/nacos",
            "/swagger-ui",
            "/v3/api-docs",
            "/favicon.ico",
            "/error"
    );


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = WebFluxUtils.getOriginalRequestUrl(exchange);

        // 检查是否需要跳过认证
        if (isSkipAuthPath(path)) {
            log.debug("跳过认证检查 - 路径: {}", path);
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // 检查是否有 Authorization 头
        if (StringUtils.isBlank(authHeader)) {
            log.warn("用户认证失败 - 缺少 Authorization 头 - 路径: {}", path);
            return Mono.error(new GatewayAuthenticationException("缺少认证令牌"));
        }

        // 远程调用 Auth 服务验证 Token
        return authServiceClient.validateToken(authHeader)
                .flatMap(response -> {
                    if (Boolean.TRUE.equals(response.getIsValid())) {
                        // Token 有效，添加用户信息到请求头
                        ServerHttpRequest.Builder requestBuilder = request.mutate()
                                .header("X-User-Id", response.getUserId())
                                .header("X-Username", response.getUsername());
                        
                        // 如果有角色信息，也添加到请求头
                        if (response.getRoles() != null && !response.getRoles().isEmpty()) {
                            requestBuilder.header("X-User-Roles", response.getRoles());
                        }
                        
                        ServerHttpRequest mutatedRequest = requestBuilder.build();

                        ServerWebExchange mutatedExchange = exchange.mutate()
                                .request(mutatedRequest)
                                .build();

                        log.debug("用户认证成功 - 用户ID: {}, 角色: {}, 路径: {}", response.getUserId(), response.getRoles(), path);
                        return chain.filter(mutatedExchange);
                    } else {
                        // Token 无效 - 使用自定义异常
                        log.warn("用户认证失败 - {} - 路径: {}", response.getMessage(), path);
                        return Mono.error(new GatewayAuthenticationException(response.getMessage()));
                    }
                })
                .onErrorResume(e -> {
                    // 如果已经是网关异常，直接抛出
                    if (e instanceof GatewayAuthenticationException) {
                        return Mono.error(e);
                    }
                    // 其他异常转换为认证异常
                    log.error("Token 验证异常 - 路径: {}, 错误: {}", path, e.getMessage(), e);
                    return Mono.error(new GatewayAuthenticationException("认证服务异常", e));
                });
    }


    /**
     * 检查是否为跳过认证的路径
     */
    private boolean isSkipAuthPath(String path) {
        return SKIP_AUTH_PATHS.stream().anyMatch(path::startsWith);
    }


    /**
     * 返回未授权响应
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        return WebFluxUtils.webFluxResponseWriter(
                response,
                HttpStatus.UNAUTHORIZED,
                message,
                R.UNAUTHORIZED
        );
    }


    @Override
    public int getOrder() {
        // 在 CORS 过滤器之后，日志过滤器之前执行
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}