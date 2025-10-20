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
 * 集成JWT + RememberMe + 自动续签机制
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
                                "/zhiyan/auth/clear-remember-me",
                                "/zhiyan/auth/check-email"
                        ).permitAll()

                        // 系统基础接口 - 无需登录
                        .requestMatchers(
                                "/error",
                                "/favicon.ico",
                                "/actuator/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // 用户服务内部接口 - 无需登录（供服务间调用）
                        .requestMatchers(
                                "/zhiyan/users/email",              // 根据邮箱查询用户
                                "/zhiyan/users/name",               // 根据姓名查询用户
                                "/zhiyan/users/batch-query",        // 批量查询用户
                                "/zhiyan/users/*/has-permission",   // 检查用户权限
                                "/zhiyan/users/*/has-permissions",  // 批量检查权限
                                "/zhiyan/users/*/has-role"          // 检查用户角色
                        ).permitAll()

                        // 权限校验接口 - 无需登录（供API网关和其他服务调用）
                        .requestMatchers(
                                "/zhiyan/auth/check-permission",    // 权限校验
                                "/zhiyan/auth/check-permissions"    // 批量权限校验
                        ).permitAll()

                        // TODO: 临时开放权限管理接口，用于初始化权限数据 - 开发完成后需要删除此行
                        .requestMatchers("/auth/permissions/**").permitAll()
                        // TODO: 临时开放权限管理接口，用于初始化角色数据 - 开发完成后需要删除此行
                        .requestMatchers("/auth/roles/**").permitAll()

                        // 项目服务公开接口 - 无需登录（供项目广场等公开展示使用）
                        .requestMatchers(
                                "/api/projects/public/**"            // 公开项目列表、项目广场等
                        ).permitAll()

                        // 其他所有接口需要认证，具体权限由 @PreAuthorize 注解控制
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