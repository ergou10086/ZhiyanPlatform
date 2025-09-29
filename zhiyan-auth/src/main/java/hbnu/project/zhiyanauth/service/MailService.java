package hbnu.project.zhiyanauth.service;

import hbnu.project.zhiyanauth.model.enums.VerificationCodeType;

/**
 * 邮件服务接口
 *
 * @author ErgouTree
 */
public interface MailService {

    /**
     * 发送验证码邮件
     *
     * @param toEmail 收件人邮箱
     * @param code 验证码
     * @param type 验证码类型
     * @return 邮件发送结果
     */
    boolean sendVerificationCode(String toEmail, String code, VerificationCodeType type);

    /**
     * 发送纯文本邮件
     *
     * @param toEmail 收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     * @return 邮件发送结果
     */
    boolean sendTextMail(String toEmail, String subject, String content);

    /**
     * 发送HTML邮件
     *
     * @param toEmail 收件人邮箱
     * @param subject 邮件主题
     * @param htmlContent HTML内容
     * @return 邮件发送结果
     */
    boolean sendHtmlMail(String toEmail, String subject, String htmlContent);
}
