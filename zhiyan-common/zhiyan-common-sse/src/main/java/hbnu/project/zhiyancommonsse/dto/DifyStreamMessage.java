package hbnu.project.zhiyancommonsse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * Dify 流式消息 DTO
 * 用于封装 AI 对话的流式响应数据
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DifyStreamMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 对话 ID
     */
    @JsonProperty("conversation_id")
    private String conversationId;

    /**
     * 消息 ID
     */
    @JsonProperty("message_id")
    private String messageId;

    /**
     * 事件类型：message（消息）、chunk（文本块）、answer（完整答案）、error（错误）、done（完成）、workflow_started（工作流开始）、workflow_finished（工作流完成）、node_started（节点开始）、node_finished（节点完成）
     */
    private String event;

    /**
     * 消息数据（可能是文本内容或 JSON 对象）
     */
    private String data;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 任务 ID（用于追踪工作流）
     */
    @JsonProperty("task_id")
    private String taskId;

    /**
     * 工作流运行 ID
     */
    @JsonProperty("workflow_run_id")
    private String workflowRunId;

    /**
     * 节点执行 ID
     */
    @JsonProperty("node_execution_id")
    private String nodeExecutionId;

    /**
     * 节点 ID
     */
    @JsonProperty("node_id")
    private String nodeId;

    /**
     * 节点类型
     */
    @JsonProperty("node_type")
    private String nodeType;

    /**
     * 节点标题
     */
    @JsonProperty("node_title")
    private String nodeTitle;

    /**
     * 元数据（额外信息）
     */
    private Map<String, Object> metadata;

    /**
     * 文件列表（如果有上传文件）
     */
    private Map<String, Object> files;

    /**
     * 错误信息
     */
    @JsonProperty("error_message")
    private String errorMessage;

    /**
     * 错误代码
     */
    @JsonProperty("error_code")
    private String errorCode;

    /**
     * 创建时间
     */
    @JsonProperty("created_at")
    private Long createdAt;

}
