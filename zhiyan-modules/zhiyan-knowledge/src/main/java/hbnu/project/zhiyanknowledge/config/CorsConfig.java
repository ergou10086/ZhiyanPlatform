package hbnu.project.zhiyanknowledge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS跨域配置
 * 允许前端开发服务器（localhost:8001）访问后端API
 *
 * @author ErgouTree
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许跨域的源（前端开发服务器）
        config.addAllowedOrigin("http://localhost:8001");
        config.addAllowedOrigin("http://127.0.0.1:8001");
        
        // 允许其他后端服务调用（微服务之间的调用）
        config.addAllowedOrigin("http://localhost:8091");  // 认证服务
        config.addAllowedOrigin("http://localhost:8093");  // 知识库服务自身
        config.addAllowedOrigin("http://localhost:8095");  // 项目服务
        
        // 允许的HTTP方法
        config.addAllowedMethod("*");
        
        // 允许的请求头
        config.addAllowedHeader("*");
        
        // 允许发送Cookie
        config.setAllowCredentials(true);
        
        // 预检请求的有效期（秒）
        config.setMaxAge(3600L);
        
        // 允许的响应头
        config.addExposedHeader("Authorization");
        config.addExposedHeader("X-Refresh-Token");
        config.addExposedHeader("Content-Disposition");  // 支持文件下载
        
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}

