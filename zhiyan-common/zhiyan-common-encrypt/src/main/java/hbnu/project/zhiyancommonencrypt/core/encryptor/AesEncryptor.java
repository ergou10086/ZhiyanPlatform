package hbnu.project.zhiyancommonencrypt.core.encryptor;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import hbnu.project.zhiyancommonencrypt.core.EncryptContext;
import hbnu.project.zhiyancommonencrypt.enumd.AlgorithmType;
import hbnu.project.zhiyancommonencrypt.enumd.EncodeType;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES 对称加密算法实现
 * 使用 Hutool 加密工具和 basic 模块的工具类
 *
 * @author ErgouTree
 * @version 2.0.0
 */
@Slf4j
public class AesEncryptor extends AbstractEncryptor {

    private final AES aes;

    public AesEncryptor(EncryptContext context) {
        super(context);
        if (isEmpty(context.getAesKey())) {
            throw new IllegalArgumentException("AES 密钥不能为空");
        }
        this.aes = SecureUtil.aes(context.getAesKey().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public AlgorithmType algorithm() {
        return AlgorithmType.AES;
    }

    @Override
    public String encrypt(String value, EncodeType encodeType) {
        if (isEmpty(value)) {
            return value;
        }

        try {
            byte[] encrypted = aes.encrypt(value);

            // 根据编码类型返回
            if (encodeType == EncodeType.HEX) {
                return HexUtil.encodeHexStr(encrypted);
            } else {
                return Base64.getEncoder().encodeToString(encrypted);
            }
        } catch (Exception e) {
            log.error("AES 加密失败: {}", e.getMessage(), e);
            throw new RuntimeException("AES 加密失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String decrypt(String value) {
        if (isEmpty(value)) {
            return value;
        }

        try {
            // 尝试 Base64 解码
            byte[] encrypted;
            try {
                encrypted = Base64.getDecoder().decode(value);
            } catch (IllegalArgumentException e) {
                // 如果 Base64 解码失败，尝试 HEX 解码
                encrypted = HexUtil.decodeHex(value);
            }

            byte[] decrypted = aes.decrypt(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES 解密失败: {}", e.getMessage(), e);
            throw new RuntimeException("AES 解密失败: " + e.getMessage(), e);
        }
    }
}
