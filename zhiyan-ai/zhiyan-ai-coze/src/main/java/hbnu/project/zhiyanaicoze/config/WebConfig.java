package hbnu.project.zhiyanaicoze.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Web 配置
 * 注意：此模块使用 WebFlux（响应式），需要使用 WebFilter 配置 CORS
 *
 * @author ErgouTree
 */
@Configuration
public class WebConfig {

    /**
     * CORS 跨域配置 WebFilter
     * 用于 WebFlux 响应式应用
     * 设置最高优先级，确保在所有过滤器之前执行
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public WebFilter corsWebFilter() {
        return (ServerWebExchange exchange, WebFilterChain chain) -> {
            var request = exchange.getRequest();
            
            // 判断是否为跨域请求
            if (CorsUtils.isCorsRequest(request)) {
                var response = exchange.getResponse();
                var headers = response.getHeaders();
                
                // 设置允许的源（精确匹配，支持 credentials）
                String origin = request.getHeaders().getFirst(HttpHeaders.ORIGIN);
                if (origin != null && (
                    origin.equals("http://localhost:8001") || 
                    origin.equals("http://127.0.0.1:8001")
                )) {
                    headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
                    headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
                }
                
                // 允许的请求头
                headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");
                
                // 允许的HTTP方法
                headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, 
                    "GET, POST, PUT, DELETE, OPTIONS, PATCH");
                
                // 暴露的响应头
                headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, 
                    "Content-Type, Content-Length, Authorization, X-Total-Count");
                
                // 预检请求缓存时间
                headers.add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
                
                // 处理 OPTIONS 预检请求（必须在实际请求之前处理）
                if (request.getMethod() == HttpMethod.OPTIONS) {
                    response.setStatusCode(HttpStatus.OK);
                    return Mono.empty();
                }
            }
            
            return chain.filter(exchange);
        };
    }

    /**
     * RestTemplate Bean 配置
     * 用于 HTTP 请求调用 Coze API
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000); // 连接超时 30 秒
        factory.setReadTimeout(300000);   // 读取超时 5 分钟（AI 调用可能较慢）
        return new RestTemplate(factory);
    }

    /**
     * ObjectMapper Bean 配置
     * 用于 JSON 序列化和反序列化
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // 注册 Java 8 时间模块
        mapper.registerModule(new JavaTimeModule());
        
        // 配置序列化选项
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        
        // ✅ 禁用缩进输出（生成单行 JSON，用于 SSE 流式传输）
        mapper.disable(SerializationFeature.INDENT_OUTPUT);
        
        return mapper;
    }
}

