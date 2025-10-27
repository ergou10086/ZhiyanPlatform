package hbnu.project.zhiyanproject.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Feign 配置类
 * 用于配置 Feign 客户端的请求拦截器
 * 
 * 主要功能：
 * - 将当前请求的 Authorization header 传递给 Feign 调用的下游服务
 * - 确保微服务间调用时 JWT Token 正确传递
 *
 * @author Tokito
 */
@Slf4j
@Configuration
public class FeignConfig {

    /**
     * Feign 请求拦截器
     * 自动将当前请求的 Authorization header 传递给 Feign 调用
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // 从当前请求上下文中获取 HttpServletRequest
                ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    
                    // 获取 Authorization header
                    String authorizationHeader = request.getHeader("Authorization");
                    
                    if (authorizationHeader != null && !authorizationHeader.isEmpty()) {
                        // 将 Authorization header 添加到 Feign 请求中
                        template.header("Authorization", authorizationHeader);
                        
                        log.debug("Feign请求拦截器: 添加Authorization header到请求 {}", template.url());
                        log.debug("Authorization: {}", authorizationHeader.substring(0, Math.min(20, authorizationHeader.length())) + "...");
                    } else {
                        log.warn("Feign请求拦截器: 当前请求没有Authorization header, 请求URL: {}", template.url());
                    }
                } else {
                    log.warn("Feign请求拦截器: 无法获取当前请求上下文, 请求URL: {}", template.url());
                }
            }
        };
    }
}


