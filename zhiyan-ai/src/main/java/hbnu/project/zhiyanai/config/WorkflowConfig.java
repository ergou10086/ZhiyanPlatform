package hbnu.project.zhiyanai.config;

import lombok.Data;

/**
 * n8n工作流配置项
 * @author ErgouTree
 */
@Data
public class WorkflowConfig {
    /**
     * Webhook URL
     */
    private String webhookUrl;

    /**
     * 工作流ID
     */
    private String workflowId;

    /**
     * 是否启用
     */
    private Boolean enabled = true;
}
