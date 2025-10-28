package hbnu.project.zhiyanai.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Dify 配置属性
 *
 * @author ErgouTree
 */
@Data
@Component
@ConfigurationProperties(prefix = "dify")
public class DifyProperties {

    /**
     * Dify API URL
     */
    private String apiUrl;

    /**
     * Dify Chat URL
     */
    private String chatUrl;

    /**
     * API Key
     */
    private String apiKey;

    /**
     * 超时时间（毫秒）
     */
    private Integer timeout = 60000;

    /**
     * 最大 token 数量
     */
    private Integer maxTokens = 4096;

    /**
     * 温度参数（0-1）
     */
    private Double temperature = 0.7;

    /**
     * 是否启用流式传输
     */
    private Boolean streamEnabled = true;
}
