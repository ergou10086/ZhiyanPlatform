package hbnu.project.zhiyanaicoze.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Coze 对话查询响应
 *
 * @author ErgouTree
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CozeChatResponse {

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
     * 智能体 ID
     */
    @JsonProperty("bot_id")
    private String botId;

    /**
     * 对话状态：created, in_progress, completed, failed, requires_action
     */
    private String status;

    /**
     * 创建时间
     */
    @JsonProperty("created_at")
    private Long createdAt;

    /**
     * 完成时间
     */
    @JsonProperty("completed_at")
    private Long completedAt;

    /**
     * Token 使用量
     */
    private Usage usage;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        /**
         * 输入 token 数量
         */
        @JsonProperty("input_tokens")
        private Integer inputTokens;

        /**
         * 输出 token 数量
         */
        @JsonProperty("output_tokens")
        private Integer outputTokens;

        /**
         * 总 token 数量
         */
        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }
}