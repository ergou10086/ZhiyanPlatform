package hbnu.project.zhiyangateway.filter;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import hbnu.project.zhiyancommonbasic.utils.SqlUtil;
import hbnu.project.zhiyancommonsecurity.xss.XssValidator;
import hbnu.project.zhiyangateway.utils.WebFluxUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * SQL注入防护过滤器
 * 综合防护SQL注入、XSS攻击等安全威胁
 *
 * @author ErgouTree
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SqlInjectionProtectionFilter implements GlobalFilter, Ordered {

    /**
     * 跳过安全检查的路径列表
     */
    private static final List<String> SKIP_SECURITY_PATHS = Arrays.asList(
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
            "/error",                // 错误页面
            "/health",               // 健康检查
            "/metrics"               // 监控指标
    );

    /**
     * SQL注入攻击模式检测
     */
    private static final List<Pattern> SQL_INJECTION_PATTERNS = Arrays.asList(
            // 基础SQL关键字
            Pattern.compile("(?i)(union|select|insert|update|delete|drop|create|alter|exec|execute)"),
            // SQL注释
            Pattern.compile("(?i)(--|#|/\\*|\\*/)"),
            // SQL函数
            Pattern.compile("(?i)(count|sum|avg|max|min|substring|char|ascii|hex|unhex)"),
            // 特殊字符组合
            Pattern.compile("(?i)(\\s+or\\s+\\d+\\s*=\\s*\\d+|\\s+and\\s+\\d+\\s*=\\s*\\d+)"),
            // 时间延迟攻击
            Pattern.compile("(?i)(sleep|waitfor|benchmark|delay)"),
            // 信息收集
            Pattern.compile("(?i)(information_schema|sys\\.|mysql\\.|pg_|sqlite_)"),
            // 盲注特征
            Pattern.compile("(?i)(if\\s*\\(|case\\s+when|\\?\\s*:\\s*\\d+)"),
            // 堆叠查询
            Pattern.compile("(?i)(;\\s*\\w+\\s*\\w+)"),
            // 布尔盲注
            Pattern.compile("(?i)(\\d+\\s*=\\s*\\d+|\\d+\\s*!=\\s*\\d+)"),
            // 报错注入
            Pattern.compile("(?i)(extractvalue|updatexml|floor|exp|polygon)")
    );

    /**
     * 危险字符模式
     */
    private static final List<Pattern> DANGEROUS_PATTERNS = Arrays.asList(
            // 脚本标签
            Pattern.compile("(?i)<script[^>]*>.*?</script>"),
            // 事件处理器
            Pattern.compile("(?i)(onload|onerror|onclick|onmouseover)\\s*="),
            // JavaScript协议
            Pattern.compile("(?i)javascript\\s*:"),
            // 数据协议
            Pattern.compile("(?i)data\\s*:"),
            // 特殊字符
            Pattern.compile("[<>\"'%;()&+]"),
            // 编码攻击
            Pattern.compile("(?i)(%3c|%3e|%22|%27|%3b|%25|%28|%29|%26|%2b)")
    );

    /**
     * IP黑名单（内存缓存，实际应用中应使用Redis）
     */
    private final Map<String, Integer> suspiciousIpCount = new ConcurrentHashMap<>();
    private final Map<String, Long> ipBlockTime = new ConcurrentHashMap<>();

    /**
     * 攻击检测阈值
     */
    private static final int MAX_SUSPICIOUS_COUNT = 5;
    private static final long IP_BLOCK_DURATION = 30 * 60 * 1000L; // 30分钟

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = WebFluxUtils.getOriginalRequestUrl(exchange);
        String clientIp = getClientIp(request);

        // 检查是否为跳过安全检查的路径
        if (isSkipSecurityPath(path)) {
            log.debug("跳过安全检查 - 路径: {}", path);
            return chain.filter(exchange);
        }

        // 检查IP是否被临时封禁
        if (isIpBlocked(clientIp)) {
            log.warn("IP被临时封禁 - IP: {}, 路径: {}", clientIp, path);
            return forbiddenResponse(exchange, "IP已被临时封禁，请稍后再试");
        }

        try {
            // 1. 检查请求参数中的SQL注入
            if (HttpMethod.GET.equals(request.getMethod()) || HttpMethod.DELETE.equals(request.getMethod())) {
                MultiValueMap<String, String> queryParams = request.getQueryParams();
                if (hasSqlInjection(queryParams)) {
                    handleSecurityViolation(exchange, clientIp, "SQL注入攻击检测 - GET参数", path);
                    return forbiddenResponse(exchange, "请求参数包含非法字符");
                }
            }

            // 2. 检查请求体中的SQL注入（POST/PUT请求）
            if (HttpMethod.POST.equals(request.getMethod()) || HttpMethod.PUT.equals(request.getMethod())) {
                String requestBody = WebFluxUtils.resolveBodyFromCacheRequest(exchange);
                if (StringUtils.isNotBlank(requestBody)) {
                    if (hasSqlInjection(requestBody)) {
                        handleSecurityViolation(exchange, clientIp, "SQL注入攻击检测 - 请求体", path);
                        return forbiddenResponse(exchange, "请求内容包含非法字符");
                    }
                }
            }

            // 3. 检查请求头中的潜在攻击
            if (hasMaliciousHeaders(request)) {
                handleSecurityViolation(exchange, clientIp, "恶意请求头检测", path);
                return forbiddenResponse(exchange, "请求头包含非法内容");
            }

            // 4. 检查User-Agent中的异常
            if (hasSuspiciousUserAgent(request)) {
                log.warn("可疑User-Agent - IP: {}, UA: {}", clientIp, request.getHeaders().getFirst("User-Agent"));
                incrementSuspiciousCount(clientIp);
            }

            // 5. 成功通过安全检查，记录正常访问
            resetSuspiciousCount(clientIp);

            return chain.filter(exchange);

        } catch (Exception e) {
            log.error("安全检查异常 - IP: {}, 路径: {}, 错误: {}", clientIp, path, e.getMessage(), e);
            return forbiddenResponse(exchange, "安全检查异常");
        }
    }

    /**
     * 检查查询参数中是否包含SQL注入
     */
    private boolean hasSqlInjection(MultiValueMap<String, String> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return false;
        }

        for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();

            // 检查参数名
            if (isDangerousContent(key)) {
                return true;
            }

            // 检查参数值
            for (String value : values) {
                if (StringUtils.isNotBlank(value) && isDangerousContent(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检查请求体中是否包含SQL注入
     */
    private boolean hasSqlInjection(String requestBody) {
        if (StringUtils.isBlank(requestBody)) {
            return false;
        }

        return isDangerousContent(requestBody);
    }

    /**
     * 检查内容是否危险（SQL注入、XSS等）
     */
    private boolean isDangerousContent(String content) {
        if (StringUtils.isBlank(content)) {
            return false;
        }

        // 1. 检查SQL注入模式
        for (Pattern pattern : SQL_INJECTION_PATTERNS) {
            if (pattern.matcher(content).find()) {
                log.warn("检测到SQL注入模式: {} - 内容: {}", pattern.pattern(), content);
                return true;
            }
        }

        // 2. 检查危险字符模式
        for (Pattern pattern : DANGEROUS_PATTERNS) {
            if (pattern.matcher(content).find()) {
                log.warn("检测到危险字符模式: {} - 内容: {}", pattern.pattern(), content);
                return true;
            }
        }

        // 3. 使用现有的SQL工具类检查
        try {
            SqlUtil.filterKeyword(content);
        } catch (Exception e) {
            log.warn("SqlUtil检测到危险内容: {} - 错误: {}", content, e.getMessage());
            return true;
        }

        // 4. 检查XSS
        if (XssValidator.containsHtml(content)) {
            log.warn("检测到XSS攻击: {}", content);
            return true;
        }

        return false;
    }

    /**
     * 检查请求头是否包含恶意内容
     */
    private boolean hasMaliciousHeaders(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();

        // 检查常见的恶意请求头
        String[] suspiciousHeaders = {
                "X-Forwarded-For", "X-Real-IP", "X-Originating-IP",
                "X-Remote-IP", "X-Remote-Addr", "X-Client-IP"
        };

        for (String headerName : suspiciousHeaders) {
            String headerValue = headers.getFirst(headerName);
            if (StringUtils.isNotBlank(headerValue) && isDangerousContent(headerValue)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查User-Agent是否可疑
     */
    private boolean hasSuspiciousUserAgent(ServerHttpRequest request) {
        String userAgent = request.getHeaders().getFirst("User-Agent");
        if (StringUtils.isBlank(userAgent)) {
            return true; // 空的User-Agent是可疑的
        }

        // 检查常见的恶意User-Agent模式
        String[] suspiciousPatterns = {
                "sqlmap", "nmap", "nikto", "havij", "sqlninja", "pangolin",
                "sqlsus", "marathon", "absinthe", "jsql", "bsqlbf", "fimap",
                "w3af", "burpsuite", "owasp", "acunetix", "nessus", "openvas"
        };

        String lowerUserAgent = userAgent.toLowerCase();
        for (String pattern : suspiciousPatterns) {
            if (lowerUserAgent.contains(pattern)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 处理安全违规事件
     */
    private void handleSecurityViolation(ServerWebExchange exchange, String clientIp, String violationType, String path) {
        log.error("安全违规检测 - 类型: {}, IP: {}, 路径: {}", violationType, clientIp, path);

        // 增加可疑计数
        incrementSuspiciousCount(clientIp);

        // 如果达到阈值，临时封禁IP
        if (getSuspiciousCount(clientIp) >= MAX_SUSPICIOUS_COUNT) {
            blockIp(clientIp);
            log.warn("IP被自动封禁 - IP: {}, 违规次数: {}", clientIp, getSuspiciousCount(clientIp));
        }
    }

    /**
     * 获取客户端IP地址
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

        return remoteAddr;
    }

    /**
     * 检查是否为跳过安全检查的路径
     */
    private boolean isSkipSecurityPath(String path) {
        return SKIP_SECURITY_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * 增加IP的可疑计数
     */
    private void incrementSuspiciousCount(String ip) {
        suspiciousIpCount.merge(ip, 1, Integer::sum);
    }

    /**
     * 获取IP的可疑计数
     */
    private int getSuspiciousCount(String ip) {
        return suspiciousIpCount.getOrDefault(ip, 0);
    }

    /**
     * 重置IP的可疑计数
     */
    private void resetSuspiciousCount(String ip) {
        suspiciousIpCount.remove(ip);
    }

    /**
     * 封禁IP
     */
    private void blockIp(String ip) {
        ipBlockTime.put(ip, System.currentTimeMillis());
    }

    /**
     * 检查IP是否被封禁
     */
    private boolean isIpBlocked(String ip) {
        Long blockTime = ipBlockTime.get(ip);
        if (blockTime == null) {
            return false;
        }

        // 检查封禁时间是否已过期
        if (System.currentTimeMillis() - blockTime > IP_BLOCK_DURATION) {
            ipBlockTime.remove(ip);
            suspiciousIpCount.remove(ip);
            return false;
        }

        return true;
    }

    /**
     * 返回禁止访问响应
     */
    private Mono<Void> forbiddenResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        return WebFluxUtils.webFluxResponseWriter(
                response,
                HttpStatus.FORBIDDEN,
                message,
                R.FORBIDDEN
        );
    }

    @Override
    public int getOrder() {
        // 在用户认证过滤器之前执行，确保安全防护优先
        return Ordered.HIGHEST_PRECEDENCE;
    }
}