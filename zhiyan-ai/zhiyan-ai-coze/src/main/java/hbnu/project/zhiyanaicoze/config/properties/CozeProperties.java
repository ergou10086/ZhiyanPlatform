package hbnu.project.zhiyanaicoze.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Coze 配置属性
 *
 * @author ErgouTree
 */
@Data
@Component
@ConfigurationProperties(prefix = "coze")
public class CozeProperties {

    /**
     * Coze API URL
     */
    private String apiUrl;

    /**
     * Personal Access Token
     */
    private String token;

    /**
     * 智能体ID（Bot ID）
     */
    private String botId;

    /**
     * 智能体链接
     */
    private String botUrl;

    /**
     * 超时时间（毫秒）
     */
    private Integer timeout = 60000;

    /**
     * 最大 token 数量
     */
    private Integer maxTokens = 4096;

    /**
     * 是否启用流式传输
     */
    private Boolean streamEnabled = true;

    /**
     * 连接超时时间（秒）
     */
    private Integer connectTimeout = 30;

    /**
     * 读取超时时间（秒）
     */
    private Integer readTimeout = 60;
}

