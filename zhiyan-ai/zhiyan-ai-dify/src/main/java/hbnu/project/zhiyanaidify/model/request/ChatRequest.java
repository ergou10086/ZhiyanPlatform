package hbnu.project.zhiyanaidify.model.request;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
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
    @Builder.Default
    private String responseMode = "blocking";

    /**
     * 关联的文件列表（Dify API 格式）
     */
    private List<DifyFile> files;

    /**
     * Dify 文件对象
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DifyFile {
        /**
         * 文件 MIME 类型
         * 例如：text/markdown, image/png, application/pdf
         * 必须是有效的 MIME 类型，否则 Dify API 会返回 400 错误
         */
        private String type;

        /**
         * 传输方式：remote_url, local_file
         */
        @JsonProperty("transfer_method")
        private String transferMethod;

        /**
         * 上传的文件 ID（当 transfer_method 为 local_file 时）
         */
        @JsonProperty("upload_file_id")
        private String uploadFileId;

        /**
         * 远程 URL（当 transfer_method 为 remote_url 时）
         */
        private String url;
    }
}
