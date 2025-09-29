package hbnu.project.zhiyanauth.utils;

import hbnu.project.zhiyancommonbasic.utils.id.UUID;

import java.security.SecureRandom;

/**
 * 验证码生成工具类
 *
 * @author ErgouTree
 */
public class VerificationCodeGenerator {

    private static final SecureRandom SECURE_RANDOM = UUID.getSecureRandom();

    // 数字验证码字符集
    private static final String NUMERIC_CHARS = "0123456789";

    // 字母数字验证码字符集（排除易混淆字符）
    private static final String ALPHANUMERIC_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";

    /**
     * 生成纯数字验证码
     *
     * @param length 验证码长度
     * @return 验证码字符串
     */
    public static String generateNumericCode(int length) {
        return generateCode(NUMERIC_CHARS, length);
    }

    /**
     * 生成字母数字验证码（排除易混淆字符）
     *
     * @param length 验证码长度
     * @return 验证码字符串
     */
    public static String generateAlphanumericCode(int length) {
        return generateCode(ALPHANUMERIC_CHARS, length);
    }

    /**
     * 根据字符集生成验证码
     *
     * @param charset 字符集
     * @param length 长度
     * @return 验证码字符串
     */
    private static String generateCode(String charset, int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("验证码长度必须大于0");
        }

        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = SECURE_RANDOM.nextInt(charset.length());
            code.append(charset.charAt(index));
        }

        return code.toString();
    }
}
