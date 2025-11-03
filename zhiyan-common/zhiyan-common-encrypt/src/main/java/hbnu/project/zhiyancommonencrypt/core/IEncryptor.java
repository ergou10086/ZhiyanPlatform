package hbnu.project.zhiyancommonencrypt.core;

import hbnu.project.zhiyancommonencrypt.enumd.AlgorithmType;
import hbnu.project.zhiyancommonencrypt.enumd.EncodeType;

/**
 * 加密器接口
 * 定义统一的加密解密规范
 *
 * @author ErgouTree
 * @version 2.0.0
 */
public interface IEncryptor {

    /**
     * 获取当前加密器的算法类型
     *
     * @return 算法类型
     */
    AlgorithmType algorithm();

    /**
     * 加密数据
     *
     * @param value      待加密的字符串
     * @param encodeType 加密后的编码格式（BASE64/HEX）
     * @return 加密后的字符串
     */
    String encrypt(String value, EncodeType encodeType);

    /**
     * 解密数据
     *
     * @param value 待解密的字符串
     * @return 解密后的字符串
     */
    String decrypt(String value);

    /**
     * 使用默认编码加密
     *
     * @param value 待加密的字符串
     * @return 加密后的字符串
     */
    default String encrypt(String value) {
        return encrypt(value, EncodeType.BASE64);
    }
}
