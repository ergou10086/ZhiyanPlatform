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
                .defaultHeader("Accept", "application/json")
                .build();
    }
}

