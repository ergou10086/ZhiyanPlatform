package hbnu.project.zhiyanauth.service;

import hbnu.project.zhiyanauth.model.enums.VerificationCodeType;
import hbnu.project.zhiyancommonbasic.domain.R;

/**
 * 验证码服务接口
 *
 * @author ErgouTree
 */
public interface VerificationCodeService {

    /**
     * 生成并发送验证码
     *
     * @param email 邮箱地址
     * @param type 验证码类型
     * @return 操作结果
     */
    R<Void> generateAndSendCode(String email, VerificationCodeType type);

    /**
     * 验证验证码
     *
     * @param email 邮箱地址
     * @param code 验证码
     * @param type 验证码类型
     * @return 验证结果
     */
    R<Boolean> validateCode(String email, String code, VerificationCodeType type);

    /**
     * 检查验证码发送频率限制
     *
     * @param email 邮箱地址
     * @param type 验证码类型
     * @return 是否可以发送
     */
    boolean canSendCode(String email, VerificationCodeType type);

    /**
     * 清理过期的验证码
     */
    void cleanExpiredCodes();

    /**
     * 标记验证码为已使用
     *
     * @param email 邮箱地址
     * @param code 验证码
     * @param type 验证码类型
     */
    void markCodeAsUsed(String email, String code, VerificationCodeType type);
}