package hbnu.project.zhiyanaicoze.config;

import hbnu.project.zhiyanaicoze.config.properties.CozeProperties;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebClient 配置
 * 用于配置响应式 HTTP 客户端，支持流式响应
 *
 * @author ErgouTree
 */
@Configuration
public class WebClientConfig {

    private final CozeProperties cozeProperties;

    public WebClientConfig(CozeProperties cozeProperties) {
        this.cozeProperties = cozeProperties;
    }

    /**
     * 配置 WebClient Bean
     * 用于调用 Coze API，支持 SSE 流式响应
     */
    @Bean
    public WebClient cozeWebClient() {
        // 配置 HttpClient
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, cozeProperties.getConnectTimeout() * 1000)
                .responseTimeout(Duration.ofSeconds(cozeProperties.getReadTimeout()))
                .doOnConnected(conn -> 
                    conn.addHandlerLast(new ReadTimeoutHandler(cozeProperties.getReadTimeout(), TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(cozeProperties.getConnectTimeout(), TimeUnit.SECONDS))
                );

        return WebClient.builder()
                .baseUrl(cozeProperties.getApiUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("Authorization", "Bearer " + cozeProperties.getToken())
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "text/event-stream")  // SSE 流式响应需要此头
                .build();
    }

    /**
     * 配置通用的 WebClient Bean
     * 用于服务间调用（如调用 Auth 服务）
     * 增加超时时间到 10 秒，避免 Auth 服务调用超时
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)  // 连接超时 10 秒
                .responseTimeout(Duration.ofSeconds(10))  // 响应超时 10 秒
                .doOnConnected(conn ->
                    conn.addHandlerLast(new ReadTimeoutHandler(10, TimeUnit.SECONDS))  // 读取超时 10 秒
                        .addHandlerLast(new WriteTimeoutHandler(10, TimeUnit.SECONDS))  // 写入超时 10 秒
                );

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json");
    }
}

