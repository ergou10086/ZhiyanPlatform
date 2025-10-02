package hbnu.project.zhiyancommonsecurity.utils;

import hbnu.project.zhiyancommonbasic.constants.TokenConstants;
import hbnu.project.zhiyancommonbasic.utils.JwtUtils;
import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import hbnu.project.zhiyancommonsecurity.context.LoginUserBody;
import hbnu.project.zhiyancommonsecurity.context.SecurityContextHolder;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * 安全工具类
 * 提供用户认证、权限验证、Token处理等安全相关功能
 *
 * @author ErgouTree
 */
@Slf4j
@Component
public class SecurityUtils {

    private static JwtUtils jwtUtils;

    @Autowired
    public void setJwtUtils(JwtUtils jwtUtils) {
        SecurityUtils.jwtUtils = jwtUtils;
    }

    /**
     * 获取当前用户ID
     * 优先从上下文获取，如果没有则从JWT Token中解析
     *
     * @return 用户ID，如果未登录或Token无效则返回null
     */
    public static Long getUserId() {
        try {
            // 优先从优化后的上下文获取
            Long userId = SecurityContextHolder.getUserId();
            if (userId != null && userId > 0) {
                return userId;
            }

            // 从JWT Token解析
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
     * 获取当前用户名
     * 优先从JWT Token中获取，如果没有则从Spring Security上下文获取
     *
     * @return 用户名
     */
    public static String getUsername() {
        try {
            // 优先从JWT Token中获取用户名
            String token = getToken();
            if (StringUtils.isNotBlank(token)) {
                String userId = jwtUtils.parseToken(token);
                if (StringUtils.isNotBlank(userId)) {
                    return userId;
                }
            }

            // 从Spring Security上下文获取
            Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && !"anonymousUser".equals(authentication.getName())) {
                return authentication.getName();
            }
        } catch (Exception e) {
            log.debug("获取当前用户名失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 获取当前登录用户的详细信息（重命名为避免冲突）
     * 从JWT Token中解析用户相关信息
     *
     * @return 用户信息Map，包含用户ID、用户名等
     */
    public static Map<String, Object> getLoginUserInfo() {
        try {
            String token = getToken();
            if (StringUtils.isBlank(token)) {
                return null;
            }

            Claims claims = jwtUtils.getClaims(token);
            if (claims == null) {
                return null;
            }

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", claims.get(TokenConstants.JWT_CLAIM_USER_ID));
            userInfo.put("username", claims.getSubject());
            userInfo.put("tokenType", claims.get(TokenConstants.JWT_CLAIM_TOKEN_TYPE));
            userInfo.put("issuer", claims.getIssuer());
            userInfo.put("issuedAt", claims.getIssuedAt());
            userInfo.put("expiration", claims.getExpiration());

            return userInfo;
        } catch (Exception e) {
            log.debug("获取登录用户信息失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取当前请求的JWT Token
     *
     * @return JWT Token，如果没有则返回null
     */
    public static String getToken() {
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
     * 从HttpServletRequest中获取JWT Token
     *
     * @param request HTTP请求对象
     * @return JWT Token，如果没有则返回null
     */
    public static String getToken(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        // 1. 从Authorization头获取
        String token = request.getHeader("Authorization");
        if (StringUtils.isNotBlank(token)) {
            return replaceTokenPrefix(token);
        }

        // 2. 从请求参数获取（用于某些特殊场景）
        token = request.getParameter("token");
        if (StringUtils.isNotBlank(token)) {
            return token;
        }

        // 3. 从Cookie获取（可选，根据需要启用）    -- by asddjv
        // Cookie[] cookies = request.getCookies();
        // if (cookies != null) {
        //     for (Cookie cookie : cookies) {
        //         if ("token".equals(cookie.getName())) {
        //             return cookie.getValue();
        //         }
        //     }
        // }

        return null;
    }

    /**
     * 移除Token前缀（如Bearer）
     *
     * @param token 原始token字符串
     * @return 处理后的token
     */
    public static String replaceTokenPrefix(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }

        // 移除Bearer前缀
        if (token.startsWith(TokenConstants.TOKEN_TYPE_BEARER + " ")) {
            return token.substring(TokenConstants.TOKEN_TYPE_BEARER.length() + 1);
        }

        // 移除其他可能的前缀
        if (token.startsWith("Bearer ")) {
            return token.substring(7);
        }

        return token;
    }

    /**
     * 判断当前用户是否为管理员
     *
     * @return 是否为管理员
     */
    public static boolean isAdmin() {
        Long userId = getUserId();
        return isAdmin(userId);
    }

    /**
     * 判断指定用户是否为管理员
     * 可以根据用户ID、角色等进行判断
     *
     * @param userId 用户ID
     * @return 是否为管理员
     */
    public static boolean isAdmin(Long userId) {
        if (userId == null) {
            return false;
        }

        // TODO: 这里可以根据实际业务逻辑实现
        // 可以查询用户的角色信息，判断是否包含管理员角色
        return userId.equals(1L);
    }

    /**
     * 密码加密
     * 使用BCrypt算法对密码进行加密
     *
     * @param password 原始密码
     * @return 加密后的密码
     */
    public static String encryptPassword(String password) {
        if (StringUtils.isBlank(password)) {
            throw new IllegalArgumentException("密码不能为空");
        }
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }

    /**
     * 密码匹配验证
     * 验证原始密码与加密密码是否匹配
     *
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        if (StringUtils.isBlank(rawPassword) || StringUtils.isBlank(encodedPassword)) {
            return false;
        }
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 验证JWT Token是否有效
     *
     * @param token JWT Token
     * @return 是否有效
     */
    public static boolean isValidToken(String token) {
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
     * 验证当前请求的Token是否有效
     *
     * @return 是否有效
     */
    public static boolean isValidCurrentToken() {
        String token = getToken();
        return isValidToken(token);
    }

    /**
     * 获取Token的剩余有效时间（秒）
     *
     * @param token JWT Token
     * @return 剩余时间（秒），如果Token无效则返回null
     */
    public static Long getTokenRemainingTime(String token) {
        try {
            if (StringUtils.isBlank(token)) {
                return null;
            }
            return jwtUtils.getRemainingTime(token);
        } catch (Exception e) {
            log.debug("获取Token剩余时间失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取当前Token的剩余有效时间（秒）
     *
     * @return 剩余时间（秒），如果Token无效则返回null
     */
    public static Long getCurrentTokenRemainingTime() {
        String token = getToken();
        return getTokenRemainingTime(token);
    }

    /**
     * 检查Token是否即将过期
     *
     * @param token JWT Token
     * @param minutes 提前多少分钟算作即将过期
     * @return 是否即将过期
     */
    public static boolean isTokenExpiringSoon(String token, int minutes) {
        try {
            if (StringUtils.isBlank(token)) {
                return true;
            }
            return jwtUtils.isTokenExpiringSoon(token, minutes);
        } catch (Exception e) {
            log.debug("检查Token过期状态失败: {}", e.getMessage());
            return true;
        }
    }

    /**
     * 检查当前Token是否即将过期
     *
     * @param minutes 提前多少分钟算作即将过期
     * @return 是否即将过期
     */
    public static boolean isCurrentTokenExpiringSoon(int minutes) {
        String token = getToken();
        return isTokenExpiringSoon(token, minutes);
    }

    /**
     * 获取客户端IP地址
     *
     * @return 客户端IP地址
     */
    public static String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return "unknown";
            }
            HttpServletRequest request = attributes.getRequest();
            return getClientIp(request);
        } catch (Exception e) {
            log.debug("获取客户端IP失败: {}", e.getMessage());
            return "unknown";
        }
    }

    /**
     * 从HttpServletRequest中获取客户端IP地址
     *
     * @param request HTTP请求对象
     * @return 客户端IP地址
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String ip = null;

        // 1. 从X-Forwarded-For头获取（代理服务器会设置）
        ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
            // 多级代理的情况，第一个IP为客户端真实IP
            int index = ip.indexOf(',');
            if (index != -1) {
                ip = ip.substring(0, index);
            }
            return ip.trim();
        }

        // 2. 从Proxy-Client-IP头获取
        ip = request.getHeader("Proxy-Client-IP");
        if (StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }

        // 3. 从WL-Proxy-Client-IP头获取
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }

        // 4. 从HTTP_CLIENT_IP头获取
        ip = request.getHeader("HTTP_CLIENT_IP");
        if (StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }

        // 5. 从HTTP_X_FORWARDED_FOR头获取
        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }

        // 6. 从X-Real-IP头获取（Nginx代理常用）
        ip = request.getHeader("X-Real-IP");
        if (StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }

        // 7. 最后从getRemoteAddr获取
        ip = request.getRemoteAddr();
        return StringUtils.isNotBlank(ip) ? ip.trim() : "unknown";
    }

    /**
     * 获取用户代理信息
     *
     * @return User-Agent字符串
     */
    public static String getUserAgent() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return null;
            }
            HttpServletRequest request = attributes.getRequest();
            return request.getHeader("User-Agent");
        } catch (Exception e) {
            log.debug("获取User-Agent失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取请求的完整URL
     *
     * @return 完整的请求URL
     */
    public static String getRequestUrl() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return null;
            }
            HttpServletRequest request = attributes.getRequest();
            return request.getRequestURL().toString();
        } catch (Exception e) {
            log.debug("获取请求URL失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 判断是否为Ajax请求
     *
     * @return 是否为Ajax请求
     */
    public static boolean isAjaxRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return false;
            }
            HttpServletRequest request = attributes.getRequest();
            return isAjaxRequest(request);
        } catch (Exception e) {
            log.debug("判断Ajax请求失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 判断HttpServletRequest是否为Ajax请求
     *
     * @param request HTTP请求对象
     * @return 是否为Ajax请求
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }

        String requestedWith = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(requestedWith);
    }

    /**
     * 清除Spring Security上下文
     */
    public static void clearSecurityContext() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    /**
     * 获取当前认证对象
     *
     * @return Authentication对象
     */
    public static Authentication getAuthentication() {
        return org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 判断当前用户是否已认证
     *
     * @return 是否已认证
     */
    public static boolean isAuthenticated() {
        // 优先检查自定义上下文
        if (SecurityContextHolder.isLogin()) {
            return true;
        }

        // 检查Spring Security上下文
        Authentication authentication = getAuthentication();
        return authentication != null &&
                authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getName());
    }

    /**
     * 获取当前登录用户的完整信息
     *
     * @return 登录用户信息，如果未登录则返回null
     */
    public static LoginUserBody getLoginUser() {
        return SecurityContextHolder.getLoginUser();
    }

    /**
     * 设置当前登录用户信息到上下文
     *
     * @param loginUser 登录用户信息
     */
    public static void setLoginUser(LoginUserBody loginUser) {
        SecurityContextHolder.setLoginUser(loginUser);
    }

    /**
     * 验证当前用户是否拥有指定权限
     *
     * @param permission 权限标识
     * @return 是否拥有权限
     */
    public static boolean hasPermission(String permission) {
        return SecurityContextHolder.hasPermission(permission);
    }

    /**
     * 验证当前用户是否拥有指定角色
     *
     * @param role 角色名称
     * @return 是否拥有角色
     */
    public static boolean hasRole(String role) {
        return SecurityContextHolder.hasRole(role);
    }

    /**
     * 验证当前用户是否拥有任意一个指定权限
     *
     * @param permissions 权限列表
     * @return 是否拥有任意一个权限
     */
    public static boolean hasAnyPermission(String... permissions) {
        LoginUserBody loginUser = getLoginUser();
        return loginUser != null && loginUser.hasAnyPermission(permissions);
    }

    /**
     * 验证当前用户是否拥有任意一个指定角色
     *
     * @param roles 角色列表
     * @return 是否拥有任意一个角色
     */
    public static boolean hasAnyRole(String... roles) {
        LoginUserBody loginUser = getLoginUser();
        return loginUser != null && loginUser.hasAnyRole(roles);
    }

    /**
     * 验证当前用户是否拥有所有指定权限
     *
     * @param permissions 权限列表
     * @return 是否拥有所有权限
     */
    public static boolean hasAllPermissions(String... permissions) {
        LoginUserBody loginUser = getLoginUser();
        if (loginUser == null || loginUser.getPermissions() == null) {
            return false;
        }

        for (String permission : permissions) {
            if (!loginUser.hasPermission(permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 验证当前用户是否拥有所有指定角色
     *
     * @param roles 角色列表
     * @return 是否拥有所有角色
     */
    public static boolean hasAllRoles(String... roles) {
        LoginUserBody loginUser = getLoginUser();
        if (loginUser == null || loginUser.getRoles() == null) {
            return false;
        }

        for (String role : roles) {
            if (!loginUser.hasRole(role)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 清除所有安全上下文（包括Spring Security和自定义上下文）
     */
    public static void clearAllContext() {
        clearSecurityContext();
        SecurityContextHolder.clear();
    }
}