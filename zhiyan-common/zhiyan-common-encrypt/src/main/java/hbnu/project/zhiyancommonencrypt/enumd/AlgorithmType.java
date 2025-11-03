package hbnu.project.zhiyancommonencrypt.enumd;

import hbnu.project.zhiyancommonencrypt.core.encryptor.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 加密算法类型枚举
 * 定义系统支持的所有加密算法
 *
 * @author ErgouTree
 * @version 2.0.0
 */
@Getter
@AllArgsConstructor
public enum AlgorithmType {

    /**
     * 默认算法（使用配置文件中指定的算法）
     */
    DEFAULT(null, "默认算法"),

    /**
     * Base64 编码（非加密算法）
     */
    BASE64(Base64Encryptor.class, "Base64编码"),

    /**
     * AES 对称加密算法
     */
    AES(AesEncryptor.class, "AES对称加密"),

    /**
     * RSA 非对称加密算法
     */
    RSA(RsaEncryptor.class, "RSA非对称加密"),

    /**
     * 国密 SM2 非对称加密算法
     */
    SM2(Sm2Encryptor.class, "国密SM2非对称加密"),

    /**
     * 国密 SM4 对称加密算法
     */
    SM4(Sm4Encryptor.class, "国密SM4对称加密"),

    /**
     * MD5 消息摘要算法（不可逆）
     */
    MD5(null, "MD5消息摘要");

    /**
     * 加密器实现类
     */
    private final Class<? extends AbstractEncryptor> clazz;

    /**
     * 算法描述
     */
    private final String description;
}
