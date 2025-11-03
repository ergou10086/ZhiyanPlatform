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

    /**
     * 验证 Token 是否有效
     *
     * @param token JWT Token（包含 Bearer 前缀）
     * @return Token 验证结果
     */
    public Mono<TokenValidateResponse> validateToken(String token) {
        return Mono.defer(() -> {
            try {
                // 从 Nacos 获取 zhiyan-auth 服务实例
                List<ServiceInstance> instances = discoveryClient.getInstances("zhiyan-auth");
                if (instances == null || instances.isEmpty()) {
                    log.error("[AuthServiceClient] 未找到 zhiyan-auth 认证服务实例");
                    return Mono.just(TokenValidateResponse.invalid("Auth服务不可用"));
                }

                // 选择第一个实例，可以后续优化为负载均衡
                ServiceInstance instance = instances.get(0);
                String authServiceUrl = instance.getUri().toString();

                log.debug("[AuthServiceClient] 调用 Auth 服务验证 Token: {}", authServiceUrl);

                // 使用 WebClient 调用 Auth 服务的 /validate 接口
                // Auth 服务返回的是 Result<TokenValidateResponse>
                return webClientBuilder.build()
                        .get()
                        .uri(authServiceUrl + "/zhiyan/auth/validate")
                        .header("Authorization", token)
                        .retrieve()
                        .bodyToMono(Result.class)
                        .timeout(Duration.ofSeconds(3))
                        .map(result -> {
                            if (result != null && result.getData() != null) {
                                // 将 Result 中的 data 转换为 TokenValidateResponse
                                Object data = result.getData();
                                return convertToTokenValidateResponse(data);
                            }
                            return TokenValidateResponse.invalid("Token验证失败");
                        })
                        .doOnError(e -> log.error("[AuthServiceClient] 调用 Auth 服务验证 Token 失败: {}", e.getMessage(), e))
                        .onErrorReturn(TokenValidateResponse.invalid("验证服务异常"));
            } catch (Exception e) {
                log.error("[AuthServiceClient] 获取 Auth 服务实例失败: {}", e.getMessage(), e);
                return Mono.just(TokenValidateResponse.invalid("服务发现失败"));
            }
        });
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

