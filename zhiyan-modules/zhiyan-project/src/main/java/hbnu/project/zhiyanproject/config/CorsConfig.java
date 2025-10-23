package hbnu.project.zhiyanproject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS 跨域配置
 * 用于开发环境直接访问项目服务
 * 必须在 Security 过滤器之前执行
 *
 * @author AI Assistant
 */
@Configuration
public class CorsConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许的源（开发环境）
        config.addAllowedOriginPattern("*");  // 开发环境允许所有源
        
        // 允许的HTTP方法
        config.addAllowedMethod("*");  // 允许所有方法
        
        // 允许的请求头
        config.addAllowedHeader("*");
        
        // 允许携带凭证（如 Cookies）
        config.setAllowCredentials(true);
        
        // 预检请求的有效期（秒）
        config.setMaxAge(3600L);
        
        // 暴露的响应头
        config.addExposedHeader("*");
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}

