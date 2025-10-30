package hbnu.project.zhiyanaicoze.config;

import feign.Logger;
import feign.RequestInterceptor;
import hbnu.project.zhiyanaicoze.config.properties.CozeProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Coze Feign 配置
 * 用于配置 Feign 客户端调用 Coze API
 *
 * @author ErgouTree
 */
@Configuration
public class CozeFeignConfig {

    private final CozeProperties cozeProperties;

    public CozeFeignConfig(CozeProperties cozeProperties) {
        this.cozeProperties = cozeProperties;
    }

    /**
     * Feign 日志级别配置
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }


    /**
     * Coze 专用请求拦截器
     * 用于添加 Coze API 所需的请求头
     */
    @Bean
    public RequestInterceptor cozeRequestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Content-Type", "application/json");
            requestTemplate.header("Accept", "application/json");
            // 添加 Coze API 的 Authorization 头
            requestTemplate.header("Authorization", "Bearer " + cozeProperties.getToken());
        };
    }
}

