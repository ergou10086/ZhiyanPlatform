package hbnu.project.zhiyanai.config;

import hbnu.project.zhiyancommonbasic.utils.JwtUtils;
import hbnu.project.zhiyancommonsecurity.filter.JwtAuthenticationFilter;
import hbnu.project.zhiyancommonsecurity.service.RememberMeService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

/**
 * AI 模块安全配置
 * AI 模块不需要完整的 Spring Security 认证流程
 * 认证由网关或 Auth 服务完成，AI 模块只需要从 Token 中解析用户信息
 *
 * @author ErgouTree
 */
@Configuration
public class AISecurityConfig {

    /**
     * JWT 工具类 Bean
     * 用于解析 Token 获取用户信息
     */
    @Bean
    public JwtUtils jwtUtils() {
        return new JwtUtils();
    }

    /**
     * RememberMe 服务 Bean
     * AI 模块不需要 RememberMe 功能，提供空实现以满足依赖
     */
    @Bean
    @ConditionalOnMissingBean
    public RememberMeService rememberMeService() {
        return new RememberMeService() {
            @Override
            public String createRememberMeToken(Long userId) {
                return "";
            }

            @Override
            public Optional<Long> validateRememberMeToken(String token) {
                // AI模块不验证RememberMe token
                return Optional.empty();
            }

            @Override
            public void deleteRememberMeToken(String token) {
                // AI模块不需要删除RememberMe token
            }

            /**
             * 删除用户的RememberMe token
             */
            @Override
            public void deleteRememberMeToken(Long userId) {

            }
        };
    }

    /**
     * JWT 认证过滤器 Bean
     * 用于从请求中解析JWT并设置安全上下文
     */
    @Bean
    @ConditionalOnMissingBean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtUtils jwtUtils,
            RememberMeService rememberMeService) {
        return new JwtAuthenticationFilter(jwtUtils, rememberMeService);
    }
}