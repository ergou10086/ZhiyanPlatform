package hbnu.project.zhiyancommonsecurity.config;

import hbnu.project.zhiyancommonsecurity.filter.JwtAuthenticationFilter;
import hbnu.project.zhiyancommonsecurity.interceptor.HeaderInterceptor;

import hbnu.project.zhiyancommonsecurity.service.RememberMeService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring Security安全配置
 * 集成JWT + RememberMe + 自动续签机制
 * @author ErgouTree
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig implements WebMvcConfigurer {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;



    /**
     * 密码编码器Bean
     * 使用BCrypt算法进行密码加密
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 认证提供者配置
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * 请求头拦截器Bean
     */
    @Bean
    public HeaderInterceptor headerInterceptor() {
        return new HeaderInterceptor();
    }

    /**
     * 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(headerInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/error",
                        "/favicon.ico",
                        "/actuator/**",
                        "/swagger-ui/**",
                        "/swagger-resources/**",
                        "/v3/api-docs/**"
                );
    }

    /**
     * 安全过滤器链配置
     * 集成JWT、RememberMe自动续签机制
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 关闭CSRF
                .csrf(AbstractHttpConfigurer::disable)
                // 无状态会话
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 授权配置
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/zhiyan/auth/login",
                                "/zhiyan/auth/logout",
                                "/zhiyan/auth/register",
                                "/zhiyan/auth/refresh-token",
                                "/zhiyan/auth/refresh",
                                "/zhiyan/auth/send-verfcode",
                                "/zhiyan/auth/verify-code",
                                "/zhiyan/auth/forgot-password",
                                "/zhiyan/auth/reset-password",
                                "/zhiyan/auth/auto-login-check",
                                "/zhiyan/auth/clear-remember-me"
                        ).permitAll()
                        .requestMatchers(
                                "/error",
                                "/favicon.ico",
                                "/actuator/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                // 认证提供者
                .authenticationProvider(authenticationProvider())
                // 添加JWT认证过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 添加RememberMe自动登录支持（交给自定义逻辑处理）
                .rememberMe(remember -> remember.disable());


        return http.build();
    }

}
