package hbnu.project.zhiyanauth.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许的源地址 - 支持多种访问方式
        config.addAllowedOrigin("http://localhost:8001");     // 本地开发
        config.addAllowedOrigin("http://127.0.0.1:8001");    // 本地回环
        config.addAllowedOrigin("http://0.0.0.0:8001");      // 本地所有接口
        
        // 支持团队成员通过IP访问（根据实际网络环境调整）
        // 例如：config.addAllowedOrigin("http://192.168.1.100:8001");
        // 例如：config.addAllowedOrigin("http://192.168.0.100:8001");
        
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