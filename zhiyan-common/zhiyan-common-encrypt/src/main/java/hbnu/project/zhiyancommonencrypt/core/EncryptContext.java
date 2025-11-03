package hbnu.project.zhiyancommonencrypt.core;

import hbnu.project.zhiyancommonencrypt.enumd.AlgorithmType;
import hbnu.project.zhiyancommonencrypt.enumd.EncodeType;
import hbnu.project.zhiyancommonencrypt.properties.EncryptorProperties;
import lombok.Data;

/**
 * 加密上下文
 * 用于在加密器之间传递必要的参数和配置信息
 *
 * @author ErgouTree
 * @version 2.0.0
 */
@Data
public class EncryptContext {

    /**
     * 默认加密算法
     */
    private AlgorithmType algorithm;

    /**
     * 编码方式（BASE64/HEX）
     */
    private EncodeType encode;

    /**
     * AES 密钥
     */
    private String aesKey;

    /**
     * SM4 密钥
     */
    private String sm4Key;

    /**
     * RSA 公钥
     */
    private String rsaPublicKey;

    /**
     * RSA 私钥
     */
    private String rsaPrivateKey;

    /**
     * SM2 公钥
     */
    private String sm2PublicKey;

    /**
     * SM2 私钥
     */
    private String sm2PrivateKey;

    /**
     * 从配置属性创建加密上下文
     *
     * @param properties 配置属性
     * @return 加密上下文
     */
    public static EncryptContext from(EncryptorProperties properties) {
        EncryptContext context = new EncryptContext();
        context.setAlgorithm(properties.getAlgorithm());
        context.setEncode(properties.getEncode());
        context.setAesKey(properties.getAesKey());
        context.setSm4Key(properties.getSm4Key());
        context.setRsaPublicKey(properties.getRsaPublicKey());
        context.setRsaPrivateKey(properties.getRsaPrivateKey());
        context.setSm2PublicKey(properties.getSm2PublicKey());
        context.setSm2PrivateKey(properties.getSm2PrivateKey());
        return context;
    }
}
