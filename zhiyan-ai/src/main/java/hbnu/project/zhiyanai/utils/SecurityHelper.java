package hbnu.project.zhiyanai.utils;

import hbnu.project.zhiyancommonbasic.constants.TokenConstants;
import hbnu.project.zhiyancommonbasic.utils.JwtUtils;
import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * AI 模块安全辅助工具类
 * 简化版的 SecurityUtils，专门用于 AI 模块
 * 
 * 功能：从 HTTP 请求中解析 JWT Token，获取用户信息
 *
 * @author ErgouTree
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityHelper {

    private final JwtUtils jwtUtils;

    /**
     * 获取当前用户 ID
     *
     * @return 用户 ID，如果未登录或 Token 无效则返回 null
     */
    public Long getUserId() {
        try {
            String token = getToken();
            if (StringUtils.isBlank(token)) {
                return null;
            }

            Claims claims = jwtUtils.getClaims(token);
            if (claims == null) {
                return null;
            }

            Object userIdObj = claims.get(TokenConstants.JWT_CLAIM_USER_ID);
            return switch (userIdObj) {
                case null -> null;
                case Integer i -> i.longValue();
                case Long l -> l;
                case String s -> Long.parseLong(s);
                default -> null;
            };

        } catch (Exception e) {
            log.debug("获取当前用户ID失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取当前请求的 JWT Token
     *
     * @return JWT Token，如果没有则返回 null
     */
    public String getToken() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return null;
            }
            HttpServletRequest request = attributes.getRequest();
            return getToken(request);
        } catch (Exception e) {
            log.debug("获取Token失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 HttpServletRequest 中获取 JWT Token
     *
     * @param request HTTP 请求对象
     * @return JWT Token，如果没有则返回 null
     */
    public String getToken(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        // 1. 从 Authorization 头获取
        String token = request.getHeader("Authorization");
        if (StringUtils.isNotBlank(token)) {
            return replaceTokenPrefix(token);
        }

        // 2. 从请求参数获取
        token = request.getParameter("token");
        if (StringUtils.isNotBlank(token)) {
            return token;
        }

        return null;
    }

    /**
     * 移除 Token 前缀（如 Bearer）
     *
     * @param token 原始 token 字符串
     * @return 处理后的 token
     */
    private String replaceTokenPrefix(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }

        // 移除 Bearer 前缀
        if (token.startsWith(TokenConstants.TOKEN_TYPE_BEARER + " ")) {
            return token.substring(TokenConstants.TOKEN_TYPE_BEARER.length() + 1);
        }

        if (token.startsWith("Bearer ")) {
            return token.substring(7);
        }

        return token;
    }

    /**
     * 获取当前用户名
     *
     * @return 用户名
     */
    public String getUsername() {
        try {
            String token = getToken();
            if (StringUtils.isNotBlank(token)) {
                String userId = jwtUtils.parseToken(token);
                if (StringUtils.isNotBlank(userId)) {
                    return userId;
                }
            }
        } catch (Exception e) {
            log.debug("获取当前用户名失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 验证 JWT Token 是否有效
     *
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean isValidToken(String token) {
        try {
            if (StringUtils.isBlank(token)) {
                return false;
            }
            return jwtUtils.validateToken(token);
        } catch (Exception e) {
            log.debug("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证当前请求的 Token 是否有效
     *
     * @return 是否有效
     */
    public boolean isValidCurrentToken() {
        String token = getToken();
        return isValidToken(token);
    }
}

