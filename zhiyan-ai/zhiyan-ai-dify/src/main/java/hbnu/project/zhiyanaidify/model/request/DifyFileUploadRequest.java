package hbnu.project.zhiyanaidify.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * Dify 文件上传请求 DTO
 * 用于向 Dify 上传文件
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DifyFileUploadRequest {

    /**
     * 用户标识
     */
    private String user;

    /**
     * 上传的文件
     */
    private MultipartFile file;

    /**
     * 文件类型（document/image/audio/video）
     */
    @JsonProperty("file_type")
    private String fileType;

    /**
     * 文件用途（assistant/vision）
     */
    private String purpose;

    /**
     * 额外的元数据
     */
    private String metadata;
}

