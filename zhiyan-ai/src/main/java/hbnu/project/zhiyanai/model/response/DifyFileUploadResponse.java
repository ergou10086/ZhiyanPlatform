package hbnu.project.zhiyanai.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dify 文件上传响应 DTO
 * 封装 Dify API 返回的文件上传结果
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DifyFileUploadResponse {

    /**
     * 文件 ID
     */
    @JsonProperty("id")
    private String fileId;

    /**
     * 文件名称
     */
    @JsonProperty("name")
    private String fileName;

    /**
     * 文件大小（字节）
     */
    @JsonProperty("size")
    private Long fileSize;

    /**
     * 文件扩展名
     */
    @JsonProperty("extension")
    private String extension;

    /**
     * MIME 类型
     */
    @JsonProperty("mime_type")
    private String mimeType;

    /**
     * 文件类型
     */
    @JsonProperty("type")
    private String type;

    /**
     * 创建时间
     */
    @JsonProperty("created_at")
    private Long createdAt;

    /**
     * 创建者
     */
    @JsonProperty("created_by")
    private String createdBy;

    /**
     * 上传状态
     */
    private String status;

    /**
     * 错误信息
     */
    @JsonProperty("error_message")
    private String errorMessage;
}

