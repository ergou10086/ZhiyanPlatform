package hbnu.project.zhiyancommonencrypt.core.encryptor;

import cn.hutool.core.util.HexUtil;
import hbnu.project.zhiyancommonbasic.utils.encrypt.RSAUtils;
import hbnu.project.zhiyancommonencrypt.core.EncryptContext;
import hbnu.project.zhiyancommonencrypt.enumd.AlgorithmType;
import hbnu.project.zhiyancommonencrypt.enumd.EncodeType;

import java.util.Base64;

/**
 * RSA 非对称加密算法实现
 * 直接使用 basic 模块的 RSAUtils
 *
 * @author ErgouTree
 * @version 2.0.0
 */
public class RsaEncryptor extends AbstractEncryptor {

    public RsaEncryptor(EncryptContext context) {
        super(context);
    }

    @Override
    protected void validateContext() {
        super.validateContext();
        if (isEmpty(context.getRsaPublicKey())) {
            throw new IllegalArgumentException("RSA 公钥不能为空");
        }
        if (isEmpty(context.getRsaPrivateKey())) {
            throw new IllegalArgumentException("RSA 私钥不能为空");
        }
    }

    @Override
    public AlgorithmType algorithm() {
        return AlgorithmType.RSA;
    }

    @Override
    public String encrypt(String value, EncodeType encodeType) {
        if (isEmpty(value)) {
            return value;
        }

        try {
            // RSAUtils 已经返回 Base64 编码的结果
            String encrypted = RSAUtils.encrypt(value, context.getRsaPublicKey());

            // 如果需要 HEX 编码，进行转换
            if (encodeType == EncodeType.HEX) {
                byte[] bytes = Base64.getDecoder().decode(encrypted);
                return HexUtil.encodeHexStr(bytes);
            }

            return encrypted;
        } catch (Exception e) {
            log.error("RSA 加密失败: {}", e.getMessage(), e);
            throw new RuntimeException("RSA 加密失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String decrypt(String value) {
        if (isEmpty(value)) {
            return value;
        }

        try {
            // 检查是 HEX 还是 Base64 编码
            String base64Value = value;
            if (isHexEncoded(value)) {
                byte[] bytes = HexUtil.decodeHex(value);
                base64Value = Base64.getEncoder().encodeToString(bytes);
            }

            return RSAUtils.decrypt(base64Value, context.getRsaPrivateKey());
        } catch (Exception e) {
            log.error("RSA 解密失败: {}", e.getMessage(), e);
            throw new RuntimeException("RSA 解密失败: " + e.getMessage(), e);
        }
    }

    /**
     * 判断是否为 HEX 编码
     */
    private boolean isHexEncoded(String value) {
        return value.matches("^[0-9a-fA-F]+$");
    }
}
