package hbnu.project.zhiyanauth.service.impl;

import hbnu.project.zhiyanauth.model.entity.VerificationCode;
import hbnu.project.zhiyanauth.model.enums.VerificationCodeType;
import hbnu.project.zhiyanauth.repository.VerificationCodeRepository;
import hbnu.project.zhiyanauth.service.MailService;
import hbnu.project.zhiyanauth.service.VerificationCodeService;
import hbnu.project.zhiyanauth.utils.VerificationCodeGenerator;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonredis.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
    private final MailService mailService;

    // Redis键前缀
    private static final String VERIFICATION_CODE_PREFIX = "verification_code:";
    private static final String RATE_LIMIT_PREFIX = "rate_limit:verification_code:";
    private static final String USED_CODE_PREFIX = "used_verification_code:";

    // 验证码配置
    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRE_MINUTES = 10;
    private static final int RATE_LIMIT_MINUTES = 1;

    /**
     * 生成并发送验证码
     *
     * @param email  邮箱
     * @param type   验证码类型
     * @return R<Void>
     */
    @Override
    @Transactional
    public R<Void> generateAndSendCode(String email, VerificationCodeType type) {
        try {
            // 检查发送频率限制
            if (!canSendCode(email, type)) {
                return R.fail("验证码发送过于频繁,请稍后再试");
            }

            // 生成验证码
            String code = VerificationCodeGenerator.generateNumericCode(CODE_LENGTH);

            // 存入Redis缓存
            String redisKey = buildRedisKey(email, type);
            redisService.setCacheObject(redisKey, code, (long) CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

            // 持久化到数据库
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
            boolean emailSent = mailService.sendVerificationCode(email, code, type);
            if (!emailSent) {
                log.warn("验证码邮件发送失败,但已保存到数据库 - 邮箱: {}, 类型: {}", email, type);
                return R.fail("邮件发送失败,请稍后重试");
            }

            // 设置频率限制
            String rateLimitKey = buildRateLimitKey(email, type);
            redisService.setCacheObject(rateLimitKey, "1", (long) RATE_LIMIT_MINUTES, TimeUnit.MINUTES);

            log.info("验证码发送成功 - 邮箱: {}, 类型: {}", email, type);
            return R.ok(null, "验证码发送成功");

        } catch (Exception e) {
            log.error("验证码发送失败 - 邮箱: {}, 类型: {}, 错误: {}", email, type, e.getMessage(), e);
            return R.fail("验证码发送失败,请稍后重试");
        }
    }

    @Override
    public R<Boolean> validateCode(String email, String code, VerificationCodeType type) {
        try {
            // 检查验证码是否已被使用
            String usedKey = buildUsedCodeKey(email, code, type);
            if (redisService.hasKey(usedKey)) {
                log.warn("验证码已被使用 - 邮箱: {}, 验证码: {}, 类型: {}", email, code, type);
                return R.ok(false, "验证码已被使用");
            }

            // 先从Redis验证
            String redisKey = buildRedisKey(email, type);
            String storedCode = redisService.getCacheObject(redisKey);

            if (storedCode != null && storedCode.equals(code)) {
                markCodeAsUsed(email, code, type);
                log.info("验证码验证成功(Redis) - 邮箱: {}, 类型: {}", email, type);
                return R.ok(true, "验证码验证成功");
            }

            // 从数据库验证(兜底)
            var optionalCode = verificationCodeRepository
                    .findByEmailAndCodeAndTypeAndIsUsedFalse(email, code, type);

            if (optionalCode.isPresent()) {
                VerificationCode verificationCode = optionalCode.get();

                if (verificationCode.getExpiresAt().isBefore(LocalDateTime.now())) {
                    log.warn("验证码已过期 - 邮箱: {}, 类型: {}", email, type);
                    return R.ok(false, "验证码已过期");
                }

                verificationCode.setIsUsed(true);
                verificationCodeRepository.save(verificationCode);
                markCodeAsUsed(email, code, type);

                log.info("验证码验证成功(数据库) - 邮箱: {}, 类型: {}", email, type);
                return R.ok(true, "验证码验证成功");
            }

            log.warn("验证码验证失败 - 邮箱: {}, 验证码: {}, 类型: {}", email, code, type);
            return R.ok(false, "验证码错误或已过期");

        } catch (Exception e) {
            log.error("验证码验证异常 - 邮箱: {}, 类型: {}, 错误: {}", email, type, e.getMessage(), e);
            return R.fail("验证码验证失败,请稍后重试");
        }
    }

    @Override
    public boolean canSendCode(String email, VerificationCodeType type) {
        String rateLimitKey = buildRateLimitKey(email, type);
        return !redisService.hasKey(rateLimitKey);
    }

    @Override
    @Transactional
    public void cleanExpiredCodes() {
        try {
            LocalDateTime now = LocalDateTime.now();
            int deletedCount = verificationCodeRepository.deleteExpiredCodes(now);
            log.info("清理过期验证码完成,删除数量: {}", deletedCount);
        } catch (Exception e) {
            log.error("清理过期验证码失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public void markCodeAsUsed(String email, String code, VerificationCodeType type) {
        String usedKey = buildUsedCodeKey(email, code, type);
        redisService.setCacheObject(usedKey, "1", (long) CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        String redisKey = buildRedisKey(email, type);
        redisService.deleteObject(redisKey);
    }

    @Scheduled(cron = "0 0 * * * ?")
    @ConditionalOnProperty(name = "app.verification-code.enable-cleanup-task", havingValue = "true", matchIfMissing = true)
    public void scheduledCleanupExpiredCodes() {
        log.debug("开始执行定时清理过期验证码任务");
        try {
            cleanExpiredCodes();
            cleanupRedisExpiredKeys();
            log.debug("定时清理过期验证码任务执行完成");
        } catch (Exception e) {
            log.error("定时清理过期验证码任务执行失败", e);
        }
    }

    private void cleanupRedisExpiredKeys() {
        try {
            var codeKeys = redisService.keys(VERIFICATION_CODE_PREFIX + "*");
            int cleanedCodeKeys = 0;
            for (String key : codeKeys) {
                if (!redisService.hasKey(key)) {
                    cleanedCodeKeys++;
                }
            }

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

    private String buildRedisKey(String email, VerificationCodeType type) {
        return VERIFICATION_CODE_PREFIX + type.name().toLowerCase() + ":" + email;
    }

    private String buildRateLimitKey(String email, VerificationCodeType type) {
        return RATE_LIMIT_PREFIX + type.name().toLowerCase() + ":" + email;
    }

    private String buildUsedCodeKey(String email, String code, VerificationCodeType type) {
        return USED_CODE_PREFIX + type.name().toLowerCase() + ":" + email + ":" + code;
    }
}