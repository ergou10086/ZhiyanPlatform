package hbnu.project.zhiyangateway.client;

import com.alibaba.nacos.api.model.v2.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import hbnu.project.zhiyancommonbasic.container.MapUtils;
import hbnu.project.zhiyangateway.model.TokenValidateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
 * 使用 WebClient 远程调用
 * 通过服务发现调用 Auth 模块的接口
 *
 * @author ErgouTree
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthServiceClient {

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private ObjectMapper objectMapper;

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
                    log.error("未找到 zhiyan-auth 认证服务实例");
                    return Mono.just(TokenValidateResponse.invalid("Auth服务不可用"));
                }

                // 选择第一个实例，可以后续优化为负载均衡
                ServiceInstance instance = instances.get(0);
                String authServiceUrl = instance.getUri().toString();

                log.debug("调用 Auth 服务验证 Token: {}", authServiceUrl);

                // 使用 WebClient 调用 Auth 服务的 /validate 接口
                return webClientBuilder.build()
                        .get()
                        .uri(authServiceUrl + "/zhiyan/auth/validate")
                        .header("Authorization", token)
                        .retrieve()
                        .bodyToMono(Result.class)  // Auth 返回的是 Result<TokenValidateResponse>
                        .timeout(Duration.ofSeconds(3))  // 设置超时时间
                        .map(result -> {
                            if (result != null && result.getData() != null) {
                                // 将 Result 中的 data 转换为 TokenValidateResponse
                                Object data = result.getData();
                                // 这里需要进行类型转换
                                return convertToTokenValidateResponse(data);
                            }
                            return TokenValidateResponse.invalid("Token验证失败");
                        })
                        .doOnError(e -> log.error("调用 Auth 服务验证 Token 失败: {}", e.getMessage()))
                        .onErrorReturn(TokenValidateResponse.invalid("验证服务异常"));
            } catch (Exception e) {
                log.error("获取 Auth 服务实例失败: {}", e.getMessage(), e);
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
                
                // 使用 MapUtils 安全地从 Map 中提取数据
                response.setIsValid(MapUtils.getBoolean(map, "isValid"));
                response.setUserId(MapUtils.getString(map, "userId"));
                response.setUsername(MapUtils.getString(map, "username"));
                response.setMessage(MapUtils.getString(map, "message"));
                response.setRemainingTime(MapUtils.getLong(map, "remainingTime"));
                
                return response;
            }

            // 情况3：使用 Jackson 进行通用转换
            return objectMapper.convertValue(data, TokenValidateResponse.class);
            
        } catch (Exception e) {
            log.error("类型转换失败: {}", e.getMessage(), e);
            return TokenValidateResponse.invalid("数据格式错误");
        }
    }
}
