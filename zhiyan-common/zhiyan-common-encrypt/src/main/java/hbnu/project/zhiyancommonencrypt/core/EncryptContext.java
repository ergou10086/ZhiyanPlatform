package hbnu.project.zhiyancommonencrypt.core;

import hbnu.project.zhiyancommonencrypt.enumd.AlgorithmType;
import hbnu.project.zhiyancommonencrypt.enumd.EncodeType;
import hbnu.project.zhiyancommonencrypt.properties.EncryptorProperties;
import lombok.Data;

import java.util.Objects;

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

    /**
     * 设置密钥（通用方法，用于对称加密算法）
     * 根据当前算法类型设置对应的密钥
     *
     * @param password 密钥
     */
    public void setPassword(String password) {
        if (this.algorithm == AlgorithmType.AES) {
            this.aesKey = password;
        } else if (this.algorithm == AlgorithmType.SM4) {
            this.sm4Key = password;
        }
    }

    /**
     * 设置公钥（通用方法，用于非对称加密算法）
     * 根据当前算法类型设置对应的公钥
     *
     * @param publicKey 公钥
     */
    public void setPublicKey(String publicKey) {
        if (this.algorithm == AlgorithmType.RSA) {
            this.rsaPublicKey = publicKey;
        } else if (this.algorithm == AlgorithmType.SM2) {
            this.sm2PublicKey = publicKey;
        }
    }

    /**
     * 设置私钥（通用方法，用于非对称加密算法）
     * 根据当前算法类型设置对应的私钥
     *
     * @param privateKey 私钥
     */
    public void setPrivateKey(String privateKey) {
        if (this.algorithm == AlgorithmType.RSA) {
            this.rsaPrivateKey = privateKey;
        } else if (this.algorithm == AlgorithmType.SM2) {
            this.sm2PrivateKey = privateKey;
        }
    }

    /**
     * 重写 equals 方法
     * 用于比较两个加密上下文是否相同
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EncryptContext that = (EncryptContext) o;
        return algorithm == that.algorithm &&
                encode == that.encode &&
                Objects.equals(aesKey, that.aesKey) &&
                Objects.equals(sm4Key, that.sm4Key) &&
                Objects.equals(rsaPublicKey, that.rsaPublicKey) &&
                Objects.equals(rsaPrivateKey, that.rsaPrivateKey) &&
                Objects.equals(sm2PublicKey, that.sm2PublicKey) &&
                Objects.equals(sm2PrivateKey, that.sm2PrivateKey);
    }

    /**
     * 重写 hashCode 方法
     * 用于加密器缓存的 key
     */
    @Override
    public int hashCode() {
        return Objects.hash(algorithm, encode, aesKey, sm4Key, 
                rsaPublicKey, rsaPrivateKey, sm2PublicKey, sm2PrivateKey);
    }
}
