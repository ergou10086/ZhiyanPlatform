package hbnu.project.zhiyanai.config;

import hbnu.project.zhiyanai.config.properties.N8nProperties;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebClient 配置
 * 用于调用 N8N 工作流 API
 *
 * @author ErgouTree
 */
@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final N8nProperties n8nProperties;

    @Bean
    public WebClient n8nWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, n8nProperties.getTimeout())
                .responseTimeout(Duration.ofMillis(n8nProperties.getTimeout()))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(n8nProperties.getTimeout(), TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(n8nProperties.getTimeout(), TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .baseUrl(n8nProperties.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
//                // N8N 先不搞 API Key 认证
//                .defaultHeader("X-N8N-API-KEY", n8nProperties.getApiKey())
                .build();
    }
}
