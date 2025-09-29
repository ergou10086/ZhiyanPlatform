package hbnu.project.zhiyancommonsecurity.utils;

import hbnu.project.zhiyancommon.constants.SecurityConstants;
import hbnu.project.zhiyancommon.utils.StringUtils;
import hbnu.project.zhiyansecurity.context.LoginUserBody;
import hbnu.project.zhiyansecurity.context.SecurityContextHolder;
import org.springframework.http.HttpHeaders;

import java.util.HashMap;
import java.util.Map;

/**
 * 安全请求头工具类
 * 用于在微服务间调用时传递用户上下文信息
 *
 * @author ErgouTree
 */
public class SecurityHeaderUtils {

    /**
     * 获取当前用户的安全请求头
     * 用于微服务间调用时传递用户上下文
     *
     * @return 包含用户信息的请求头Map
     */
    public static Map<String, String> getSecurityHeaders() {
        Map<String, String> headers = new HashMap<>();

        try {
            // 获取基本用户信息
            Long userId = SecurityContextHolder.getUserId();
            if (userId != null && userId > 0) {
                headers.put(SecurityConstants.DETAILS_USER_ID, userId.toString());
            }

            String username = SecurityContextHolder.getUsername();
            if (StringUtils.isNotBlank(username)) {
                headers.put(SecurityConstants.DETAILS_USERNAME, username);
            }

            String userName = SecurityContextHolder.getUserName();
            if (StringUtils.isNotBlank(userName)) {
                headers.put(SecurityConstants.DETAILS_USER_NAME, userName);
            }

            String userEmail = SecurityContextHolder.getUserEmail();
            if (StringUtils.isNotBlank(userEmail)) {
                headers.put(SecurityConstants.DETAILS_USER_EMAIL, userEmail);
            }

            String userKey = SecurityContextHolder.getUserKey();
            if (StringUtils.isNotBlank(userKey)) {
                headers.put(SecurityConstants.USER_KEY, userKey);
            }

            // 获取权限和角色信息
            String permissions = SecurityContextHolder.getPermissions();
            if (StringUtils.isNotBlank(permissions)) {
                headers.put(SecurityConstants.LOGIN_USER_PERMISSIONS, permissions);
            }

            String roles = SecurityContextHolder.getRoles();
            if (StringUtils.isNotBlank(roles)) {
                headers.put(SecurityConstants.LOGIN_USER_ROLES, roles);
            }

            // 获取登录相关信息
            String loginIp = SecurityContextHolder.getLoginIp();
            if (StringUtils.isNotBlank(loginIp)) {
                headers.put(SecurityConstants.LOGIN_IP, loginIp);
            }

            String userAgent = SecurityContextHolder.getUserAgent();
            if (StringUtils.isNotBlank(userAgent)) {
                headers.put(SecurityConstants.USER_AGENT, userAgent);
            }

            // 获取其他用户属性
            String avatar = SecurityContextHolder.get(SecurityConstants.DETAILS_USER_AVATAR);
            if (StringUtils.isNotBlank(avatar)) {
                headers.put(SecurityConstants.DETAILS_USER_AVATAR, avatar);
            }

            String title = SecurityContextHolder.get(SecurityConstants.DETAILS_USER_TITLE);
            if (StringUtils.isNotBlank(title)) {
                headers.put(SecurityConstants.DETAILS_USER_TITLE, title);
            }

            String institution = SecurityContextHolder.get(SecurityConstants.DETAILS_USER_INSTITUTION);
            if (StringUtils.isNotBlank(institution)) {
                headers.put(SecurityConstants.DETAILS_USER_INSTITUTION, institution);
            }

            // 标记为内部请求
            headers.put(SecurityConstants.FROM_SOURCE, SecurityConstants.INNER);

        } catch (Exception e) {
            // 忽略异常，返回空的headers
        }

        return headers;
    }

    /**
     * 获取HttpHeaders对象
     * 适用于Spring的RestTemplate或WebClient
     *
     * @return HttpHeaders对象
     */
    public static HttpHeaders getHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        Map<String, String> securityHeaders = getSecurityHeaders();
        
