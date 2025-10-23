package hbnu.project.zhiyanproject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 项目模块CORS配置
 * 允许前端跨域访问项目服务
 *
 * @author Tokito
 */
@Configuration
public class CorsConfig {
    
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许的源地址 - 支持多种访问方式
        config.addAllowedOrigin("http://localhost:8001");     // 前端开发服务器
        config.addAllowedOrigin("http://127.0.0.1:8001");     // 本地回环
        config.addAllowedOrigin("http://0.0.0.0:8001");       // 本地所有接口
        config.addAllowedOrigin("http://localhost:8000");     // 备用端口
        config.addAllowedOrigin("http://127.0.0.1:8000");     // 备用端口回环
        
        // 允许所有请求头
        config.addAllowedHeader("*");
        
        // 允许所有HTTP方法
        config.addAllowedMethod("*");
        
        // 允许携带凭证（cookies, authorization headers等）
        config.setAllowCredentials(true);
        
        // 预检请求的缓存时间（秒）
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
