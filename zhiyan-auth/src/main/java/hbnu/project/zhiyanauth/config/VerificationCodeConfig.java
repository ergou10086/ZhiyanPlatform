package hbnu.project.zhiyanauth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 验证码配置类
 *
 * @author ErgouTree
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.verification-code")
public class VerificationCodeConfig {

    /**
     * 验证码长度
     */
    private int length = 6;

    /**
     * 验证码过期时间（分钟）
     */
    private int expireMinutes = 10;

    /**
     * 发送频率限制（分钟）
     */
    private int rateLimitMinutes = 1;

    /**
     * 是否启用数据库存储
     */
    private boolean enableDatabaseStorage = true;

    /**
     * 是否启用邮件发送
     */
    private boolean enableEmailSending = true;

    /**
     * 最大重试次数
     */
    private int maxRetryAttempts = 3;
}
