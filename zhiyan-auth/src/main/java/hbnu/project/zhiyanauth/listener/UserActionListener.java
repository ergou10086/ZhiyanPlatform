package hbnu.project.zhiyanauth.listener;

import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import hbnu.project.zhiyanauth.service.UserService;
import hbnu.project.zhiyancommonbasic.constants.CacheConstants;
import hbnu.project.zhiyancommonbasic.constants.GeneralConstants;
import hbnu.project.zhiyancommonbasic.utils.ServletUtils;
import hbnu.project.zhiyancommonbasic.utils.SpringUtils;
import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import hbnu.project.zhiyancommonbasic.utils.ip.AddressUtils;
import hbnu.project.zhiyancommonbasic.utils.ip.IpUtils;
import hbnu.project.zhiyancommonredis.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户行为监听器
 * 监听用户登录、注销等行为，记录在线状态和登录日志
 *
 * @author ErgouTree
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class UserActionListener implements ApplicationListener<UserActionListener.UserActionEvent> {

    private final RedisService redisService;
    private final UserService userService;

    @Override
    public void onApplicationEvent(UserActionEvent event) {
        switch (event.getActionType()) {
            case LOGIN:
                handleLogin(event);
                break;
            case LOGOUT:
                handleLogout(event);
                break;
            case TOKEN_REFRESH:
                handleTokenRefresh(event);
                break;
            case KICKOUT:
                handleKickout(event);
                break;
            default:
                log.warn("未知的用户行为类型: {}", event.getActionType());
        }
    }

    /**
     * 处理用户登录事件
     */
    private void handleLogin(UserActionEvent event) {
        try {
            HttpServletRequest request = ServletUtils.getRequest();
            if (request == null) {
                log.warn("无法获取HttpServletRequest，跳过登录记录");
                return;
            }

            // 解析用户代理信息
            UserAgent userAgent = UserAgentUtil.parse(request.getHeader("User-Agent"));
            String ip = IpUtils.getIpAddr(request);
            String location = AddressUtils.getRealAddressByIP(ip);

            // 构建在线用户信息
            Map<String, Object> userOnline = new HashMap<>();
            userOnline.put("userId", event.getUserId());
            userOnline.put("username", event.getUsername());
            userOnline.put("email", event.getEmail());
            userOnline.put("tokenId", event.getTokenValue());
            userOnline.put("ipAddr", ip);
            userOnline.put("loginLocation", location);
            userOnline.put("browser", userAgent.getBrowser().getName());
            userOnline.put("os", userAgent.getOs().getName());
            userOnline.put("loginTime", System.currentTimeMillis());
            userOnline.put("isRememberMe", event.isRememberMe());

            // 存储在线用户信息到Redis
            String onlineKey = CacheConstants.USER_SESSION_PREFIX + event.getTokenValue();
            long expireSeconds = event.getTokenExpireTime();
            if (expireSeconds > 0) {
                redisService.setCacheObject(onlineKey, userOnline, expireSeconds, TimeUnit.SECONDS);
            } else {
                redisService.setCacheObject(onlineKey, userOnline);
            }

            // 发布登录日志事件
            LoginLogEvent loginLogEvent = new LoginLogEvent();
            loginLogEvent.setUserId(event.getUserId());
            loginLogEvent.setUsername(event.getUsername());
            loginLogEvent.setEmail(event.getEmail());
            loginLogEvent.setIpAddr(ip);
            loginLogEvent.setLocation(location);
            loginLogEvent.setBrowser(userAgent.getBrowser().getName());
            loginLogEvent.setOs(userAgent.getOs().getName());
            loginLogEvent.setStatus(GeneralConstants.LOGIN_SUCCESS_STATUS);
            loginLogEvent.setMessage("登录成功");
            loginLogEvent.setLoginTime(LocalDateTime.now());

            SpringUtils.context().publishEvent(loginLogEvent);

            // 更新用户最后登录信息
            // TODO：userService.updateLastLoginInfo(event.getUserId(), ip);

            log.info("用户登录成功 - 用户ID: {}, Token: {}, IP: {}",
                    event.getUserId(), event.getTokenValue(), ip);

        } catch (Exception e) {
            log.error("处理用户登录事件失败 - 用户ID: {}, 错误: {}",
                    event.getUserId(), e.getMessage(), e);
        }
    }

    /**
     * 处理用户注销事件
     */
    private void handleLogout(UserActionEvent event) {
        try {
            // 删除在线用户信息
            String onlineKey = CacheConstants.USER_SESSION_PREFIX + event.getTokenValue();
            redisService.deleteObject(onlineKey);

            // 发布注销日志事件
            LoginLogEvent logoutEvent = new LoginLogEvent();
            logoutEvent.setUserId(event.getUserId());
            logoutEvent.setUsername(event.getUsername());
            logoutEvent.setEmail(event.getEmail());
            logoutEvent.setStatus(GeneralConstants.LOGIN_SUCCESS_STATUS);
            logoutEvent.setMessage("注销成功");
            logoutEvent.setLoginTime(LocalDateTime.now());

            if (ServletUtils.getRequest() != null) {
                String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
                logoutEvent.setIpAddr(ip);
                logoutEvent.setLocation(AddressUtils.getRealAddressByIP(ip));
            }

            SpringUtils.context().publishEvent(logoutEvent);

            log.info("用户注销成功 - 用户ID: {}, Token: {}",
                    event.getUserId(), event.getTokenValue());

        } catch (Exception e) {
            log.error("处理用户注销事件失败 - 用户ID: {}, 错误: {}",
                    event.getUserId(), e.getMessage(), e);
        }
    }

    /**
     * 处理Token刷新事件
     */
    private void handleTokenRefresh(UserActionEvent event) {
        try {
            // 删除旧的在线信息
            if (StringUtils.isNotBlank(event.getOldTokenValue())) {
                String oldOnlineKey = CacheConstants.USER_SESSION_PREFIX + event.getOldTokenValue();
                redisService.deleteObject(oldOnlineKey);
            }

            // 重新记录登录（使用新Token）
            handleLogin(event);

            log.info("Token刷新成功 - 用户ID: {}, 新Token: {}",
                    event.getUserId(), event.getTokenValue());

        } catch (Exception e) {
            log.error("处理Token刷新事件失败 - 用户ID: {}, 错误: {}",
                    event.getUserId(), e.getMessage(), e);
        }
    }

    /**
     * 处理用户被踢出事件
     */
    private void handleKickout(UserActionEvent event) {
        try {
            // 删除在线用户信息
            String onlineKey = CacheConstants.USER_SESSION_PREFIX + event.getTokenValue();
            redisService.deleteObject(onlineKey);

            // 发布踢出日志事件
            LoginLogEvent kickoutEvent = new LoginLogEvent();
            kickoutEvent.setUserId(event.getUserId());
            kickoutEvent.setUsername(event.getUsername());
            kickoutEvent.setEmail(event.getEmail());
            kickoutEvent.setStatus(GeneralConstants.LOGIN_FAIL_STATUS);
            kickoutEvent.setMessage("用户被踢出");
            kickoutEvent.setLoginTime(LocalDateTime.now());

            SpringUtils.context().publishEvent(kickoutEvent);

            log.info("用户被踢出 - 用户ID: {}, Token: {}",
                    event.getUserId(), event.getTokenValue());

        } catch (Exception e) {
            log.error("处理用户踢出事件失败 - 用户ID: {}, 错误: {}",
                    event.getUserId(), e.getMessage(), e);
        }
    }

    /**
     * 用户行为事件类型枚举
     */
    public enum UserActionType {
        LOGIN,      // 登录
        LOGOUT,     // 注销
        TOKEN_REFRESH, // Token刷新
        KICKOUT     // 被踢出
    }

    /**
     * 用户行为事件
     */
    public static class UserActionEvent extends ApplicationEvent {
        private final UserActionType actionType;
        private final Long userId;
        private final String username;
        private final String email;
        private final String tokenValue;
        private final String oldTokenValue;
        private final long tokenExpireTime;
        private final boolean rememberMe;

        public UserActionEvent(Object source, UserActionType actionType,
                               Long userId, String username, String email,
                               String tokenValue, long tokenExpireTime, boolean rememberMe) {
            super(source);
            this.actionType = actionType;
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.tokenValue = tokenValue;
            this.oldTokenValue = null;
            this.tokenExpireTime = tokenExpireTime;
            this.rememberMe = rememberMe;
        }

        public UserActionEvent(Object source, UserActionType actionType,
                               Long userId, String username, String email,
                               String tokenValue, String oldTokenValue,
                               long tokenExpireTime, boolean rememberMe) {
            super(source);
            this.actionType = actionType;
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.tokenValue = tokenValue;
            this.oldTokenValue = oldTokenValue;
            this.tokenExpireTime = tokenExpireTime;
            this.rememberMe = rememberMe;
        }

        // Getters
        public UserActionType getActionType() { return actionType; }
        public Long getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getTokenValue() { return tokenValue; }
        public String getOldTokenValue() { return oldTokenValue; }
        public long getTokenExpireTime() { return tokenExpireTime; }
        public boolean isRememberMe() { return rememberMe; }
    }

    /**
     * 登录日志事件
     */
    public static class LoginLogEvent extends ApplicationEvent {
        private Long userId;
        private String username;
        private String email;
        private String ipAddr;
        private String location;
        private String browser;
        private String os;
        private String status;
        private String message;
        private LocalDateTime loginTime;

        public LoginLogEvent() {
            super(new Object());
        }

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getIpAddr() { return ipAddr; }
        public void setIpAddr(String ipAddr) { this.ipAddr = ipAddr; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getBrowser() { return browser; }
        public void setBrowser(String browser) { this.browser = browser; }
        public String getOs() { return os; }
        public void setOs(String os) { this.os = os; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public LocalDateTime getLoginTime() { return loginTime; }
        public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }
    }
}