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
 * 工作流对话请求 DTO
 * 用于调用 Dify 工作流，支持文件上传
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowChatRequest {

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
     * 输入参数（工作流变量）
     */
    private Map<String, Object> inputs;

    /**
     * 响应模式（streaming/blocking）
     */
    @JsonProperty("response_mode")
    private String responseMode = "streaming";

    /**
     * 已上传到 Dify 的文件 ID 列表
     */
    @JsonProperty("file_ids")
    private List<String> fileIds;

    /**
     * 文件上传类型（local/remote）
     */
    @JsonProperty("file_upload_type")
    private String fileUploadType = "local";

    /**
     * 自动播放（用于音频）
     */
    @JsonProperty("auto_play")
    private Boolean autoPlay = false;
}

