package hbnu.project.zhiyancommonsecurity.config;

import hbnu.project.zhiyancommonsecurity.filter.JwtAuthenticationFilter;
import hbnu.project.zhiyancommonsecurity.interceptor.HeaderInterceptor;
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
     *
     * @return HeaderInterceptor实例
     */
    @Bean
    public HeaderInterceptor headerInterceptor() {
        return new HeaderInterceptor();
    }


    /**
     * 注册拦截器
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(headerInterceptor())
                // 拦截所有请求
                .addPathPatterns("/**")
                // 排除不需要拦截的路径
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
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF
                .csrf(AbstractHttpConfigurer::disable)
                // 设置会话创建策略为无状态
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 配置授权规则
                .authorizeHttpRequests(auth -> auth
                        // 公开接口
                        .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/refresh-token").permitAll()
                        .requestMatchers("/api/auth/send-code", "/api/auth/verify-code").permitAll()
                        .requestMatchers("/error", "/favicon.ico").permitAll()
                        .requestMatchers("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // 其他请求需要认证
                        .anyRequest().authenticated()
                )
                // 配置认证提供者
                .authenticationProvider(authenticationProvider())

                // TODO:配置登录

                // TODO:配置登出

                // 添加JWT过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
