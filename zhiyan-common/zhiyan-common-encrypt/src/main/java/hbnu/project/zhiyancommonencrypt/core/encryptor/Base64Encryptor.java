package hbnu.project.zhiyancommonencrypt.core.encryptor;

import cn.hutool.core.util.HexUtil;
import hbnu.project.zhiyancommonencrypt.core.EncryptContext;
import hbnu.project.zhiyancommonencrypt.enumd.AlgorithmType;
import hbnu.project.zhiyancommonencrypt.enumd.EncodeType;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Base64 编码实现（非加密算法）
 * 仅用于数据编码，不提供安全性
 *
 * @author ErgouTree
 * @version 2.0.0
 */
public class Base64Encryptor extends AbstractEncryptor {

    public Base64Encryptor(EncryptContext context) {
        super(context);
    }

    /**
     * 获得当前算法
     */
    @Override
    public AlgorithmType algorithm() {
        return AlgorithmType.BASE64;
    }

    /**
     * 加密
     *
     * @param value      待加密字符串
     * @param encodeType 加密后的编码格式
     */
    @Override
    public String encrypt(String value, EncodeType encodeType) {
        if (isEmpty(value)) {
            return value;
        }

        try {
            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);

            // 根据编码类型返回
            if (encodeType == EncodeType.HEX) {
                return HexUtil.encodeHexStr(bytes);
            } else {
                return Base64.getEncoder().encodeToString(bytes);
            }
        } catch (Exception e) {
            log.error("Base64 编码失败: {}", e.getMessage(), e);
            throw new RuntimeException("Base64 编码失败: " + e.getMessage(), e);
        }
    }

    
    @Override
    public String decrypt(String value) {
        if (isEmpty(value)) {
            return value;
        }

        try {
            // 尝试 Base64 解码
            byte[] decoded;
            try {
                decoded = Base64.getDecoder().decode(value);
            } catch (IllegalArgumentException e) {
                // 如果 Base64 解码失败，尝试 HEX 解码
                decoded = HexUtil.decodeHex(value);
            }

            return new String(decoded, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Base64 解码失败: {}", e.getMessage(), e);
            throw new RuntimeException("Base64 解码失败: " + e.getMessage(), e);
        }
    }
}
