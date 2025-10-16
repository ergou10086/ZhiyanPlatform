package hbnu.project.zhiyancommonbasic.utils.encrypt;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * RSA加密解密工具类
 * 支持公钥加密/私钥解密、私钥签名/公钥验签、分段加解密
 *
 * @author ErgouTree
 */
public class RSAUtils {

    private static final String ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final int KEY_SIZE = 2048;
    private static final String PUBLIC_KEY = "publicKey";
    private static final String PRIVATE_KEY = "privateKey";

    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 245;

    /**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 256;

    /**
     * 生成RSA密钥对
     *
     * @return 包含公钥和私钥的Map
     * @throws NoSuchAlgorithmException 算法不存在异常
     */
    public static Map<String, String> generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
        keyPairGenerator.initialize(KEY_SIZE);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        Map<String, String> keyMap = new HashMap<>();
        keyMap.put(PUBLIC_KEY, Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        keyMap.put(PRIVATE_KEY, Base64.getEncoder().encodeToString(privateKey.getEncoded()));

        return keyMap;
    }

    /**
     * 使用公钥加密（支持分段加密）
     *
     * @param data      待加密的数据
     * @param publicKey Base64编码的公钥字符串
     * @return Base64编码的加密结果
     * @throws Exception 加密异常
     */
    public static String encrypt(String data, String publicKey) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(publicKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        PublicKey pubKey = keyFactory.generatePublic(spec);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);

        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedBytes = doFinal(cipher, dataBytes, MAX_ENCRYPT_BLOCK);

        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * 使用私钥解密（支持分段解密）
     *
     * @param encryptedData Base64编码的加密数据
     * @param privateKey    Base64编码的私钥字符串
     * @return 解密后的原始字符串
     * @throws Exception 解密异常
     */
    public static String decrypt(String encryptedData, String privateKey) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(privateKey);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        PrivateKey privKey = keyFactory.generatePrivate(spec);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privKey);

        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedBytes = doFinal(cipher, encryptedBytes, MAX_DECRYPT_BLOCK);

        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    /**
     * 使用私钥签名
     *
     * @param data       待签名的数据
     * @param privateKey Base64编码的私钥字符串
     * @return Base64编码的签名结果
     * @throws Exception 签名异常
     */
    public static String sign(String data, String privateKey) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(privateKey);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        PrivateKey privKey = keyFactory.generatePrivate(spec);

        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(privKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(signature.sign());
    }

    /**
     * 使用公钥验证签名
     *
     * @param data      原始数据
     * @param sign      Base64编码的签名
     * @param publicKey Base64编码的公钥字符串
     * @return 验证结果
     * @throws Exception 验签异常
     */
    public static boolean verify(String data, String sign, String publicKey) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(publicKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        PublicKey pubKey = keyFactory.generatePublic(spec);

        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(pubKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] signBytes = Base64.getDecoder().decode(sign);

        return signature.verify(signBytes);
    }

    /**
     * 分段加解密处理
     *
     * @param cipher    加密或解密的Cipher对象
     * @param data      待处理的数据
     * @param maxBlock  每次处理的最大块大小
     * @return 处理后的数据
     * @throws Exception 处理异常
     */
    private static byte[] doFinal(Cipher cipher, byte[] data, int maxBlock) throws Exception {
        int dataLen = data.length;
        if (dataLen <= maxBlock) {
            return cipher.doFinal(data);
        }

        int offset = 0;
        byte[] cache;
        int i = 0;
        // 创建输出缓冲区
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();

        try {
            while (dataLen - offset > 0) {
                if (dataLen - offset > maxBlock) {
                    cache = cipher.doFinal(data, offset, maxBlock);
                } else {
                    cache = cipher.doFinal(data, offset, dataLen - offset);
                }
                out.write(cache, 0, cache.length);
                i++;
                offset = i * maxBlock;
            }
            return out.toByteArray();
        } finally {
            out.close();
        }
    }

    /**
     * 获取公钥字符串
     *
     * @param keyMap 密钥对Map
     * @return 公钥字符串
     */
    public static String getPublicKey(Map<String, String> keyMap) {
        return keyMap.get(PUBLIC_KEY);
    }

    /**
     * 获取私钥字符串
     *
     * @param keyMap 密钥对Map
     * @return 私钥字符串
     */
    public static String getPrivateKey(Map<String, String> keyMap) {
        return keyMap.get(PRIVATE_KEY);
    }

    /**
     * 使用公钥加密字节数组
     *
     * @param data      待加密的字节数组
     * @param publicKey Base64编码的公钥字符串
     * @return Base64编码的加密结果
     * @throws Exception 加密异常
     */
    public static String encryptBytes(byte[] data, String publicKey) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(publicKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        PublicKey pubKey = keyFactory.generatePublic(spec);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);

        byte[] encryptedBytes = doFinal(cipher, data, MAX_ENCRYPT_BLOCK);
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * 使用私钥解密为字节数组
     *
     * @param encryptedData Base64编码的加密数据
     * @param privateKey    Base64编码的私钥字符串
     * @return 解密后的字节数组
     * @throws Exception 解密异常
     */
    public static byte[] decryptToBytes(String encryptedData, String privateKey) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(privateKey);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        PrivateKey privKey = keyFactory.generatePrivate(spec);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privKey);

        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
        return doFinal(cipher, encryptedBytes, MAX_DECRYPT_BLOCK);
    }
}