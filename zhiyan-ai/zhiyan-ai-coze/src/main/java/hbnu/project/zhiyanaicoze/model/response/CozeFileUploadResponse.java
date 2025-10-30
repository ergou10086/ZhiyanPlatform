package hbnu.project.zhiyanaicoze.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Coze 文件上传响应
 *
 * @author ErgouTree
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CozeFileUploadResponse {

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 文件数据
     */
    private FileData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileData {
        /**
         * 文件 ID
         */
        @JsonProperty("file_id")
        private String fileId;

        /**
         * 文件名
         */
        @JsonProperty("file_name")
        private String fileName;

        /**
         * 文件大小（字节）
         */
        @JsonProperty("file_size")
        private Long fileSize;

        /**
         * 文件类型
         */
        @JsonProperty("file_type")
        private String fileType;

        /**
         * 创建时间
         */
        @JsonProperty("created_at")
        private Long createdAt;
    }
}
