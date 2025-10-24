package hbnu.project.zhiyancommonsecurity.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Feign请求拦截器配置（全局配置）
 * 用于在微服务间调用时自动传递JWT认证信息
 * 
 * @author ErgouTree
 */
@Slf4j
@Configuration
@ConditionalOnClass(RequestInterceptor.class)
public class FeignRequestInterceptorConfig {

    /**
     * 创建Feign请求拦截器Bean
     * 自动将当前请求的JWT token添加到Feign请求头中
     * 
     * @return RequestInterceptor
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                try {
                    // 从当前请求的安全上下文中获取JWT token
                    String token = SecurityUtils.getToken();
                    
                    if (StringUtils.hasText(token)) {
                        // 将token添加到Feign请求的Authorization头中
                        template.header("Authorization", "Bearer " + token);
                        log.debug("Feign请求添加Authorization头: {} -> {}", 
                                template.method(), template.path());
                    } else {
                        log.debug("Feign请求未找到JWT token: {} -> {}", 
                                template.method(), template.path());
                    }
                } catch (Exception e) {
                    log.error("Feign请求拦截器处理失败: {} -> {}, 错误: {}", 
                            template.method(), template.path(), e.getMessage());
                }
            }
        };
    }
}
