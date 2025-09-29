package hbnu.project.zhiyancommonsecurity.context;

import hbnu.project.zhiyancommonbasic.constants.SecurityConstants;
import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import hbnu.project.zhiyancommonbasic.utils.text.ConvertUtils;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 安全上下文持有者
 * 获取当前线程变量中的用户id、用户名称、Token等信息
 * 注意：必须在网关通过请求头的方法传入，同时在HeaderInterceptor拦截器设置值。否则这里无法获取
 *
 * @author ErgouTree
 */
@Slf4j
public class SecurityContextHolder {

    /**
     * 使用InheritableThreadLocal支持父子线程间的数据传递
     * 也可以考虑使用阿里的TransmittableThreadLocal来支持线程池场景
     */
    private static final ThreadLocal<Map<String, Object>> THREAD_LOCAL = new InheritableThreadLocal<>();

    /**
     * 兼容原有的LoginUser存储方式
     */
    private static final ThreadLocal<LoginUserBody> LOGIN_USER_HOLDER = new ThreadLocal<>();

    /**
     * 设置键值对到线程上下文
     *
     * @param key   键
     * @param value 值
     */
    public static void set(String key, Object value) {
        Map<String, Object> map = getLocalMap();
        map.put(key, value == null ? StringUtils.EMPTY : value);
    }

    /**
     * 从线程上下文获取值
     *
     * @param key 键
     * @return 值
     */
    public static String get(String key) {
        Map<String, Object> map = getLocalMap();
        return ConvertUtils.toStr(map.getOrDefault(key, StringUtils.EMPTY));
    }

    /**
     * 从线程上下文获取指定类型的值
     *
     * @param key   键
     * @param clazz 目标类型
     * @param <T>   泛型类型
     * @return 转换后的值
     */
    public static <T> T get(String key, Class<T> clazz) {
        Map<String, Object> map = getLocalMap();
        return StringUtils.cast(map.getOrDefault(key, null));
    }

    /**
     * 获取当前线程的本地Map
     *
     * @return 线程本地Map
     */
    public static Map<String, Object> getLocalMap() {
        Map<String, Object> map = THREAD_LOCAL.get();
        if (map == null) {
            map = new ConcurrentHashMap<>();
            THREAD_LOCAL.set(map);
        }
        return map;
    }

    /**
     * 设置整个线程本地Map
     *
     * @param threadLocalMap 线程本地Map
     */
    public static void setLocalMap(Map<String, Object> threadLocalMap) {
        THREAD_LOCAL.set(threadLocalMap);
    }

    /**
     * 设置当前登录用户信息（兼容原有方式）
     *
     * @param loginUserBody 登录用户信息
     */
    public static void setLoginUser(LoginUserBody loginUserBody) {
        LOGIN_USER_HOLDER.set(loginUserBody);
        if (loginUserBody != null) {
            // 同时设置到通用上下文中
            setUserId(loginUserBody.getUserId().toString());
            setUserName(loginUserBody.getName());
            setUserEmail(loginUserBody.getEmail());
            set(SecurityConstants.DETAILS_USER_AVATAR, loginUserBody.getAvatarUrl());
            set(SecurityConstants.DETAILS_USER_TITLE, loginUserBody.getTitle());
            set(SecurityConstants.DETAILS_USER_INSTITUTION, loginUserBody.getInstitution());
            set(SecurityConstants.IS_LOCKED, loginUserBody.getIsLocked());
            set(SecurityConstants.LOGIN_TIME, loginUserBody.getLoginTime());
            set(SecurityConstants.LOGIN_IP, loginUserBody.getLoginIp());
            set(SecurityConstants.USER_AGENT, loginUserBody.getBrowser());
            set(SecurityConstants.TOKEN_EXPIRE_TIME, loginUserBody.getExpireTime());
            
            // 设置角色和权限
            if (loginUserBody.getRoles() != null) {
                set(SecurityConstants.LOGIN_USER_ROLES, String.join(",", loginUserBody.getRoles()));
            }
            if (loginUserBody.getPermissions() != null) {
                set(SecurityConstants.LOGIN_USER_PERMISSIONS, String.join(",", loginUserBody.getPermissions()));
            }
            
            log.debug("设置用户上下文 - 用户ID: {}, 用户名: {}", loginUserBody.getUserId(), loginUserBody.getName());
        }
    }

