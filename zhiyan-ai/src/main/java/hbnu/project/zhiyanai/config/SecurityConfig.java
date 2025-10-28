package hbnu.project.zhiyanai.config;

import hbnu.project.zhiyancommonbasic.utils.JwtUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 模块安全配置
 * AI 模块不需要完整的 Spring Security 认证流程
 * 认证由网关或 Auth 服务完成，AI 模块只需要从 Token 中解析用户信息
 *
 * @author ErgouTree
 */
@Configuration
public class SecurityConfig {

    /**
     * JWT 工具类 Bean
     * 用于解析 Token 获取用户信息
     */
    @Bean
    public JwtUtils jwtUtils() {
        return new JwtUtils();
    }
}

