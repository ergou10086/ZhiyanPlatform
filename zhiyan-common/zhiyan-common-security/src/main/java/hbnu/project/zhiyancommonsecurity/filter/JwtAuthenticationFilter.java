package hbnu.project.zhiyancommonsecurity.filter;

import hbnu.project.zhiyancommonbasic.constants.TokenConstants;
import hbnu.project.zhiyancommonbasic.utils.JwtUtils;
import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import hbnu.project.zhiyancommonsecurity.context.LoginUserBody;
import hbnu.project.zhiyancommonsecurity.context.SecurityContextHolder;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 * 用于验证JWT token并设置认证信息
 * 在请求处理过程中验证 JWT 令牌并设置认证信息
 *
 * @author akoiv
 */
@Slf4j
@Component
@RequiredArgsConstructor
// 继承OncePerRequestFilter，确保每个请求只被过滤一次
public class JwtAuthenticationFilter extends OncePerRequestFilter{

    private final JwtUtils jwtUtils;


    /**
     * 从Authorization头提取token
     */
    private String extractTokenFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.isNotBlank(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // 移除"Bearer "前缀
        }
        return null;
    }


    /**
     * 处理每个请求的认证逻辑
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try{
            // 1. 从请求中获取JWT token
            String token = ServletRequestUtils.getStringParameter(request, "token");

            // 从Authorization头提取token
            if(StringUtils.isBlank(token)){
                token = extractTokenFromHeader(request);
            }


            // 2. 检查token是否存在且有效2
            if(StringUtils.isNotBlank(token) && jwtUtils.validateToken(token)){

//                // 3. 检查token是否在黑名单中
//                if () {
//                    log.debug("Token已在黑名单中，拒绝认证: {}", token.substring(0, Math.min(token.length(), 10)) + "...");
//                    // Token在黑名单中，清理上下文并继续过滤器链
//                    clearSecurityContext();
//                    filterChain.doFilter(request, response);
//                    return;
//                }

                // 3.解析token获取用户信息
                Claims claims = jwtUtils.getClaims(token);

                if(claims != null){
                    // 4. 从token的载荷(claims)中提取用户信息
                    // 从自定义声明中获取用户ID
                    String userIdStr = String.valueOf(claims.get(TokenConstants.JWT_CLAIM_USER_ID));
                    // 从主题(Subject)中获取邮箱(也可以是用户名)
                    String email = claims.getSubject();

                    // 5.验证提取的用户信息是否有效
                    if (StringUtils.isNotBlank(userIdStr) && StringUtils.isNotBlank(email)) {
                        // 6. 构建简化的LoginUserBody对象
                        // 这里只包含基本信息，权限信息可以在后续需要时再加载
                        LoginUserBody loginUser = LoginUserBody.builder()
                                .userId(Long.valueOf(userIdStr))
                                .email(email)
                                .build();

                        // 7.设置到Spring Security上下文
                        // 创建认证令牌，包含用户信息，凭证为null，权限列表为null
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(loginUser, null, null);

                        // 设置认证详情，如请求IP、会话ID等
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // 8.将认证信息设置到Spring Security的上下文中
                        org.springframework.security.core.context.SecurityContextHolder.getContext()
                                .setAuthentication(authToken);

                        // 9. 设置到自定义上下文，方便业务代码中获取当前登录用户
                        SecurityContextHolder.setLoginUser(loginUser);

                        // 输出调试日志
                        log.debug("JWT认证成功，用户ID: {}, 邮箱: {}", userIdStr, email);
                    }
                }
            }
        }catch (Exception e){
            // 认证过程中发生异常时输出调试日志
            log.debug("JWT认证失败: {}", e.getMessage());
            // 认证失败时清理上下文，避免残留无效信息
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
            SecurityContextHolder.remove();
        }

        // 继续过滤器链，让请求进入下一个过滤器或目标资源
        filterChain.doFilter(request, response);
    }



    /**
     * 清理安全上下文
     */
    private void clearSecurityContext() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        SecurityContextHolder.remove();
    }
}
