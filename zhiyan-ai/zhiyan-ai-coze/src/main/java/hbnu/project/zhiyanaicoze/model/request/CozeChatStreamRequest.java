package hbnu.project.zhiyanaicoze.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Coze流式对话请求（用于解决URL参数过长导致431错误）
 * 
 * @author AI Assistant
 * @date 2025-11-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Coze流式对话请求")
public class CozeChatStreamRequest {
    
    @Schema(description = "用户问题", required = true, example = "你好，请介绍一下自己")
    @NotBlank(message = "用户问题不能为空")
    private String query;
    
    @Schema(description = "对话ID（可选，用于维持会话）", example = "1234567890")
    @JsonProperty("conversationId")
    private String conversationId;
    
    @Schema(description = "自定义变量（可选）")
    @JsonProperty("customVariables")
    private Map<String, Object> customVariables;
}


