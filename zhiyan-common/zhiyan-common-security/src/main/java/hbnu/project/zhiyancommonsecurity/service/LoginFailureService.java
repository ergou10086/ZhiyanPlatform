package hbnu.project.zhiyancommonsecurity.service;

// common-security 模块


import jakarta.servlet.http.HttpServletRequest;

/**
 * 登录失败处理服务接口
 * 提供登录失败处理的标准定义
 *
 * @author yxy
 */
public interface LoginFailureService {

    /**
     * 记录登录失败
     */
    boolean recordLoginFailure(String email, HttpServletRequest request);

    /**
     * 检查邮箱是否被锁定
     */
    boolean isEmailLocked(String email);

    /**
     * 检查IP是否被锁定
     */
    boolean isIpLocked(String clientIp, HttpServletRequest request);

    /**
     * 获取邮箱剩余失败次数
     */
    int getRemainingAttempts(String email);

    /**
     * 获取锁定剩余时间（分钟）
     */
    long getLockRemainingTime(String email);

    /**
     * 清除登录失败记录（登录成功时调用）
     */
    void clearLoginFailure(String email, HttpServletRequest request);

    /**
     * 手动解锁用户（后台预留功能）
     */
    void unlockUser(String email);

    /**
     * 手动解锁IP（后台预留功能）
     */
    void unlockIp(String clientIp);
}