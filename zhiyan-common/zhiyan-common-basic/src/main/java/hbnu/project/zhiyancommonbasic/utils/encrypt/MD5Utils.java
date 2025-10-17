package hbnu.project.zhiyancommonbasic.utils.encrypt;

import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import hbnu.project.zhiyancommonbasic.utils.text.CharsetKit;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

/**
 * MD5加密工具类
 * 提供多种MD5加密方式，包括加盐、多重加密等安全特性
 *
 * @author yui
 */
public class MD5Utils {

    private MD5Utils() {
        throw new UnsupportedOperationException("MD5Utils cannot be instantiated");
    }

    // 十六进制字符数组
    private static final char[] HEX_DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    // 加盐字符集，随机去除一位增加对撞难度
    private static final String SALT_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMOPQRSTUVWXYZ0123456789";
    private static final int SALT_LENGTH = 8;
    private static final Random RANDOM = new SecureRandom();

    /**
     * 基础MD5加密
     *
     * @param input 输入字符串
     * @return 32位小写MD5
     */
    public static String md5(String input) {
        return md5(input, CharsetKit.CHARSET_UTF_8);
    }

    /**
     * 基础MD5加密（指定字符集）
     *
     * @param input 输入字符串
     * @param charset 字符集
     * @return 32位小写MD5
     */
    public static String md5(String input, Charset charset) {
        if (StringUtils.isEmpty(input)) {
            return null;
        }

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = input.getBytes(charset);
            return byteArrayToHexString(md.digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    /**
     * 生成32位大写MD5
     *
     * @param input 输入字符串
     * @return 32位大写MD5
     */
    public static String md5Upper(String input) {
        String result = md5(input);
        return result != null ? result.toUpperCase() : null;
    }

    /**
     * 生成16位小写MD5
     *
     * @param input 输入字符串
     * @return 16位小写MD5
     */
    public static String md516(String input) {
        String result = md5(input);
        return result != null ? result.substring(8, 24) : null;
    }

    /**
     * 生成16位大写MD5
     *
     * @param input 输入字符串
     * @return 16位大写MD5
     */
    public static String md5Upper16(String input) {
        String result = md516(input);
        return result != null ? result.toUpperCase() : null;
    }

    /**
     * 加盐MD5加密
     *
     * @param input 输入字符串
     * @param salt 盐值
     * @return 加盐后的MD5
     */
    public static String md5WithSalt(String input, String salt) {
        if (StringUtils.isEmpty(input)) {
            return null;
        }
        if (StringUtils.isEmpty(salt)) {
            return md5(input);
        }
        return md5(input + salt);
    }

    /**
     * 自动生成盐值的MD5加密
     *
     * @param input 输入字符串
     * @return 包含盐值和加密结果的MD5Salt对象
     */
    public static MD5Salt md5WithAutoSalt(String input) {
        String salt = generateSalt();
        String encrypted = md5WithSalt(input, salt);
        return new MD5Salt(encrypted, salt);
    }

    /**
     * 多重MD5加密
     *
     * @param input 输入字符串
     * @param times 加密次数
     * @return 多重加密结果
     */
    public static String md5Multiple(String input, int times) {
        if (StringUtils.isEmpty(input) || times <= 0) {
            return input;
        }

        String result = input;
        for (int i = 0; i < times; i++) {
            result = md5(result);
        }
        return result;
    }

    /**
     * 验证MD5（普通）
     *
     * @param input 原始字符串
     * @param md5 MD5值
     * @return 是否匹配
     */
    public static boolean verify(String input, String md5) {
        if (StringUtils.isEmpty(input) || StringUtils.isEmpty(md5)) {
            return false;
        }
        return md5.equals(md5(input));
    }

    /**
     * 验证MD5（加盐）
     *
     * @param input 原始字符串
     * @param md5 MD5值
     * @param salt 盐值
     * @return 是否匹配
     */
    public static boolean verifyWithSalt(String input, String md5, String salt) {
        if (StringUtils.isEmpty(input) || StringUtils.isEmpty(md5) || StringUtils.isEmpty(salt)) {
            return false;
        }
        return md5.equals(md5WithSalt(input, salt));
    }

    /**
     * 生成随机盐值
     *
     * @return 随机盐值
     */
    public static String generateSalt() {
        StringBuilder salt = new StringBuilder();
        for (int i = 0; i < SALT_LENGTH; i++) {
            salt.append(SALT_CHARS.charAt(RANDOM.nextInt(SALT_CHARS.length())));
        }
        return salt.toString();
    }

    /**
     * 文件MD5计算
     *
     * @param bytes 文件字节数组
     * @return 文件MD5值
     */
    public static String md5File(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return byteArrayToHexString(md.digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    /**
     * 安全的MD5比较（防止时序攻击）
     *
     * @param md5a MD5值A
     * @param md5b MD5值B
     * @return 是否相等
     */
    public static boolean safeEquals(String md5a, String md5b) {
        if (md5a == null || md5b == null) {
            return false;
        }

        // 使用恒定时间比较，防止时序攻击
        if (md5a.length() != md5b.length()) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < md5a.length(); i++) {
            result |= md5a.charAt(i) ^ md5b.charAt(i);
        }
        return result == 0;
    }

    /**
     * 字节数组转十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String byteArrayToHexString(byte[] bytes) {
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            result.append(HEX_DIGITS[(b & 0xf0) >>> 4]);
            result.append(HEX_DIGITS[b & 0x0f]);
        }
        return result.toString();
    }

    /**
     * MD5加盐结果封装类
     */
    public static class MD5Salt {
        private final String encrypted;
        private final String salt;

        public MD5Salt(String encrypted, String salt) {
            this.encrypted = encrypted;
            this.salt = salt;
        }

        public String getEncrypted() {
            return encrypted;
        }

        public String getSalt() {
            return salt;
        }

        @Override
        public String toString() {
            return "MD5Salt{" +
                    "encrypted='" + encrypted + '\'' +
                    ", salt='" + salt + '\'' +
                    '}';
        }
    }

    /**
     * 密码强度校验（配合MD5使用）
     *
     * @param password 密码
     * @return 强度级别：0-弱，1-中，2-强
     */
    public static int checkPasswordStrength(String password) {
        if (StringUtils.isEmpty(password)) {
            return 0;
        }

        int strength = 0;

        // 长度检查
        if (password.length() >= 8) strength++;
        if (password.length() >= 12) strength++;

        // 包含数字
        if (password.matches(".*\\d.*")) strength++;

        // 包含小写字母
        if (password.matches(".*[a-z].*")) strength++;

        // 包含大写字母
        if (password.matches(".*[A-Z].*")) strength++;

        // 包含特殊字符
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) strength++;

        return Math.min(strength / 2, 2);
    }

    /**
     * 获取密码强度描述
     *
     * @param password 密码
     * @return 强度描述
     */
    public static String getPasswordStrengthDesc(String password) {
        int strength = checkPasswordStrength(password);
        switch (strength) {
            case 0: return "弱";
            case 1: return "中";
            case 2: return "强";
            default: return "未知";
        }
    }
}