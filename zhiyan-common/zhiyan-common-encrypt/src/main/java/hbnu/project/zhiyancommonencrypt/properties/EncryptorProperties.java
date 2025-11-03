package hbnu.project.zhiyancommonencrypt.properties;

import hbnu.project.zhiyancommonencrypt.enumd.AlgorithmType;
import hbnu.project.zhiyancommonencrypt.enumd.EncodeType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 加解密配置属性类
 * 统一管理加密相关的配置
 *
 * @author ErgouTree
 * @version 2.0.0
 */
@Data
@ConfigurationProperties(prefix = "zhiyan.encrypt")
public class EncryptorProperties {

    /**
     * 是否启用加密功能
     */
    private Boolean enabled = false;

    /**
     * 默认加密算法
     */
    private AlgorithmType algorithm = AlgorithmType.AES;

    /**
     * 默认编码方式
     */
    private EncodeType encode = EncodeType.BASE64;

    /**
     * AES 密钥（16/24/32位）
     */
    private String aesKey = "zhiyan1234567890";

    /**
     * SM4 密钥（16位）
     */
    private String sm4Key = "zhiyan1234567890";

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
     * 数据库字段加密配置
     */
    private DatabaseConfig database = new DatabaseConfig();

    /**
     * API 加密配置
     */
    private ApiConfig api = new ApiConfig();

    /**
     * 数据库字段加密配置
     */
    @Data
    public static class DatabaseConfig {
        /**
         * 是否启用数据库字段加密
         */
        private Boolean enabled = false;

        /**
         * 需要加密的字段列表
         */
        private String[] fields = new String[]{"phone", "idCard", "bankCard", "email"};

        /**
         * 排除的表名
         */
        private String[] excludeTables = new String[]{"sys_log", "sys_config"};
    }

    /**
     * API 加密配置
     */
    @Data
    public static class ApiConfig {
        /**
         * 是否启用 API 加密
         */
        private Boolean enabled = false;

        /**
         * 需要加密的路径
         */
        private String[] encryptPaths = new String[]{};

        /**
         * 排除的路径
         */
        private String[] excludePaths = new String[]{"/actuator/**", "/error"};

        /**
         * 加密标识请求头名称
         */
        private String encryptHeader = "X-Encrypt";

        /**
         * 算法标识请求头名称
         */
        private String algorithmHeader = "X-Algorithm";
    }
}
