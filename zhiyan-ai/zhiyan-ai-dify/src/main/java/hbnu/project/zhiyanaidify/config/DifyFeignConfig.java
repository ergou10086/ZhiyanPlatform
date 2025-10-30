package hbnu.project.zhiyanaidify.config;

import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Dify Feign 配置
 * 用于配置 Feign 客户端调用 Dify API
 *
 * @author ErgouTree
 */
@Configuration
public class DifyFeignConfig {

    /**
     * Feign 日志级别配置
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    /**
     * Dify 专用请求拦截器
     * 重命名为 difyRequestInterceptor 避免与其他模块的 Bean 冲突
     */
    @Bean
    public RequestInterceptor difyRequestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Content-Type", "application/json");
            requestTemplate.header("Accept", "application/json");
        };
    }
}
