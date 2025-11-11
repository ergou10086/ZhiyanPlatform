package hbnu.project.zhiyanknowledge.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 上传会话响应
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "上传会话信息")
public class UploadSessionDTO {

    @Schema(description = "会话ID")
    private String sessionId;

    @Schema(description = "MinIO uploadId")
    private String uploadId;

    @Schema(description = "成果ID")
    private Long achievementId;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "文件总大小(字节)")
    private Long fileSize;

    @Schema(description = "分片大小(字节)")
    private Integer chunkSize;

    @Schema(description = "总分片数")
    private Integer totalChunks;

    @Schema(description = "已上传分片列表")
    private List<Integer> uploadedChunks;

    @Schema(description = "上传进度(百分比)")
    private Double progress;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "对象键")
    private String objectKey;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
