package hbnu.project.zhiyanai.config.properties;

import hbnu.project.zhiyanai.config.WorkflowConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * N8N 工作流配置属性
 *
 * @author ErguTree
 */
@Data
@Component
@ConfigurationProperties(prefix = "n8n")
public class N8nProperties {
    /**
     * N8N 服务基础URL
     */
    private String baseUrl;

    /**
     * API Key（用于认证）
     */
    private String apiKey;

    /**
     * 请求超时时间（毫秒）
     */
    private Integer timeout = 30000;

    /**
     * 工作流配置
     */
    private Map<String, WorkflowConfig> workflows = new HashMap<>();
}
