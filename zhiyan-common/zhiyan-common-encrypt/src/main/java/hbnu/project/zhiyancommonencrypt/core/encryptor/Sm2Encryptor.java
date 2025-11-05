package hbnu.project.zhiyancommonencrypt.core.encryptor;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import hbnu.project.zhiyancommonencrypt.core.EncryptContext;
import hbnu.project.zhiyancommonencrypt.enumd.AlgorithmType;
import hbnu.project.zhiyancommonencrypt.enumd.EncodeType;
import lombok.extern.slf4j.Slf4j;

import java.util.Base64;

/**
 * 国密 SM2 非对称加密算法实现
 * 使用 Hutool 加密工具
 *
 * @author ErgouTree
 * @version 2.0.0
 */
@Slf4j
public class Sm2Encryptor extends AbstractEncryptor {

    private final SM2 sm2;

    public Sm2Encryptor(EncryptContext context) {
        super(context);
        this.sm2 = new SM2(context.getSm2PrivateKey(), context.getSm2PublicKey());
    }

    @Override
    protected void validateContext() {
        super.validateContext();
        if (isEmpty(context.getSm2PublicKey())) {
            throw new IllegalArgumentException("SM2 公钥不能为空");
        }
        if (isEmpty(context.getSm2PrivateKey())) {
            throw new IllegalArgumentException("SM2 私钥不能为空");
        }
    }

    @Override
    public AlgorithmType algorithm() {
        return AlgorithmType.SM2;
    }

    @Override
    public String encrypt(String value, EncodeType encodeType) {
        if (isEmpty(value)) {
            return value;
        }

        try {
            byte[] encrypted = sm2.encrypt(value.getBytes(), KeyType.PublicKey);

            // 根据编码类型返回
            if (encodeType == EncodeType.HEX) {
                return HexUtil.encodeHexStr(encrypted);
            } else {
                return Base64.getEncoder().encodeToString(encrypted);
            }
        } catch (Exception e) {
            log.error("SM2 加密失败: {}", e.getMessage(), e);
            throw new RuntimeException("SM2 加密失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解密
     *
     * @param value      待加密字符串
     */
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

            byte[] decrypted = sm2.decrypt(encrypted, KeyType.PrivateKey);
            return new String(decrypted);
        } catch (Exception e) {
            log.error("SM2 解密失败: {}", e.getMessage(), e);
            throw new RuntimeException("SM2 解密失败: " + e.getMessage(), e);
        }
    }
}