        for (Map.Entry<String, String> entry : securityHeaders.entrySet()) {
            httpHeaders.set(entry.getKey(), entry.getValue());
        }
        
        return httpHeaders;
    }

    /**
     * 从LoginUser对象创建安全请求头
     *
     * @param loginUserBody 登录用户信息
     * @return 包含用户信息的请求头Map
     */
    public static Map<String, String> createSecurityHeaders(LoginUserBody loginUserBody) {
        Map<String, String> headers = new HashMap<>();

        if (loginUserBody == null) {
            return headers;
        }

        try {
            if (loginUserBody.getUserId() != null) {
                headers.put(SecurityConstants.DETAILS_USER_ID, loginUserBody.getUserId().toString());
            }

            if (StringUtils.isNotBlank(loginUserBody.getEmail())) {
                headers.put(SecurityConstants.DETAILS_USERNAME, loginUserBody.getEmail());
                headers.put(SecurityConstants.DETAILS_USER_EMAIL, loginUserBody.getEmail());
            }

            if (StringUtils.isNotBlank(loginUserBody.getName())) {
                headers.put(SecurityConstants.DETAILS_USER_NAME, loginUserBody.getName());
            }

            if (StringUtils.isNotBlank(loginUserBody.getAvatarUrl())) {
                headers.put(SecurityConstants.DETAILS_USER_AVATAR, loginUserBody.getAvatarUrl());
            }

            if (StringUtils.isNotBlank(loginUserBody.getTitle())) {
                headers.put(SecurityConstants.DETAILS_USER_TITLE, loginUserBody.getTitle());
            }

            if (StringUtils.isNotBlank(loginUserBody.getInstitution())) {
                headers.put(SecurityConstants.DETAILS_USER_INSTITUTION, loginUserBody.getInstitution());
            }

            if (loginUserBody.getRoles() != null && !loginUserBody.getRoles().isEmpty()) {
                headers.put(SecurityConstants.LOGIN_USER_ROLES, String.join(",", loginUserBody.getRoles()));
            }

            if (loginUserBody.getPermissions() != null && !loginUserBody.getPermissions().isEmpty()) {
                headers.put(SecurityConstants.LOGIN_USER_PERMISSIONS, String.join(",", loginUserBody.getPermissions()));
            }

            if (StringUtils.isNotBlank(loginUserBody.getLoginIp())) {
                headers.put(SecurityConstants.LOGIN_IP, loginUserBody.getLoginIp());
            }

            if (StringUtils.isNotBlank(loginUserBody.getBrowser())) {
                headers.put(SecurityConstants.USER_AGENT, loginUserBody.getBrowser());
            }

            // 标记为内部请求
            headers.put(SecurityConstants.FROM_SOURCE, SecurityConstants.INNER);

        } catch (Exception e) {
            // 忽略异常，返回已有的headers
        }

        return headers;
    }

    /**
     * 从LoginUser对象创建HttpHeaders
     *
     * @param loginUserBody 登录用户信息
     * @return HttpHeaders对象
     */
    public static HttpHeaders createHttpHeaders(LoginUserBody loginUserBody) {
        HttpHeaders httpHeaders = new HttpHeaders();
        Map<String, String> securityHeaders = createSecurityHeaders(loginUserBody);
        
        for (Map.Entry<String, String> entry : securityHeaders.entrySet()) {
            httpHeaders.set(entry.getKey(), entry.getValue());
        }
        
        return httpHeaders;
    }

    /**
     * 检查请求是否来自内部服务
     *
     * @param headers 请求头Map
     * @return 是否为内部请求
     */
    public static boolean isInternalRequest(Map<String, String> headers) {
        if (headers == null) {
            return false;
        }
        return SecurityConstants.INNER.equals(headers.get(SecurityConstants.FROM_SOURCE));
    }

    /**
     * 检查HttpHeaders是否为内部请求
     *
     * @param headers HttpHeaders对象
     * @return 是否为内部请求
     */
    public static boolean isInternalRequest(HttpHeaders headers) {
        if (headers == null) {
            return false;
        }
        String fromSource = headers.getFirst(SecurityConstants.FROM_SOURCE);
        return SecurityConstants.INNER.equals(fromSource);
    }
}
