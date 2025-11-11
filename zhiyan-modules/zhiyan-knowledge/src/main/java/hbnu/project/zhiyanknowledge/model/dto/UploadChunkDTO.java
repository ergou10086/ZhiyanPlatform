package hbnu.project.zhiyanknowledge.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 上传分片请求
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "上传分片请求")
public class UploadChunkDTO {

    @NotBlank(message = "uploadId不能为空")
    @Schema(description = "MinIO uploadId")
    private String uploadId;

    @NotNull(message = "分片号不能为空")
    @Min(value = 1, message = "分片号必须大于0")
    @Schema(description = "分片号(从1开始)")
    private Integer chunkNumber;

    @Schema(description = "当前分片ETag(用于断点续传)")
    private String etag;
}
