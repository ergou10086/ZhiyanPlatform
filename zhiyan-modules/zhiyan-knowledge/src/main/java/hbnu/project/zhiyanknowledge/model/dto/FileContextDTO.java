package hbnu.project.zhiyanknowledge.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文件上下文 DTO
 * 用于向 AI 模块提供文件信息
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileContextDTO {

    /**
     * 文件 ID
     */
    @JsonProperty("file_id")
    private String fileId;

    /**
     * 成果 ID
     */
    @JsonProperty("achievement_id")
    private String achievementId;

    /**
     * 文件名称
     */
    @JsonProperty("file_name")
    private String fileName;

    /**
     * 文件类型
     */
    @JsonProperty("file_type")
    private String fileType;

    /**
     * 文件大小（字节）
     */
    @JsonProperty("file_size")
    private Long fileSize;

    /**
     * 文件大小（格式化）
     */
    @JsonProperty("file_size_formatted")
    private String fileSizeFormatted;

    /**
     * 文件内容/摘要（如果有）
     */
    private String content;

    /**
     * 文件 URL
     */
    @JsonProperty("file_url")
    private String fileUrl;

    /**
     * 上传者姓名
     */
    @JsonProperty("uploader_name")
    private String uploaderName;

    /**
     * 上传时间
     */
    @JsonProperty("upload_at")
    private LocalDateTime uploadAt;

    /**
     * 文件扩展名
     */
    private String extension;

    /**
     * MIME 类型
     */
    @JsonProperty("mime_type")
    private String mimeType;
}