    /**
     * 获取当前登录用户信息（兼容原有方式）
     *
     * @return 登录用户信息，如果未登录则返回null
     */
    public static LoginUserBody getLoginUser() {
        LoginUserBody loginUserBody = LOGIN_USER_HOLDER.get();
        if (loginUserBody != null) {
            return loginUserBody;
        }
        
        // 如果没有完整的LoginUser对象，尝试从上下文构建
        Long userId = getUserId();
        if (userId != null && userId > 0) {
            return LoginUserBody.builder()
                    .userId(userId)
                    .name(get(SecurityConstants.DETAILS_USER_NAME))
                    .email(get(SecurityConstants.DETAILS_USER_EMAIL))
                    .avatarUrl(get(SecurityConstants.DETAILS_USER_AVATAR))
                    .title(get(SecurityConstants.DETAILS_USER_TITLE))
                    .institution(get(SecurityConstants.DETAILS_USER_INSTITUTION))
                    .isLocked(ConvertUtils.toBool(get(SecurityConstants.IS_LOCKED)))
                    .loginTime(get(SecurityConstants.LOGIN_TIME, LocalDateTime.class))
                    .loginIp(get(SecurityConstants.LOGIN_IP))
                    .browser(get(SecurityConstants.USER_AGENT))
                    .expireTime(get(SecurityConstants.TOKEN_EXPIRE_TIME, LocalDateTime.class))
                    .build();
        }
        
        return null;
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID，如果未登录则返回0L
     */
    public static Long getUserId() {
        return ConvertUtils.toLong(get(SecurityConstants.DETAILS_USER_ID), 0L);
    }

    /**
     * 设置用户ID
     *
     * @param userId 用户ID
     */
    public static void setUserId(String userId) {
        set(SecurityConstants.DETAILS_USER_ID, userId);
    }

    /**
     * 设置用户ID
     *
     * @param userId 用户ID
     */
    public static void setUserId(Long userId) {
        set(SecurityConstants.DETAILS_USER_ID, userId != null ? userId.toString() : null);
    }

    /**
     * 获取当前登录用户名（登录账号）
     *
     * @return 用户名，如果未登录则返回空字符串
     */
    public static String getUsername() {
        return get(SecurityConstants.DETAILS_USERNAME);
    }

    /**
     * 设置用户名（登录账号）
     *
     * @param username 用户名
     */
    public static void setUsername(String username) {
        set(SecurityConstants.DETAILS_USERNAME, username);
    }

    /**
     * 获取当前用户姓名
     *
     * @return 用户姓名
     */
    public static String getUserName() {
        return get(SecurityConstants.DETAILS_USER_NAME);
    }

    /**
     * 设置用户姓名
     *
     * @param userName 用户姓名
     */
    public static void setUserName(String userName) {
        set(SecurityConstants.DETAILS_USER_NAME, userName);
    }

    /**
     * 获取当前登录用户邮箱
     *
     * @return 用户邮箱
     */
    public static String getUserEmail() {
        return get(SecurityConstants.DETAILS_USER_EMAIL);
    }

    /**
     * 设置用户邮箱
     *
     * @param email 用户邮箱
     */
    public static void setUserEmail(String email) {
        set(SecurityConstants.DETAILS_USER_EMAIL, email);
    }

    /**
     * 获取用户唯一标识
     *
     * @return 用户唯一标识
     */
    public static String getUserKey() {
        return get(SecurityConstants.USER_KEY);
    }

    /**
     * 设置用户唯一标识
     *
     * @param userKey 用户唯一标识
     */
    public static void setUserKey(String userKey) {
        set(SecurityConstants.USER_KEY, userKey);
    }

    /**
     * 获取用户权限
     *
     * @return 权限字符串
     */
    public static String getPermissions() {
        return get(SecurityConstants.LOGIN_USER_PERMISSIONS);
    }

    /**
     * 设置用户权限
     *
     * @param permissions 权限字符串
     */
    public static void setPermissions(String permissions) {
        set(SecurityConstants.LOGIN_USER_PERMISSIONS, permissions);
    }

    /**
     * 获取用户角色
     *
     * @return 角色字符串
     */
    public static String getRoles() {
        return get(SecurityConstants.LOGIN_USER_ROLES);
    }

    /**
     * 设置用户角色
     *
     * @param roles 角色字符串
     */
    public static void setRoles(String roles) {
        set(SecurityConstants.LOGIN_USER_ROLES, roles);
    }

    /**
     * 获取登录时间
     *
     * @return 登录时间
     */
    public static LocalDateTime getLoginTime() {
        return get(SecurityConstants.LOGIN_TIME, LocalDateTime.class);
    }

    /**
     * 设置登录时间
     *
     * @param loginTime 登录时间
     */
    public static void setLoginTime(LocalDateTime loginTime) {
        set(SecurityConstants.LOGIN_TIME, loginTime);
    }

    /**
     * 获取登录IP
     *
     * @return 登录IP
     */
    public static String getLoginIp() {
        return get(SecurityConstants.LOGIN_IP);
    }

    /**
     * 设置登录IP
     *
     * @param loginIp 登录IP
     */
    public static void setLoginIp(String loginIp) {
        set(SecurityConstants.LOGIN_IP, loginIp);
    }

    /**
     * 获取浏览器信息
     *
     * @return 浏览器信息
     */
    public static String getUserAgent() {
        return get(SecurityConstants.USER_AGENT);
    }

    /**
     * 设置浏览器信息
     *
     * @param userAgent 浏览器信息
     */
    public static void setUserAgent(String userAgent) {
        set(SecurityConstants.USER_AGENT, userAgent);
    }

    /**
     * 判断当前用户是否已登录
     *
     * @return 是否已登录
     */
    public static boolean isLogin() {
        Long userId = getUserId();
        return userId != null && userId > 0;
    }

    /**
     * 判断当前用户是否拥有指定权限
     *
     * @param permission 权限标识
     * @return 是否拥有权限
     */
    public static boolean hasPermission(String permission) {
        if (StringUtils.isEmpty(permission)) {
            return false;
        }
        
        // 优先从LoginUser对象获取
        LoginUserBody loginUserBody = LOGIN_USER_HOLDER.get();
        if (loginUserBody != null) {
            return loginUserBody.hasPermission(permission);
        }
        
        // 从上下文字符串中查找
        String permissions = getPermissions();
        if (StringUtils.isEmpty(permissions)) {
            return false;
        }
        
        return permissions.contains(permission);
    }

    /**
     * 判断当前用户是否拥有指定角色
     *
     * @param role 角色名称
     * @return 是否拥有角色
     */
    public static boolean hasRole(String role) {
        if (StringUtils.isEmpty(role)) {
            return false;
        }
        
        // 优先从LoginUser对象获取
        LoginUserBody loginUserBody = LOGIN_USER_HOLDER.get();
        if (loginUserBody != null) {
            return loginUserBody.hasRole(role);
        }
        
        // 从上下文字符串中查找
        String roles = getRoles();
        if (StringUtils.isEmpty(roles)) {
            return false;
        }
        
        return roles.contains(role);
    }

    /**
     * 判断当前用户是否为管理员
     *
     * @return 是否为管理员
     */
    public static boolean isAdmin() {
        return hasRole("系统管理员") || hasRole("超级管理员") || hasRole("ADMIN") || hasRole("管理员");
    }

    /**
     * 清除当前线程的用户上下文
     */
    public static void clear() {
        Long userId = getUserId();
        String userName = getUserName();
        if (userId != null && userId > 0) {
            log.debug("清除用户上下文 - 用户ID: {}, 用户名: {}", userId, userName);
        }
        THREAD_LOCAL.remove();
        LOGIN_USER_HOLDER.remove();
    }

    /**
     * 移除指定key的值
     *
     * @param key 键
     */
    public static void remove(String key) {
        Map<String, Object> map = getLocalMap();
        map.remove(key);
    }

    /**
     * 完全清除（兼容原有方法名）
     */
    public static void remove() {
        clear();
    }

    /**
     * 获取当前用户的浏览器信息（兼容原有方法）
     *
     * @return 浏览器信息
     */
    public static String getBrowser() {
        return getUserAgent();
    }

    /**
     * 判断是否包含指定的key
     *
     * @param key 键
     * @return 是否包含
     */
    public static boolean containsKey(String key) {
        return getLocalMap().containsKey(key);
    }

    /**
     * 获取所有的键
     *
     * @return 键集合
     */
    public static Set<String> getKeys() {
        return getLocalMap().keySet();
    }

    /**
     * 获取上下文大小
     *
     * @return 上下文中键值对的数量
     */
    public static int size() {
        return getLocalMap().size();
    }

    /**
     * 判断上下文是否为空
     *
     * @return 是否为空
     */
    public static boolean isEmpty() {
        return getLocalMap().isEmpty();
    }
}
