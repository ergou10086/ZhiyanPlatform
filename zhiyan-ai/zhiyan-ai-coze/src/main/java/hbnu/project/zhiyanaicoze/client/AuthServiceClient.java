package hbnu.project.zhiyanaicoze.client;

import com.alibaba.nacos.api.model.v2.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import hbnu.project.zhiyanaicoze.model.response.TokenValidateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Auth 服务客户端
 * 使用 WebClient 远程调用 Auth 服务的 Token 验证接口
 * 通过服务发现调用 Auth 模块的接口
 *
 * @author ErgouTree
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final DiscoveryClient discoveryClient;
    private final ObjectMapper objectMapper;

    // 备用的 Auth 服务地址（当服务发现失败时使用）
    private static final String FALLBACK_AUTH_URL = "http://127.0.0.1:8091";

    /**
     * 验证 Token 是否有效
     *
     * @param token JWT Token（包含 Bearer 前缀）
     * @return Token 验证结果
     */
    public Mono<TokenValidateResponse> validateToken(String token) {
        log.info("[AuthServiceClient] 开始验证 Token，准备查找服务实例...");
        
        return Mono.fromCallable(() -> {
            // 从 Nacos 获取 zhiyan-auth 服务实例（同步调用，但很快）
            log.info("[AuthServiceClient] 正在从 Nacos 查找 zhiyan-auth 服务实例...");
            List<ServiceInstance> instances = discoveryClient.getInstances("zhiyan-auth");
            
            if (instances == null || instances.isEmpty()) {
                log.error("[AuthServiceClient] 未找到 zhiyan-auth 认证服务实例，使用备用地址: {}", FALLBACK_AUTH_URL);
                // 打印所有可用服务（用于调试）
                try {
                    List<String> services = discoveryClient.getServices();
                    log.warn("[AuthServiceClient] 当前可用的服务列表: {}", services);
                } catch (Exception e) {
                    log.warn("[AuthServiceClient] 无法获取服务列表: {}", e.getMessage());
                }
                return FALLBACK_AUTH_URL;  // 使用备用地址
            }
            
            log.info("[AuthServiceClient] 找到 {} 个 zhiyan-auth 服务实例", instances.size());
            
            // 优先选择 localhost 或 127.0.0.1 的实例（本地服务更可靠）
            ServiceInstance selectedInstance = instances.stream()
                    .filter(instance -> {
                        String host = instance.getHost();
                        return "localhost".equalsIgnoreCase(host) || 
                               "127.0.0.1".equals(host) ||
                               "0.0.0.0".equals(host);
                    })
                    .findFirst()
                    .orElse(instances.get(0));  // 如果没有本地实例，使用第一个
            
            String authServiceUrl = selectedInstance.getUri().toString();
            log.info("[AuthServiceClient] 选择服务实例: {} (host={}, port={})", 
                    authServiceUrl, selectedInstance.getHost(), selectedInstance.getPort());
            
            return authServiceUrl;
        })
        .subscribeOn(Schedulers.boundedElastic())  // 在弹性线程池上执行服务发现，避免阻塞
        .flatMap(authServiceUrl -> {
            log.info("[AuthServiceClient] 使用服务URL: {}", authServiceUrl);
            log.info("[AuthServiceClient] 开始调用 Auth 服务验证 Token: {}", authServiceUrl);
            
            // 使用 WebClient 调用 Auth 服务的 /validate 接口
            // Auth 服务返回的是 Result<TokenValidateResponse>
            return webClientBuilder.build()
                    .get()
                    .uri(authServiceUrl + "/zhiyan/auth/validate")
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(Result.class)
                    .timeout(Duration.ofSeconds(10))  // 增加超时时间到 10 秒
                    .doOnSubscribe(s -> log.info("[AuthServiceClient] WebClient 请求已订阅，等待响应..."))
                    .doOnSuccess(result -> log.info("[AuthServiceClient] 收到 Auth 服务响应: {}", result != null ? "success" : "null"))
                    .map(result -> {
                        if (result != null && result.getData() != null) {
                            // 将 Result 中的 data 转换为 TokenValidateResponse
                            Object data = result.getData();
                            TokenValidateResponse response = convertToTokenValidateResponse(data);
                            log.info("[AuthServiceClient] Token 验证结果: isValid={}, userId={}", 
                                    response.getIsValid(), response.getUserId());
                            return response;
                        }
                        log.warn("[AuthServiceClient] Auth 服务返回的结果为空或 data 为空");
                        return TokenValidateResponse.invalid("Token验证失败：响应数据为空");
                    })
                    .doOnError(e -> log.error("[AuthServiceClient] 调用 Auth 服务验证 Token 失败: {} (类型: {})", 
                            e.getMessage(), e.getClass().getSimpleName(), e))
                    .onErrorResume(error -> {
                        log.error("[AuthServiceClient] 捕获到错误，返回默认响应: {}", error.getMessage());
                        return Mono.just(TokenValidateResponse.invalid("验证服务异常: " + error.getClass().getSimpleName()));
                    });
        })
        .timeout(Duration.ofSeconds(15))  // 整个操作的总超时时间（包括服务发现）
        .doOnError(e -> log.error("[AuthServiceClient] Token 验证流程超时或失败: {}", e.getMessage(), e))
        .onErrorReturn(TokenValidateResponse.invalid("验证服务超时或异常"))
        .doOnSuccess(response -> log.info("[AuthServiceClient] Token 验证流程完成: isValid={}", response.getIsValid()));
    }

    /**
     * 将返回的数据转换为 TokenValidateResponse
     * 处理从 Result 中获取的数据，支持多种数据类型
     *
     * @param data Result.getData() 返回的数据对象
     * @return TokenValidateResponse 实例
     */
    @SuppressWarnings("unchecked")
    private TokenValidateResponse convertToTokenValidateResponse(Object data) {
        try {
            // 情况1：如果已经是 TokenValidateResponse 类型（理想情况）
            if (data instanceof TokenValidateResponse) {
                return (TokenValidateResponse) data;
            }

            // 情况2：如果是 Map 类型（常见情况）
            if (data instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) data;
                TokenValidateResponse response = new TokenValidateResponse();
                
                // 安全地从 Map 中提取数据
                response.setIsValid(getBoolean(map, "isValid"));
                response.setUserId(getString(map, "userId"));
                response.setUsername(getString(map, "username"));
                response.setRoles(getString(map, "roles"));
                response.setMessage(getString(map, "message"));
                response.setRemainingTime(getLong(map, "remainingTime"));
                
                return response;
            }

            // 情况3：使用 Jackson 进行通用转换
            return objectMapper.convertValue(data, TokenValidateResponse.class);
            
        } catch (Exception e) {
            log.error("[AuthServiceClient] 类型转换失败: {}", e.getMessage(), e);
            return TokenValidateResponse.invalid("数据格式错误");
        }
    }

    /**
     * 从 Map 中安全获取 Boolean 值
     */
    private Boolean getBoolean(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof String) return Boolean.parseBoolean((String) value);
        return null;
    }

    /**
     * 从 Map 中安全获取 String 值
     */
    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 从 Map 中安全获取 Long 值
     */
    private Long getLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof Number) return ((Number) value).longValue();
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}

