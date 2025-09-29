package hbnu.project.zhiyanauth.service.impl;

import hbnu.project.zhiyanauth.model.enums.VerificationCodeType;
import hbnu.project.zhiyanauth.service.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * 邮件服务实现类
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:zhiyan163verif@163.com}")
    private String fromEmail;

    @Value("${app.name:智研平台}")
    private String appName;

    private static final int CODE_EXPIRE_MINUTES = 10;


    @Override
    public boolean sendVerificationCode(String toEmail, String code, VerificationCodeType type) {
        try {
            String subject = buildEmailSubject(type);
            String content = buildEmailContent(code, type);

            return sendTextMail(toEmail, subject, content);
        } catch (Exception e) {
            log.error("发送验证码邮件失败 - 收件人: {}, 类型: {}, 错误: {}", toEmail, type, e.getMessage(), e);
            return false;
        }
    }


    @Override
    public boolean sendTextMail(String toEmail, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);
            log.info("文本邮件发送成功 - 收件人: {}, 主题: {}", toEmail, subject);
            return true;
        } catch (Exception e) {
            log.error("文本邮件发送失败 - 收件人: {}, 主题: {}, 错误: {}", toEmail, subject, e.getMessage(), e);
            return false;
        }
    }


    @Override
    public boolean sendHtmlMail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("HTML邮件发送成功 - 收件人: {}, 主题: {}", toEmail, subject);
            return true;
        } catch (MessagingException e) {
            log.error("HTML邮件发送失败 - 收件人: {}, 主题: {}, 错误: {}", toEmail, subject, e.getMessage(), e);
            return false;
        }
    }


    /**
     * 构建邮件主题
     */
    private String buildEmailSubject(VerificationCodeType type) {
        return switch (type) {
            case REGISTER -> appName + " - 注册验证码";
            case RESET_PASSWORD -> appName + " - 密码重置验证码";
            case CHANGE_EMAIL -> appName + " - 邮箱变更验证码";
        };
    }


    /**
     * 构建邮件内容
     */
    private String buildEmailContent(String code, VerificationCodeType type) {
        String action = switch (type) {
            case REGISTER -> "注册账户";
            case RESET_PASSWORD -> "重置密码";
            case CHANGE_EMAIL -> "变更邮箱";
        };

        return String.format(
                """
                您好！
                
                您正在进行%s操作,验证码为：%s
                
                验证码有效期为%d分钟,请及时使用。
                如果这不是您的操作,请忽略此邮件。
                
                %s团队""",
                action, code, CODE_EXPIRE_MINUTES, appName
        );
    }
}
