package hbnu.project.zhiyanaidify.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 对话响应 DTO（返回给前端）
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIChatResponse {

    /**
     * 消息 ID
     */
    private String messageId;

    /**
     * 对话 ID
     */
    private String conversationId;

    /**
     * AI 回复内容
     */
    private String answer;

    /**
     * 响应时间
     */
    private LocalDateTime responseTime;

    /**
     * 处理状态
     */
    private String status;

    /**
     * 错误信息（如果有）
     */
    private String errorMessage;
}
