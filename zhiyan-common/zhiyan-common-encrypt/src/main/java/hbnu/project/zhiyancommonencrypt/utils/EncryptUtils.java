package hbnu.project.zhiyancommonencrypt.utils;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.crypto.symmetric.SM4;
import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import hbnu.project.zhiyancommonbasic.utils.encrypt.MD5Utils;
import hbnu.project.zhiyancommonbasic.utils.encrypt.RSAUtils;
import hbnu.project.zhiyancommonencrypt.enumd.AlgorithmType;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * 加解密工具类
 * 整合所有加密算法，提供统一的加解密接口
 * 充分利用 basic 模块的 MD5Utils 和 RSAUtils
 *
 * @author ErgouTree
 * @rewrite yui
 * @Re_rewrite ErgouTree
 * yui: 6666666677,你二逼吧
 */
@Slf4j
public class EncryptUtils {

    // ==================== 默认密钥配置 ====================

    /**
     * 默认 AES 密钥（16位）
     * 生产环境应从配置文件读取
     */
    private static String DEFAULT_AES_KEY = "zhiyan1234567890";

    /**
     * 默认 SM4 密钥（16位）
     * 生产环境应从配置文件读取
     */
    private static String DEFAULT_SM4_KEY = "zhiyan1234567890";

    /**
     * 默认 RSA 公钥
     * 生产环境应从配置文件读取
     */
    private static String DEFAULT_RSA_PUBLIC_KEY;

    /**
     * 默认 RSA 私钥
     * 生产环境应从配置文件读取
     */
    private static String DEFAULT_RSA_PRIVATE_KEY;

    // ==================== 配置方法 ====================

    /**
     * 设置默认 AES 密钥
     *
     * @param key AES 密钥（16/24/32位）
     */
    public static void setDefaultAesKey(String key) {
        DEFAULT_AES_KEY = key;
    }

    /**
     * 设置默认 SM4 密钥
     *
     * @param key SM4 密钥（16位）
     */
    public static void setDefaultSm4Key(String key) {
        DEFAULT_SM4_KEY = key;
    }

    /**
     * 设置默认 RSA 密钥对
     *
     * @param publicKey  公钥
     * @param privateKey 私钥
     */
    public static void setDefaultRsaKeys(String publicKey, String privateKey) {
        DEFAULT_RSA_PUBLIC_KEY = publicKey;
        DEFAULT_RSA_PRIVATE_KEY = privateKey;
    }

    // ==================== MD5 加密（使用 basic 模块）====================

    /**
     * MD5 加密（来自 basic 模块）
     *
     * @param data 原始数据
     * @return MD5 值（32位小写）
     */
    public static String md5(String data) {
        return MD5Utils.md5(data);
    }

    /**
     * MD5 加盐加密（来自 basic 模块）
     *
     * @param data 原始数据
     * @param salt 盐值
     * @return MD5 值
     */
    public static String md5WithSalt(String data, String salt) {
        return MD5Utils.md5WithSalt(data, salt);
    }

    /**
     * 生成随机盐值（来自 basic 模块）
     *
     * @return 8位随机盐值
     */
    public static String generateSalt() {
        return MD5Utils.generateSalt();
    }

    /**
     * MD5 验证（来自 basic 模块）
     *
     * @param data 原始数据
     * @param md5  MD5 值
     * @return 是否匹配
     */
    public static boolean verifyMd5(String data, String md5) {
        return MD5Utils.verify(data, md5);
    }

    // ==================== AES 加密 ====================

    /**
     * AES 加密（使用默认密钥）
     *
     * @param data 原始数据
     * @return Base64 编码的加密结果
     */
    public static String aesEncrypt(String data) {
        return aesEncrypt(data, DEFAULT_AES_KEY);
    }

