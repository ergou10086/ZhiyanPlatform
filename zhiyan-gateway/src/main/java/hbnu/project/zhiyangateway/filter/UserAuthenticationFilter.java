package hbnu.project.zhiyangateway.filter;

import hbnu.project.zhiyancommonbasic.constants.CacheConstants;
import hbnu.project.zhiyancommonbasic.constants.TokenConstants;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonbasic.utils.JwtUtils;
import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import hbnu.project.zhiyancommonredis.service.RedisService;
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
import java.util.concurrent.TimeUnit;

/**
 * 用户认证过滤器,在操作接口的时候检查用户的登陆情况
 * 检查用户登录状态，验证JWT Token的有效性
 *
 * @author ErgouTree
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtils jwtUtils;
    private final RedisService redisService;

    /**
     * 不需要认证的路径列表
     */
    private static final List<String> SKIP_AUTH_PATHS = Arrays.asList(
            "/auth/login",           // 登录接口
            "/auth/register",        // 注册接口
            "/auth/refresh-token",   // 刷新token接口
            "/auth/logout",          // 登出接口
            "/auth/auto-login-check", // 自动登录检查
            "/auth/clear-remember-me", // 清除记住我
            "/auth/verify-code",     // 验证码接口
            "/auth/forgot-password", // 忘记密码
            "/auth/reset-password",  // 重置密码
            "/actuator",             // 监控端点
            "/nacos",                // Nacos相关
            "/swagger-ui",           // Swagger UI
            "/v3/api-docs",          // API文档
            "/favicon.ico",          // 网站图标
            "/error"                 // 错误页面
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = WebFluxUtils.getOriginalRequestUrl(exchange);

        // 检查一下是否不需要认证
        if (isSkipAuthPath(path)) {
            log.debug("跳过认证检查 - 路径: {}", path);
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // 检查是否有Authorization头
        if (StringUtils.isBlank(authHeader)) {
            log.warn("用户认证失败 - 缺少Authorization头 - 路径: {}", path);
            return unauthorizedResponse(exchange, "缺少认证信息");
        }

        // 检查Bearer前缀
        if (!authHeader.startsWith(TokenConstants.TOKEN_TYPE_BEARER + " ")) {
            log.warn("用户认证失败 - Token格式错误 - 路径: {}", path);
            return unauthorizedResponse(exchange, "Token格式错误");
        }

        // 提取Token
        String token = authHeader.substring(TokenConstants.TOKEN_TYPE_BEARER.length() + 1);

        // 验证Token
        return validateToken(token, exchange, chain, path, request);
    }

    /**
     * 验证Token并继续处理请求
     */
    private Mono<Void> validateToken(String token, ServerWebExchange exchange,
                                     GatewayFilterChain chain, String path, ServerHttpRequest request) {

        try {
            // 1. 检查Token是否在黑名单中
            if (isTokenBlacklisted(token)) {
                log.warn("用户认证失败 - Token已失效 - 路径: {}", path);
                return unauthorizedResponse(exchange, "Token已失效");
            }

            // 2. 验证JWT Token
            if (!jwtUtils.validateToken(token)) {
                log.warn("用户认证失败 - Token无效或已过期 - 路径: {}", path);
                return unauthorizedResponse(exchange, "Token无效或已过期");
            }

            // 3. 解析用户ID
            String userId = jwtUtils.parseToken(token);
            if (StringUtils.isBlank(userId)) {
                log.warn("用户认证失败 - 无法解析用户ID - 路径: {}", path);
                return unauthorizedResponse(exchange, "Token解析失败");
            }

            // 4. 验证Token是否与Redis中存储的一致
            String redisTokenKey = CacheConstants.USER_TOKEN_PREFIX + userId;
            String redisToken = redisService.getCacheObject(redisTokenKey);

            if (!token.equals(redisToken)) {
                log.warn("用户认证失败 - Token不匹配 - 用户ID: {}, 路径: {}", userId, path);
                return unauthorizedResponse(exchange, "Token已失效");
            }

            // 5. 检查Token是否即将过期，如果即将过期则刷新
            Long remainingTime = jwtUtils.getRemainingTime(token);
            if (remainingTime != null && remainingTime < 300) { // 剩余时间少于5分钟
                log.info("Token即将过期，准备刷新 - 用户ID: {}, 剩余时间: {}秒", userId, remainingTime);
                refreshTokenInRedis(userId, token);
            }

            // 6. 认证成功，添加用户信息到请求头
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Token", token)
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build();

            log.debug("用户认证成功 - 用户ID: {}, 路径: {}", userId, path);
            return chain.filter(mutatedExchange);

        } catch (Exception e) {
            log.error("Token验证异常 - 路径: {}, 错误: {}", path, e.getMessage(), e);
            return unauthorizedResponse(exchange, "认证服务异常");
        }
    }

    /**
     * 刷新Redis中的Token
     */
    private void refreshTokenInRedis(String userId, String currentToken) {
        try {
            // 生成新的Token（延长过期时间）
            String newToken = jwtUtils.createToken(userId, TokenConstants.DEFAULT_ACCESS_TOKEN_EXPIRE_MINUTES);

            // 更新Redis中的Token
            String tokenKey = CacheConstants.USER_TOKEN_PREFIX + userId;
            long cacheTimeSeconds = (long) TokenConstants.DEFAULT_ACCESS_TOKEN_EXPIRE_MINUTES * 60;
            redisService.setCacheObject(tokenKey, newToken, cacheTimeSeconds, TimeUnit.SECONDS);

            log.info("Token自动刷新成功 - 用户ID: {}", userId);
        } catch (Exception e) {
            log.error("Token自动刷新失败 - 用户ID: {}, 错误: {}", userId, e.getMessage());
        }
    }

    /**
     * 检查Token是否在黑名单中
     */
    private boolean isTokenBlacklisted(String token) {
        try {
            String blacklistKey = CacheConstants.TOKEN_BLACKLIST_PREFIX + token;
            return redisService.hasKey(blacklistKey);
        } catch (Exception e) {
            log.error("检查Token黑名单异常: {}", e.getMessage());
            return false;
        }
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
        // 在CORS过滤器之后，日志过滤器之前执行
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

}