package hbnu.project.zhiyanauth.service;

import hbnu.project.zhiyancommonsecurity.service.AbstractLoginFailureService;
import hbnu.project.zhiyanauth.service.UserService;
import hbnu.project.zhiyancommonbasic.utils.ip.IpUtils;
import hbnu.project.zhiyancommonredis.service.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 认证模块登录失败服务实现
 * 扩展基础功能，添加业务特定逻辑
 */
@Slf4j
@Service
public class AuthLoginFailureService extends AbstractLoginFailureService {

    private final UserService userService;

    public AuthLoginFailureService(RedisService redisService, UserService userService) {
        super(redisService);
        this.userService = userService;
    }

    /**
     * 扩展记录登录失败方法，添加业务逻辑
     */
    @Override
    public boolean recordLoginFailure(String email, HttpServletRequest request) {
        boolean locked = super.recordLoginFailure(email, request);

        // 业务特定逻辑：如果账户被锁定，可以发送通知等
        if (locked) {
            handleAccountLocked(email, request);
        }

        return locked;
    }

    /**
     * 处理账户锁定业务逻辑
     */
    private void handleAccountLocked(String email, HttpServletRequest request) {
        String clientIp = IpUtils.getIpAddr(request);
        log.warn("账户锁定处理 - 邮箱: {}, IP: {}", email, clientIp);

        // 业务逻辑：可以在这里添加
        // 1. 发送邮件通知用户
        // 2. 记录安全审计日志
        // 3. 调用用户服务更新状态等

        // 示例：记录安全事件
        // securityAuditService.recordSecurityEvent(email, "ACCOUNT_LOCKED", clientIp);
    }

    /**
     * 检查是否存在安全威胁（业务特定方法）
     */
    public boolean hasSecurityThreat(String email, String clientIp) {
        // 业务逻辑：检测是否来自黑名单IP、是否为已知攻击模式等
        // 可以调用其他服务进行更复杂的安全检测

        // 示例：检查IP是否在黑名单中
        // return ipBlacklistService.isBlacklisted(clientIp);

        return false;
    }

    /**
     * 业务特定的解锁方法，可以添加额外逻辑
     */
    @Override
    public void unlockUser(String email) {
        super.unlockUser(email);

        // 业务逻辑：解锁后的额外处理
        log.info("用户解锁完成，可以发送通知或记录日志 - 邮箱: {}", email);
        // userService.sendUnlockNotification(email);
    }
}