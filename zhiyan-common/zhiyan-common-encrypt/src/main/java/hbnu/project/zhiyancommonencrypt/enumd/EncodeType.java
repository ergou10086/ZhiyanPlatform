package hbnu.project.zhiyancommonencrypt.enumd;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 编码类型枚举
 * 定义加密后数据的编码方式
 *
 * @author ErgouTree
 * @version 2.0.0
 */
@Getter
@AllArgsConstructor
public enum EncodeType {

    /**
     * 默认编码（使用配置文件中指定的编码）
     */
    DEFAULT("默认编码"),

    /**
     * Base64 编码（推荐，可读性好）
     */
    BASE64("Base64编码"),

    /**
     * 十六进制编码（HEX）
     */
    HEX("十六进制编码");

    /**
     * 编码描述
     */
    private final String description;
}
