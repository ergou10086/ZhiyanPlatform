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
                        "/health",                    // 健康检查端点（兼容性）
                        "/swagger-ui/**",
                        "/swagger-resources/**",
                        "/v3/api-docs/**",
                        "/doc.html",
                        "/doc.html/**"
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
                                "/health",                    // 健康检查端点（兼容性）
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/doc.html",
                                "/doc.html/**"
                        ).permitAll()

                        // 用户服务内部接口 - 无需登录（供服务间调用）
                        .requestMatchers(

                        "/zhiyan/users/email",              // 根据邮箱查询用户
                                "/zhiyan/users/name",               // 根据姓名查询用户
                                "/zhiyan/users/internal/**",        // 内部接口（按ID查询用户）
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

                        // 用户项目头像管理接口 - 认证用户可以访问
                        .requestMatchers(
                                "/zhiyan/users/avatar/**",
                                "/zhiyan/projects/upload-image",
                                "/zhiyan/projects/delete-image",
                                "/zhiyan/projects/get-image",
                                "/zhiyan/projects/get-images",
                                "/zhiyan/projects/get-image-url",
                                "/zhiyan/projects/get-image-urls",
                                "/zhiyan/projects/get-image-url"
                        )
                        .authenticated()

                        // TODO: 临时开放权限管理接口，用于初始化权限数据 - 开发完成后需要删除此行
                        .requestMatchers("/auth/permissions/**").permitAll()
                        // TODO: 临时开放权限管理接口，用于初始化角色数据 - 开发完成后需要删除此行
                        .requestMatchers("/auth/roles/**").permitAll()

                        // 项目服务公开接口 - 无需登录（供项目广场等公开展示使用）
                        .requestMatchers(
                                "/api/projects/public",
                                "/api/projects/public/**",
                                "/api/projects/search/public"
                        ).permitAll()

                        // 项目成员公开接口 - 无需登录（查看项目成员信息）
                        .requestMatchers(
                                "/api/project-members/*/public"      // 查看项目公开成员列表
                        ).permitAll()

                        // 项目服务内部接口 - 无需登录（供微服务间调用）
                        .requestMatchers(
                                "/api/*/members/check",        // 检查成员关系
                                "/api/*/owner/check",          // 检查拥有者
                                "/api/*/permissions/check"     // 检查权限
                        ).permitAll()

                        // 文件下载接口 - 使用 permitAll，支持通过查询参数传递token，权限在 Controller 层手动验证
                        // 这样可以支持浏览器直接下载（无法设置Authorization header）
                        .requestMatchers(
                                "/zhiyan/achievement/file/*/download"  // 成果文件下载
                        ).permitAll()

                        // AI 流式接口 - 使用 permitAll，权限在 Controller 层通过 @PreAuthorize 控制
                        // 这样可以避免异步请求时的二次权限检查导致的 AccessDeniedException
                        .requestMatchers("/api/ai/**").permitAll()
                        
                        // Coze AI 接口 - 使用 permitAll，权限在 Controller 层通过 SecurityHelper 控制
                        // 这样可以避免异步请求时的二次权限检查导致的 AccessDeniedException
                        .requestMatchers("/api/coze/**").permitAll()

                        .requestMatchers("/api/coze/**").permitAll()

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