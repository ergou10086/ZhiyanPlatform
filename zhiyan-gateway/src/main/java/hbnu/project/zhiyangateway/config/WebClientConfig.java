package hbnu.project.zhiyangateway.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient 配置
 * 用于服务间调用
 *
 * @author ErgouTree
 */
@Configuration
public class WebClientConfig {

    /**
     * 创建支持负载均衡的 WebClient.Builder
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }
}
