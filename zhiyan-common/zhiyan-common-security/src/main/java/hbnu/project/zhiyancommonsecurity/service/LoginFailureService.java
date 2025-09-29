package hbnu.project.zhiyancommonsecurity.service;

import hbnu.project.zhiyanauth.service.UserService;

import hbnu.project.zhiyancommonbasic.utils.ip.IpUtils;
import hbnu.project.zhiyancommonbasic.utils.StringUtils;

import hbnu.project.zhiyancommonredis.service.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;


/**
 * 登录失败处理服务
 * 实现登录失败次数限制和账户锁定
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginFailureService {

    private final RedisService redisService;
    private final UserService userService;

    private static final String LOGIN_FAILURE_KEY_PREFIX = "login:failure:";
    private static final String LOGIN_IP_FAILURE_PREFIX = "login:ip:failure:";
    private static final String LOGIN_LOCK_KEY_PREFIX = "login:lock:";

    // 邮箱登录失败限制
    private static final int MAX_EMAIL_FAILURE_COUNT = 5; // 最大失败次数
    private static final int EMAIL_LOCK_TIME_MINUTES = 30; // 邮箱锁定时间（分钟）

    // IP登录失败限制
    private static final int MAX_IP_FAILURE_COUNT = 10; // IP最大失败次数
    private static final int IP_LOCK_TIME_MINUTES = 60; // IP锁定时间（分钟）

    // 失败记录过期时间
    private static final int FAILURE_RECORD_EXPIRE_MINUTES = 60; // 失败记录过期时间


    /**
     * 记录登录失败
     *
     * @param email 用户邮箱
     * @param request HTTP请求对象，用于获取IP地址
     * @return 是否已达到锁定条件
     */
    public boolean recordLoginFailure(String email, HttpServletRequest request) {
        if (StringUtils.isBlank(email)) {
            return false;
        }

        String clientIp = IpUtils.getIpAddr(request);
        String userAgent = request.getHeader("User-Agent");

        log.warn("登录失败记录 - 邮箱: {}, IP: {}, UserAgent: {}, 时间: {}",
                email, clientIp, userAgent, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // 1. 记录邮箱失败次数
        boolean emailLocked = recordEmailFailure(email);

        // 2. 记录IP失败次数
        // 当某个 IP 地址（clientIp）登录失败时，系统会记录其失败次数，并在达到阈值时锁定该 IP，禁止其继续登录。
        boolean ipLocked = recordIpFailure(clientIp);

        return emailLocked || ipLocked;
    }


    /**
     * 记录邮箱登录失败
     */
    private boolean recordEmailFailure(String email) {
        String failureKey = LOGIN_FAILURE_KEY_PREFIX + email;
        String lockKey = LOGIN_LOCK_KEY_PREFIX + email;

        // 获取当前失败次数
        Integer failureCount = redisService.getCacheObject(failureKey);
        if (failureCount == null) {
            failureCount = 0;
        }

        failureCount++;

        // 更新失败次数，设置过期时间
        redisService.setCacheObject(failureKey, failureCount, (long) FAILURE_RECORD_EXPIRE_MINUTES, TimeUnit.MINUTES);

        log.debug("邮箱[{}]登录失败次数: {}/{}", email, failureCount, MAX_EMAIL_FAILURE_COUNT);

        // 检查是否需要锁定
        if (failureCount >= MAX_EMAIL_FAILURE_COUNT) {
            // 锁定邮箱
            redisService.setCacheObject(lockKey, LocalDateTime.now().toString(), (long) EMAIL_LOCK_TIME_MINUTES, TimeUnit.MINUTES);
            log.warn("邮箱[{}]因登录失败次数过多被锁定 {}分钟", email, EMAIL_LOCK_TIME_MINUTES);
            return true;
        }

        return false;
    }


    /**
     * 记录IP登陆失败,防止特定 IP 地址的恶意登录尝试
     */
    private boolean recordIpFailure(String clientIp) {
        String ipFailureKey = LOGIN_IP_FAILURE_PREFIX + clientIp;
        String ipLockKey = LOGIN_LOCK_KEY_PREFIX + "ip:" + clientIp;

        // 获取当前IP失败次数
        Integer ipFailureCount = redisService.getCacheObject(ipFailureKey);
        if (ipFailureCount == null) {
            ipFailureCount = 0;
        }

        ipFailureCount++;

        // 更新IP失败次数
        redisService.setCacheObject(ipFailureKey, ipFailureCount, (long) FAILURE_RECORD_EXPIRE_MINUTES, TimeUnit.MINUTES);

        log.debug("IP[{}]登录失败次数: {}/{}", clientIp, ipFailureCount, MAX_IP_FAILURE_COUNT);

        // 检查是否需要锁定IP
        if (ipFailureCount >= MAX_IP_FAILURE_COUNT) {
            redisService.setCacheObject(ipLockKey, LocalDateTime.now().toString(), (long) IP_LOCK_TIME_MINUTES, TimeUnit.MINUTES);
            log.warn("IP[{}]因登录失败次数过多被锁定 {}分钟", clientIp, IP_LOCK_TIME_MINUTES);
            return true;
        }

        return false;
    }


    /**
     * 检查邮箱是否被锁定
     */
    public boolean isEmailLocked(String email) {
        if (StringUtils.isBlank(email)) {
            return false;
        }

        String lockKey = LOGIN_LOCK_KEY_PREFIX + email;
        String lockTime = redisService.getCacheObject(lockKey);

        boolean locked = StringUtils.isNotBlank(lockTime);
        if (locked) {
            log.debug("邮箱[{}]当前处于锁定状态，锁定时间: {}", email, lockTime);
        }

        return locked;
    }


    /**
     * 检查IP是否被锁定
     */
    public boolean isIpLocked(String clientIp, HttpServletRequest request) {
        if (StringUtils.isBlank(clientIp)) {
            clientIp = IpUtils.getIpAddr(request);
        }

        String ipLockKey = LOGIN_LOCK_KEY_PREFIX + "ip:" + clientIp;
        String lockTime = redisService.getCacheObject(ipLockKey);

        boolean locked = StringUtils.isNotBlank(lockTime);
        if (locked) {
            log.debug("IP[{}]当前处于锁定状态，锁定时间: {}", clientIp, lockTime);
        }

        return locked;
    }


    /**
     * 获取邮箱剩余失败次数
     */
    public int getRemainingAttempts(String email) {
        if (StringUtils.isBlank(email)) {
            return MAX_EMAIL_FAILURE_COUNT;
        }

        String failureKey = LOGIN_FAILURE_KEY_PREFIX + email;
        Integer failureCount = redisService.getCacheObject(failureKey);

        if (failureCount == null) {
            return MAX_EMAIL_FAILURE_COUNT;
        }

        return Math.max(0, MAX_EMAIL_FAILURE_COUNT - failureCount);
    }


    /**
     * 获取锁定剩余时间（分钟）
     */
    public long getLockRemainingTime(String email) {
        if (StringUtils.isBlank(email)) {
            return 0;
        }

        String lockKey = LOGIN_LOCK_KEY_PREFIX + email;
        return redisService.getExpire(lockKey) / 60; // 转换为分钟
    }


    /**
     * 清除登录失败记录（登录成功时调用）
     */
    public void clearLoginFailure(String email, HttpServletRequest request) {
        if (StringUtils.isNotBlank(email)) {
            String failureKey = LOGIN_FAILURE_KEY_PREFIX + email;
            String lockKey = LOGIN_LOCK_KEY_PREFIX + email;

            redisService.deleteObject(failureKey);
            redisService.deleteObject(lockKey);

            log.debug("清除邮箱[{}]的登录失败记录", email);
        }

        // 可选：清除IP失败记录（如果需要的话）
        String clientIp = IpUtils.getIpAddr(request);
        if (StringUtils.isNotBlank(clientIp)) {
            String ipFailureKey = LOGIN_IP_FAILURE_PREFIX + clientIp;
            // 注意：这里不清除IP锁定，因为IP可能被多个用户使用
            // redisService.deleteObject(ipFailureKey);
        }
    }


    /**
     * 检查是否存在安全威胁（可选的高级功能）
     */
    public boolean hasSecurity威胁(String email, String clientIp) {
        // 可以实现更复杂的安全检测逻辑
        // TODO：检测是否来自黑名单IP、是否为已知攻击模式等
        // yui的大手发力了
        return false;
    }


    /**
     * 手动解锁用户（后台预留功能）
     */
    public void unlockUser(String email) {
        if (StringUtils.isNotBlank(email)) {
            String failureKey = LOGIN_FAILURE_KEY_PREFIX + email;
            String lockKey = LOGIN_LOCK_KEY_PREFIX + email;

            redisService.deleteObject(failureKey);
            redisService.deleteObject(lockKey);

            log.info("管理员手动解锁用户: {}", email);
        }
    }


    /**
     * 手动解锁IP（后台预留功能）
     */
    public void unlockIp(String clientIp) {
        if (StringUtils.isNotBlank(clientIp)) {
            String ipFailureKey = LOGIN_IP_FAILURE_PREFIX + clientIp;
            String ipLockKey = LOGIN_LOCK_KEY_PREFIX + "ip:" + clientIp;

            redisService.deleteObject(ipFailureKey);
            redisService.deleteObject(ipLockKey);

            log.info("管理员手动解锁IP: {}", clientIp);
        }
    }
}
