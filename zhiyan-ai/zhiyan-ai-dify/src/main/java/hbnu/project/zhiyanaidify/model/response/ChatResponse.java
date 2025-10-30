package hbnu.project.zhiyanaidify.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Dify Chat 响应 DTO
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    /**
     * 消息 ID
     */
    @JsonProperty("message_id")
    private String messageId;

    /**
     * 对话 ID
     */
    @JsonProperty("conversation_id")
    private String conversationId;

    /**
     * 响应模式
     */
    private String mode;

    /**
     * AI 回复内容
     */
    private String answer;

    /**
     * 元数据
     */
    private Map<String, Object> metadata;

    /**
     * 创建时间
     */
    @JsonProperty("created_at")
    private Long createdAt;
}