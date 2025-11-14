package hbnu.project.zhiyanknowledge.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * 批量下载响应
 *
 * @author ErgouTree
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "批量下载响应")
public class BatchDownloadResponseDTO {

    @Schema(description = "下载任务ID")
    private String downloadTaskId;

    @Schema(description = "打包压缩文件下载URL(如果使用压缩包方式)")
    private String zipDownloadUrl;

    @Schema(description = "单个文件下载链接列表")
    private List<FileDownloadInfo> files;


    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileDownloadInfo {
        @Schema(description = "文件ID")
        private Long fileId;

        @Schema(description = "文件名")
        private String fileName;

        @Schema(description = "下载URL")
        private String downloadUrl;

        @Schema(description = "文件大小(字节)")
        private Long fileSize;

        @Schema(description = "是否下载成功")
        private Boolean success;

        @Schema(description = "错误信息")
        private String errorMessage;
    }
}
