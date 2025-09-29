package hbnu.project.zhiyancommonsecurity.interceptor;

import hbnu.project.zhiyancommonbasic.constants.SecurityConstants;
import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import hbnu.project.zhiyancommonsecurity.context.SecurityContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 请求头拦截器
 * 用于从请求头中提取用户信息并设置到安全上下文中
 * 主要用于微服务间调用时传递用户上下文信息
 *
 * @author ErgouTree
 */
@Slf4j
public class HeaderInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            // 从请求头中提取用户信息
            String userId = request.getHeader(SecurityConstants.DETAILS_USER_ID);
            String userName = request.getHeader(SecurityConstants.DETAILS_USERNAME);
            String userEmail = request.getHeader(SecurityConstants.DETAILS_USER_EMAIL);
            String userKey = request.getHeader(SecurityConstants.USER_KEY);
            String permissions = request.getHeader(SecurityConstants.LOGIN_USER_PERMISSIONS);
            String roles = request.getHeader(SecurityConstants.LOGIN_USER_ROLES);
            String loginIp = request.getHeader(SecurityConstants.LOGIN_IP);
            String userAgent = request.getHeader(SecurityConstants.USER_AGENT);

            // 设置到安全上下文中
            if (StringUtils.isNotBlank(userId)) {
                SecurityContextHolder.setUserId(userId);
                log.debug("从请求头设置用户上下文 - 用户ID: {}, 用户名: {}", userId, userName);
            }

            if (StringUtils.isNotBlank(userName)) {
                SecurityContextHolder.setUsername(userName);
            }

            if (StringUtils.isNotBlank(userEmail)) {
                SecurityContextHolder.setUserEmail(userEmail);
            }

            if (StringUtils.isNotBlank(userKey)) {
                SecurityContextHolder.setUserKey(userKey);
            }

            if (StringUtils.isNotBlank(permissions)) {
                SecurityContextHolder.setPermissions(permissions);
            }

            if (StringUtils.isNotBlank(roles)) {
                SecurityContextHolder.setRoles(roles);
            }

            if (StringUtils.isNotBlank(loginIp)) {
                SecurityContextHolder.setLoginIp(loginIp);
            }

            if (StringUtils.isNotBlank(userAgent)) {
                SecurityContextHolder.setUserAgent(userAgent);
            }

            // 设置其他可能的请求头信息
            String realName = request.getHeader(SecurityConstants.DETAILS_USER_NAME);
            if (StringUtils.isNotBlank(realName)) {
                SecurityContextHolder.setUserName(realName);
            }

            String avatar = request.getHeader(SecurityConstants.DETAILS_USER_AVATAR);
            if (StringUtils.isNotBlank(avatar)) {
                SecurityContextHolder.set(SecurityConstants.DETAILS_USER_AVATAR, avatar);
            }

            String title = request.getHeader(SecurityConstants.DETAILS_USER_TITLE);
            if (StringUtils.isNotBlank(title)) {
                SecurityContextHolder.set(SecurityConstants.DETAILS_USER_TITLE, title);
            }

            String institution = request.getHeader(SecurityConstants.DETAILS_USER_INSTITUTION);
            if (StringUtils.isNotBlank(institution)) {
                SecurityContextHolder.set(SecurityConstants.DETAILS_USER_INSTITUTION, institution);
            }

            return true;
        } catch (Exception e) {
            log.error("设置请求头上下文信息失败", e);
            return true; // 即使失败也继续处理请求
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        try {
            // 请求处理完成后清理上下文，防止内存泄漏
            SecurityContextHolder.clear();
            log.debug("清理请求上下文完成");
        } catch (Exception e) {
            log.error("清理请求上下文失败", e);
        }
    }
}
