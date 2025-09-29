package hbnu.project.zhiyanauth.service.impl;

import hbnu.project.zhiyanauth.model.entity.VerificationCode;
import hbnu.project.zhiyanauth.model.enums.VerificationCodeType;
import hbnu.project.zhiyanauth.repository.VerificationCodeRepository;
import hbnu.project.zhiyanauth.service.VerificationCodeService;
import hbnu.project.zhiyanauth.utils.VerificationCodeGenerator;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonredis.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现类
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private final RedisService redisService;
    private final VerificationCodeRepository verificationCodeRepository;
    private final JavaMailSender mailSender;

    // Redis键前缀，用于区分不同类型的缓存键
    private static final String VERIFICATION_CODE_PREFIX = "verification_code:";   // 验证码缓存前缀
    private static final String RATE_LIMIT_PREFIX = "rate_limit:verification_code:";   // 发送频率限制前缀
    private static final String USED_CODE_PREFIX = "used_verification_code:";    // 已使用验证码标记前缀

    // 验证码配置
    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRE_MINUTES = 10;     // 验证码10分钟过期
    private static final int RATE_LIMIT_MINUTES = 1;       // 1分钟内只能发送一次

    // 发送验证码的邮箱地址
    @Value("${spring.mail.username:zhiyan163verif@163.com}")
    private String fromEmail;

    @Value("${app.name:智研平台}")
    private String appName;

    /**
     * 生成并发送验证码
     * 流程：检查发送频率 -> 生成验证码 -> 缓存到Redis -> 持久化到数据库 -> 发送邮件 -> 设置频率限制
     *
     * @param email 接收验证码的邮箱地址
     * @param type  验证码类型（注册、密码重置等）
     * @return 处理结果（成功/失败信息）
     */
    @Override
    @Transactional
    public R<Void> generateAndSendCode(String email, VerificationCodeType type) {
        try {
            // 检查发送频率限制
            if (!canSendCode(email, type)) {
                return R.fail("验证码发送过于频繁，请稍后再试");
            }

            // 生成指定长度的数字验证码
            String code = VerificationCodeGenerator.generateNumericCode(CODE_LENGTH);

            // 构建Redis缓存键，并将验证码存入Redis，设置过期时间
            String redisKey = buildRedisKey(email, type);
            redisService.setCacheObject(redisKey, code, (long) CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

            // 构建验证码实体，并存入数据库（用于审计和Redis失效后的兜底验证）
            VerificationCode verificationCode = VerificationCode.builder()
                    .email(email)
                    .code(code)
                    .type(type)
                    .expiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRE_MINUTES))
                    .isUsed(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            verificationCodeRepository.save(verificationCode);

            // 发送验证码邮件
            sendCodeByEmail(email, code, type);

            // 设置频率限制：1分钟内不允许再次发送
            String rateLimitKey = buildRateLimitKey(email, type);
            redisService.setCacheObject(rateLimitKey, "1", (long) RATE_LIMIT_MINUTES, TimeUnit.MINUTES);

            log.info("验证码发送成功 - 邮箱: {}, 类型: {}", email, type);
            return R.ok(null, "验证码发送成功");

        } catch (Exception e) {
            log.error("验证码发送失败 - 邮箱: {}, 类型: {}, 错误: {}", email, type, e.getMessage(), e);
            return R.fail("验证码发送失败，请稍后重试");
        }
    }


    /**
     * 验证验证码的有效性
     * 流程：检查是否已使用 -> Redis验证 -> 数据库兜底验证 -> 标记为已使用
     *
     * @param email 接收验证码的邮箱
     * @param code  待验证的验证码
     * @param type  验证码类型
     * @return 验证结果（成功/失败原因）
     */
    @Override
    public R<Boolean> validateCode(String email, String code, VerificationCodeType type) {
        try {
            // 检查验证码是否已被使用
            String usedKey = buildUsedCodeKey(email, code, type);
            if (redisService.hasKey(usedKey)) {
                log.warn("验证码已被使用 - 邮箱: {}, 验证码: {}, 类型: {}", email, code, type);
                return R.ok(false, "验证码已被使用");
            }

            // 先从Redis验证（快速）
            String redisKey = buildRedisKey(email, type);
            String storedCode = redisService.getCacheObject(redisKey);

            if (storedCode != null && storedCode.equals(code)) {
                // Redis验证成功，标记为已使用
                markCodeAsUsed(email, code, type);
                log.info("验证码验证成功(Redis) - 邮箱: {}, 类型: {}", email, type);
                return R.ok(true, "验证码验证成功");
            }

            // Redis中没有，从数据库验证（兜底）
            var optionalCode = verificationCodeRepository
                    .findByEmailAndCodeAndTypeAndIsUsedFalse(email, code, type);

            if (optionalCode.isPresent()) {
                VerificationCode verificationCode = optionalCode.get();

                // 检查数据库中的验证码是否过期
                if (verificationCode.getExpiresAt().isBefore(LocalDateTime.now())) {
                    log.warn("验证码已过期 - 邮箱: {}, 类型: {}", email, type);
                    return R.ok(false, "验证码已过期");
                }

                // 标记数据库中的验证码为已使用
                verificationCode.setIsUsed(true);
                verificationCodeRepository.save(verificationCode);

                // 同时标记Redis中的验证码为已使用
                markCodeAsUsed(email, code, type);

                log.info("验证码验证成功(数据库) - 邮箱: {}, 类型: {}", email, type);
                return R.ok(true, "验证码验证成功");
            }

            // 所有验证途径均失败（验证码错误或已过期）
            log.warn("验证码验证失败 - 邮箱: {}, 验证码: {}, 类型: {}", email, code, type);
            return R.ok(false, "验证码错误或已过期");

        } catch (Exception e) {
            log.error("验证码验证异常 - 邮箱: {}, 类型: {}, 错误: {}", email, type, e.getMessage(), e);
            return R.fail("验证码验证失败，请稍后重试");
        }
    }


    /**
     * 检查是否允许发送验证码（频率控制）
     *
     * @param email 接收邮箱
     * @param type  验证码类型
     * @return true-允许发送，false-不允许（频率超限）
     */
    @Override
    public boolean canSendCode(String email, VerificationCodeType type) {
        String rateLimitKey = buildRateLimitKey(email, type);
        // 如果Redis中存在该键，说明在频率限制时间内已发送过
        return !redisService.hasKey(rateLimitKey);
    }


    /**
     * 清理过期的验证码（数据库层面）
     * 定期执行，删除已过期且未使用的验证码，释放存储空间
     */
    @Override
    @Transactional
    public void cleanExpiredCodes() {
        try {
            LocalDateTime now = LocalDateTime.now();
            int deletedCount = verificationCodeRepository.deleteExpiredCodes(now);
            log.info("清理过期验证码完成，删除数量: {}", deletedCount);
        } catch (Exception e) {
            log.error("清理过期验证码失败: {}", e.getMessage(), e);
        }
    }


    /**
     * 定时清理过期验证码任务
     * 每小时执行一次，自动清理数据库中的过期验证码
     * 可通过配置 app.verification-code.enable-cleanup-task 控制是否启用
     */
    @Scheduled(cron = "0 0 * * * ?")
    @ConditionalOnProperty(name = "app.verification-code.enable-cleanup-task", havingValue = "true", matchIfMissing = true)
    public void scheduledCleanupExpiredCodes() {
        log.debug("开始执行定时清理过期验证码任务");
        try {
            cleanExpiredCodes();
            // 可以在这里添加更多清理逻辑，比如清理Redis中的过期键
            cleanupRedisExpiredKeys();
            log.debug("定时清理过期验证码任务执行完成");
        } catch (Exception e) {
            log.error("定时清理过期验证码任务执行失败", e);
        }
    }


    /**
     * 清理Redis中可能残留的过期验证码键
     * 虽然Redis有TTL自动过期机制，但为了确保清理彻底，定期检查并清理，由yui提供
     */
    private void cleanupRedisExpiredKeys() {
        try {
            // 清理验证码键
            var codeKeys = redisService.keys(VERIFICATION_CODE_PREFIX + "*");
            int cleanedCodeKeys = 0;
            for (String key : codeKeys) {
                if (!redisService.hasKey(key)) {
                    cleanedCodeKeys++;
                }
            }

            // 清理已使用验证码键（这些键可能因为TTL设置问题而残留）
            var usedKeys = redisService.keys(USED_CODE_PREFIX + "*");
            int cleanedUsedKeys = 0;
            for (String key : usedKeys) {
                if (!redisService.hasKey(key)) {
                    cleanedUsedKeys++;
                }
            }

            if (cleanedCodeKeys > 0 || cleanedUsedKeys > 0) {
                log.info("Redis清理完成 - 验证码键: {}, 已使用键: {}", cleanedCodeKeys, cleanedUsedKeys);
            }
        } catch (Exception e) {
            log.warn("Redis验证码键清理失败: {}", e.getMessage());
        }
    }


    /**
     * 标记验证码为已使用
     *
     * @param email 接收邮箱
     * @param code  验证码
     * @param type  验证码类型
     */
    @Override
    public void markCodeAsUsed(String email, String code, VerificationCodeType type) {
        // 在Redis中标记为已使用，设置与验证码相同的过期时间
        String usedKey = buildUsedCodeKey(email, code, type);
        redisService.setCacheObject(usedKey, "1", (long) CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 删除原始验证码缓存，防止重复验证
        String redisKey = buildRedisKey(email, type);
        redisService.deleteObject(redisKey);
    }


    /**
     * 发送验证码邮件
     *
     * @param email 接收邮箱
     * @param code  验证码
     * @param type  验证码类型
     */
    private void sendCodeByEmail(String email, String code, VerificationCodeType type) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject(buildEmailSubject(type));
            message.setText(buildEmailContent(code, type));

            mailSender.send(message);
            log.info("验证码邮件发送成功 - 邮箱: {}", email);

        } catch (Exception e) {
            log.error("验证码邮件发送失败 - 邮箱: {}, 错误: {}", email, e.getMessage(), e);
            throw new RuntimeException("邮件发送失败", e);
        }
    }


    /**
     * 构建验证码在Redis中的缓存键
     * 格式：verification_code:类型:邮箱
     *
     * @param email 接收邮箱
     * @param type  验证码类型
     * @return 构建后的Redis键
     */
    private String buildRedisKey(String email, VerificationCodeType type) {
        return VERIFICATION_CODE_PREFIX + type.name().toLowerCase() + ":" + email;
    }


    /**
     * 构建频率限制在Redis中的缓存键
     * 格式：rate_limit:verification_code:类型:邮箱
     *
     * @param email 接收邮箱
     * @param type  验证码类型
     * @return 构建后的Redis键
     */
    private String buildRateLimitKey(String email, VerificationCodeType type) {
        return RATE_LIMIT_PREFIX + type.name().toLowerCase() + ":" + email;
    }


    /**
     * 构建已使用验证码在Redis中的标记键
     * 格式：used_verification_code:类型:邮箱:验证码
     *
     * @param email 接收邮箱
     * @param code  验证码
     * @param type  验证码类型
     * @return 构建后的Redis键
     */
    private String buildUsedCodeKey(String email, String code, VerificationCodeType type) {
        return USED_CODE_PREFIX + type.name().toLowerCase() + ":" + email + ":" + code;
    }


    /**
     * 构建邮件主题（根据验证码类型动态生成）
     *
     * @param type 验证码类型
     * @return 邮件主题
     */
    private String buildEmailSubject(VerificationCodeType type) {
        return switch (type) {
            case REGISTER -> appName + " - 注册验证码";
            case RESET_PASSWORD -> appName + " - 密码重置验证码";
            case CHANGE_EMAIL -> appName + " - 邮箱变更验证码";
        };
    }


    /**
     * 构建邮件内容（包含验证码、有效期等信息）
     *
     * @param code 验证码
     * @param type 验证码类型
     * @return 邮件内容
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
                        
                        您正在进行%s操作，验证码为：%s
                        
                        验证码有效期为%d分钟，请及时使用。
                        如果这不是您的操作，请忽略此邮件。
                        
                        %s团队""",
                action, code, CODE_EXPIRE_MINUTES, appName
        );
    }
}
