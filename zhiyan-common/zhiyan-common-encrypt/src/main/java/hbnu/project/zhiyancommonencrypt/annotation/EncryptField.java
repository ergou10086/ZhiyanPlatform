package hbnu.project.zhiyancommonencrypt.annotation;

import hbnu.project.zhiyancommonencrypt.enumd.AlgorithmType;
import hbnu.project.zhiyancommonencrypt.enumd.EncodeType;

import java.lang.annotation.*;

/**
 * 数据库字段加密注解
 * 标注在实体类的字段上，自动进行加解密
 *
 * @author ErgouTree
 * @version 2.0.0
 */
@Documented
@Inherited
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EncryptField {

    /**
     * 加密算法类型
     * 默认使用配置文件中指定的算法
     */
    AlgorithmType algorithm() default AlgorithmType.DEFAULT;

    /**
     * AES/SM4 密钥
     * 如果不指定，则使用配置文件中的默认密钥
     */
    String password() default "";

    /**
     * RSA/SM2 公钥
     * 如果不指定，则使用配置文件中的默认公钥
     */
    String publicKey() default "";

    /**
     * RSA/SM2 私钥
     * 如果不指定，则使用配置文件中的默认私钥
     */
    String privateKey() default "";

    /**
     * 编码方式（BASE64/HEX）
     * 默认使用配置文件中指定的编码方式
     */
    EncodeType encode() default EncodeType.DEFAULT;
}
