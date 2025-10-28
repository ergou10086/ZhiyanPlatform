package hbnu.project.zhiyanai.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Dify Chat 请求 DTO
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    /**
     * 用户输入的问题
     */
    @NotBlank(message = "问题不能为空")
    private String query;

    /**
     * 对话 ID（用于维持上下文）
     */
    @JsonProperty("conversation_id")
    private String conversationId;

    /**
     * 用户标识
     */
    private String user;

    /**
     * 输入参数
     */
    private Map<String, Object> inputs;

    /**
     * 是否启用流式响应
     */
    @JsonProperty("response_mode")
    private String responseMode = "blocking";

    /**
     * 关联的文件列表
     */
    private List<FileContext> files;
}
