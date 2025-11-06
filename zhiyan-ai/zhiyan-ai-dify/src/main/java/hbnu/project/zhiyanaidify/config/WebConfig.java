package hbnu.project.zhiyanaidify.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.resolver.DefaultAddressResolverGroup;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Web 配置
 *
 * @author ErgouTree
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }

    /**
     * RestTemplate Bean 配置
     * 用于 HTTP 请求调用 Dify API
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000); // 连接超时 30 秒
        factory.setReadTimeout(300000);   // 读取超时 5 分钟（AI 调用可能较慢）
        return new RestTemplate(factory);
    }

    /**
     * WebClient.Builder Bean 配置
     * 解决 DNS 解析超时问题
     * 
     * 关键配置：
     * 1. 使用 JVM 默认的 DNS 解析器（同步解析）而不是 Netty 的异步解析器
     * 2. 配置合理的连接超时、读写超时
     * 3. 配置连接池以复用连接
     * 4. 增加响应超时时间以支持 AI 长时间响应
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        // 配置连接池
        ConnectionProvider connectionProvider = ConnectionProvider.builder("dify-connection-pool")
                .maxConnections(100)                    // 最大连接数
                .maxIdleTime(Duration.ofSeconds(30))    // 连接最大空闲时间
                .maxLifeTime(Duration.ofMinutes(5))     // 连接最大生命周期
                .pendingAcquireTimeout(Duration.ofSeconds(60))  // 获取连接的超时时间
                .evictInBackground(Duration.ofSeconds(120))     // 后台清理空闲连接的间隔
                .build();

        // 配置 HttpClient
        HttpClient httpClient = HttpClient.create(connectionProvider)
                // 使用 JVM 默认的 DNS 解析器（同步解析）
                // 这会避免 Netty 异步 DNS 解析器的超时问题
                .resolver(DefaultAddressResolverGroup.INSTANCE)
                // 配置连接超时
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)  // 30 秒连接超时
                // 配置 TCP 参数
                .option(ChannelOption.SO_KEEPALIVE, true)  // 启用 TCP keepalive
                .option(ChannelOption.TCP_NODELAY, true)   // 禁用 Nagle 算法，减少延迟
                // 配置读写超时
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(300, TimeUnit.SECONDS))   // 5 分钟读超时（AI 响应可能很慢）
                        .addHandlerLast(new WriteTimeoutHandler(30, TimeUnit.SECONDS)))  // 30 秒写超时
                // 配置响应超时（整个请求-响应的超时时间）
                .responseTimeout(Duration.ofMinutes(5));  // 5 分钟响应超时

        // 创建 ReactorClientHttpConnector
        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);

        // 配置 WebClient.Builder
        return WebClient.builder()
                .clientConnector(connector)
                // 配置默认的缓冲区大小（用于处理大响应）
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(1024 * 1024));  // 1 MB 缓冲区
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
        
        // 启用缩进输出
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        return mapper;
    }
}