    /**
     * AES 加密（自定义密钥）
     *
     * @param data 原始数据
     * @param key  AES 密钥
     * @return Base64 编码的加密结果
     */
    public static String aesEncrypt(String data, String key) {
        if (StringUtils.isEmpty(data)) {
            return data;
        }
        try {
            AES aes = SecureUtil.aes(key.getBytes(StandardCharsets.UTF_8));
            byte[] encrypted = aes.encrypt(data);
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("AES 加密失败", e);
            throw new RuntimeException("AES 加密失败: " + e.getMessage(), e);
        }
    }

    /**
     * AES 解密（使用默认密钥）
     *
     * @param encryptedData Base64 编码的加密数据
     * @return 解密后的原始数据
     */
    public static String aesDecrypt(String encryptedData) {
        return aesDecrypt(encryptedData, DEFAULT_AES_KEY);
    }

    /**
     * AES 解密（自定义密钥）
     *
     * @param encryptedData Base64 编码的加密数据
     * @param key           AES 密钥
     * @return 解密后的原始数据
     */
    public static String aesDecrypt(String encryptedData, String key) {
        if (StringUtils.isEmpty(encryptedData)) {
            return encryptedData;
        }
        try {
            AES aes = SecureUtil.aes(key.getBytes(StandardCharsets.UTF_8));
            byte[] encrypted = Base64.getDecoder().decode(encryptedData);
            byte[] decrypted = aes.decrypt(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES 解密失败", e);
            throw new RuntimeException("AES 解密失败: " + e.getMessage(), e);
        }
    }

    // ==================== RSA 加密（使用 basic 模块）====================

    /**
     * 生成 RSA 密钥对（来自 basic 模块）
     *
     * @return 密钥对 Map（包含 publicKey 和 privateKey）
     */
    public static Map<String, String> generateRsaKeyPair() {
        try {
            return RSAUtils.generateKeyPair();
        } catch (Exception e) {
            log.error("生成 RSA 密钥对失败", e);
            throw new RuntimeException("生成 RSA 密钥对失败: " + e.getMessage(), e);
        }
    }

    /**
     * RSA 公钥加密（使用默认公钥）
     *
     * @param data 原始数据
     * @return Base64 编码的加密结果
     */
    public static String rsaEncrypt(String data) {
        return rsaEncrypt(data, DEFAULT_RSA_PUBLIC_KEY);
    }

    /**
     * RSA 公钥加密（来自 basic 模块）
     *
     * @param data      原始数据
     * @param publicKey 公钥
     * @return Base64 编码的加密结果
     */
    public static String rsaEncrypt(String data, String publicKey) {
        if (StringUtils.isEmpty(data)) {
            return data;
        }
        try {
            return RSAUtils.encrypt(data, publicKey);
        } catch (Exception e) {
            log.error("RSA 加密失败", e);
            throw new RuntimeException("RSA 加密失败: " + e.getMessage(), e);
        }
    }

    /**
     * RSA 私钥解密（使用默认私钥）
     *
     * @param encryptedData Base64 编码的加密数据
     * @return 解密后的原始数据
     */
    public static String rsaDecrypt(String encryptedData) {
        return rsaDecrypt(encryptedData, DEFAULT_RSA_PRIVATE_KEY);
    }

    /**
     * RSA 私钥解密（来自 basic 模块）
     *
     * @param encryptedData Base64 编码的加密数据
     * @param privateKey    私钥
     * @return 解密后的原始数据
     */
    public static String rsaDecrypt(String encryptedData, String privateKey) {
        if (StringUtils.isEmpty(encryptedData)) {
            return encryptedData;
        }
        try {
            return RSAUtils.decrypt(encryptedData, privateKey);
        } catch (Exception e) {
            log.error("RSA 解密失败", e);
            throw new RuntimeException("RSA 解密失败: " + e.getMessage(), e);
        }
    }

    /**
     * RSA 签名（来自 basic 模块）
     *
     * @param data       原始数据
     * @param privateKey 私钥
     * @return Base64 编码的签名
     */
    public static String rsaSign(String data, String privateKey) {
        try {
            return RSAUtils.sign(data, privateKey);
        } catch (Exception e) {
            log.error("RSA 签名失败", e);
            throw new RuntimeException("RSA 签名失败: " + e.getMessage(), e);
        }
    }

    /**
     * RSA 验签（来自 basic 模块）
     *
     * @param data      原始数据
     * @param signature 签名
     * @param publicKey 公钥
     * @return 验证结果
     */
    public static boolean rsaVerify(String data, String signature, String publicKey) {
        try {
            return RSAUtils.verify(data, signature, publicKey);
        } catch (Exception e) {
            log.error("RSA 验签失败", e);
            return false;
        }
    }

    // ==================== 国密 SM4 加密 ====================

    /**
     * SM4 加密（使用默认密钥）
     *
     * @param data 原始数据
     * @return Base64 编码的加密结果
     */
    public static String sm4Encrypt(String data) {
        return sm4Encrypt(data, DEFAULT_SM4_KEY);
    }

    /**
     * SM4 加密（自定义密钥）
     *
     * @param data 原始数据
     * @param key  SM4 密钥（16位）
     * @return Base64 编码的加密结果
     */
    public static String sm4Encrypt(String data, String key) {
        if (StringUtils.isEmpty(data)) {
            return data;
        }
        try {
            SM4 sm4 = new SM4(key.getBytes(StandardCharsets.UTF_8));
            byte[] encrypted = sm4.encrypt(data);
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("SM4 加密失败", e);
            throw new RuntimeException("SM4 加密失败: " + e.getMessage(), e);
        }
    }

    /**
     * SM4 解密（使用默认密钥）
     *
     * @param encryptedData Base64 编码的加密数据
     * @return 解密后的原始数据
     */
    public static String sm4Decrypt(String encryptedData) {
        return sm4Decrypt(encryptedData, DEFAULT_SM4_KEY);
    }

    /**
     * SM4 解密（自定义密钥）
     *
     * @param encryptedData Base64 编码的加密数据
     * @param key           SM4 密钥（16位）
     * @return 解密后的原始数据
     */
    public static String sm4Decrypt(String encryptedData, String key) {
        if (StringUtils.isEmpty(encryptedData)) {
            return encryptedData;
        }
        try {
            SM4 sm4 = new SM4(key.getBytes(StandardCharsets.UTF_8));
            byte[] encrypted = Base64.getDecoder().decode(encryptedData);
            byte[] decrypted = sm4.decrypt(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("SM4 解密失败", e);
            throw new RuntimeException("SM4 解密失败: " + e.getMessage(), e);
        }
    }

    // ==================== Base64 编码 ====================

    /**
     * Base64 编码
     *
     * @param data 原始数据
     * @return Base64 编码结果
     */
    public static String base64Encode(String data) {
        if (StringUtils.isEmpty(data)) {
            return data;
        }
        return Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Base64 解码
     *
     * @param encodedData Base64 编码的数据
     * @return 解码后的原始数据
     */
    public static String base64Decode(String encodedData) {
        if (StringUtils.isEmpty(encodedData)) {
            return encodedData;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(encodedData);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Base64 解码失败", e);
            throw new RuntimeException("Base64 解码失败: " + e.getMessage(), e);
        }
    }

    // ==================== 通用加解密接口 ====================

    /**
     * 根据算法类型加密
     *
     * @param data      原始数据
     * @param algorithm 算法类型
     * @return 加密结果
     */
    public static String encrypt(String data, AlgorithmType algorithm) {
        if (StringUtils.isEmpty(data)) {
            return data;
        }

        switch (algorithm) {
            case MD5:
                return md5(data);
            case AES:
                return aesEncrypt(data);
            case RSA:
                return rsaEncrypt(data);
            case SM4:
                return sm4Encrypt(data);
            case BASE64:
                return base64Encode(data);
            default:
                throw new IllegalArgumentException("不支持的加密算法: " + algorithm);
        }
    }

    /**
     * 根据算法类型解密
     *
     * @param encryptedData 加密数据
     * @param algorithm     算法类型
     * @return 解密结果
     */
    public static String decrypt(String encryptedData, AlgorithmType algorithm) {
        if (StringUtils.isEmpty(encryptedData)) {
            return encryptedData;
        }

        switch (algorithm) {
            case AES:
                return aesDecrypt(encryptedData);
            case RSA:
                return rsaDecrypt(encryptedData);
            case SM4:
                return sm4Decrypt(encryptedData);
            case BASE64:
                return base64Decode(encryptedData);
            case MD5:
                throw new UnsupportedOperationException("MD5 is a one-way hash, cannot decrypt");
            default:
                throw new IllegalArgumentException("不支持的解密算法: " + algorithm);
        }
    }

    // ==================== 兼容旧版本 API（别名方法）====================

    /**
     * 通过 Base64 加密（别名方法）
     *
     * @param data 原始数据
     * @return Base64 编码结果
     */
    public static String encryptByBase64(String data) {
        return base64Encode(data);
    }

    /**
     * 通过 Base64 解密（别名方法）
     *
     * @param encodedData Base64 编码的数据
     * @return 解码后的原始数据
     */
    public static String decryptByBase64(String encodedData) {
        return base64Decode(encodedData);
    }

    /**
     * 通过 RSA 加密（别名方法）
     *
     * @param data      原始数据
     * @param publicKey 公钥
     * @return Base64 编码的加密结果
     */
    public static String encryptByRsa(String data, String publicKey) {
        return rsaEncrypt(data, publicKey);
    }

    /**
     * 通过 RSA 解密（别名方法）
     *
     * @param encryptedData Base64 编码的加密数据
     * @param privateKey    私钥
     * @return 解密后的原始数据
     */
    public static String decryptByRsa(String encryptedData, String privateKey) {
        return rsaDecrypt(encryptedData, privateKey);
    }

    /**
     * 通过 AES 加密（别名方法）
     *
     * @param data 原始数据
     * @param key  AES 密钥
     * @return Base64 编码的加密结果
     */
    public static String encryptByAes(String data, String key) {
        return aesEncrypt(data, key);
    }

    /**
     * 通过 AES 解密（别名方法）
     *
     * @param encryptedData Base64 编码的加密数据
     * @param key           AES 密钥
     * @return 解密后的原始数据
     */
    public static String decryptByAes(String encryptedData, String key) {
        return aesDecrypt(encryptedData, key);
    }

    /**
     * 通过 SM4 加密（别名方法）
     *
     * @param data 原始数据
     * @param key  SM4 密钥
     * @return Base64 编码的加密结果
     */
    public static String encryptBySm4(String data, String key) {
        return sm4Encrypt(data, key);
    }

    /**
     * 通过 SM4 解密（别名方法）
     *
     * @param encryptedData Base64 编码的加密数据
     * @param key           SM4 密钥
     * @return 解密后的原始数据
     */
    public static String decryptBySm4(String encryptedData, String key) {
        return sm4Decrypt(encryptedData, key);
    }

    // ==================== 工具方法 ====================

    /**
     * 判断是否为有效的 Base64 字符串
     *
     * @param str 字符串
     * @return 是否为有效 Base64
     */
    public static boolean isValidBase64(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        try {
            Base64.getDecoder().decode(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 获取密钥强度
     *
     * @param key 密钥
     * @return 密钥强度（位数）
     */
    public static int getKeyStrength(String key) {
        if (StringUtils.isEmpty(key)) {
            return 0;
        }
        return key.getBytes(StandardCharsets.UTF_8).length * 8;
    }

    /**
     * 验证密钥长度
     *
     * @param key            密钥
     * @param requiredLength 要求的长度
     * @return 是否符合要求
     */
    public static boolean isValidKeyLength(String key, int requiredLength) {
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        return key.getBytes(StandardCharsets.UTF_8).length == requiredLength;
    }
}
