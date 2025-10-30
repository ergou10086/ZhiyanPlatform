package hbnu.project.zhiyanaicoze.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * Coze 流式消息 DTO
 * 根据官方 API 文档完善
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CozeStreamMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 事件类型：
     * - conversation.chat.created: 对话创建
     * - conversation.message.delta: 消息增量（流式文本块）
     * - conversation.message.completed: 消息完成
     * - conversation.chat.completed: 对话完成
     * - conversation.chat.failed: 对话失败
     * - conversation.chat.requires_action: 需要用户操作
     * - done: 结束
     * - error: 错误
     */
    private String event;

    /**
     * 对话 ID
     */
    @JsonProperty("conversation_id")
    private String conversationId;

    /**
     * 聊天 ID
     */
    @JsonProperty("chat_id")
    private String chatId;

    /**
     * 消息 ID
     */
    @JsonProperty("id")
    private String id;

    /**
     * 消息内容（增量文本）
     */
    private String content;

    /**
     * 消息角色：user, assistant, system
     */
    private String role;

    /**
     * 消息类型：answer, function_call, tool_output, follow_up
     */
    private String type;

    /**
     * 智能体 ID
     */
    @JsonProperty("bot_id")
    private String botId;

    /**
     * 对话状态：created, in_progress, completed, failed, requires_action
     */
    private String status;

    /**
     * 创建时间（Unix 时间戳）
     */
    @JsonProperty("created_at")
    private Long createdAt;

    /**
     * 完成时间（Unix 时间戳）
     */
    @JsonProperty("completed_at")
    private Long completedAt;

    /**
     * 时间戳（本地生成）
     */
    private Long timestamp;

    /**
     * Token 使用情况
     */
    private Usage usage;

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
     * 原始 JSON 数据（用于调试）
     */
    private String rawData;

    /**
     * 额外数据
     */
    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    /**
     * Token 使用统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        @JsonProperty("input_tokens")
        private Integer inputTokens;

        @JsonProperty("output_tokens")
        private Integer outputTokens;

        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }
}