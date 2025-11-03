package hbnu.project.zhiyancommonsse.config;

import hbnu.project.zhiyancommonsse.controller.SseController;
import hbnu.project.zhiyancommonsse.core.SseEmitterManager;
import hbnu.project.zhiyancommonsse.listener.SseTopicListener;
import hbnu.project.zhiyancommonsse.service.DifyStreamService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * SSE 自动装配
 *
 * @author yui, ErgouTree
 */
@AutoConfiguration
@ConditionalOnProperty(value = "sse.enabled", havingValue = "true")
@EnableConfigurationProperties(SseProperties.class)
public class SseAutoConfiguration {

    @Bean
    public SseEmitterManager sseEmitterManager() {
        return new SseEmitterManager();
    }

    @Bean
    public SseTopicListener sseTopicListener(SseEmitterManager sseEmitterManager) {
        return new SseTopicListener(sseEmitterManager);
    }

    @Bean
    public SseController sseController(SseEmitterManager sseEmitterManager) {
        return new SseController(sseEmitterManager);
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    @ConditionalOnProperty(value = "dify.stream-enabled", havingValue = "true", matchIfMissing = false)
    public DifyStreamService difyStreamService(WebClient.Builder webClientBuilder) {
        return new DifyStreamService(webClientBuilder);
    }
}