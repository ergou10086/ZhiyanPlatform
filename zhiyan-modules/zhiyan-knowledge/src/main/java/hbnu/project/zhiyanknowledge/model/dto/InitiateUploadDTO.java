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
 * 初始化上传请求的DTO
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "初始化分片上传请求")
public class InitiateUploadDTO {

    @NotNull(message = "成果ID不能为空")
    @Schema(description = "成果ID")
    private Long achievementId;

    @NotBlank(message = "文件名不能为空")
    @Schema(description = "文件名")
    private String fileName;

    @NotNull(message = "文件大小不能为空")
    @Min(value = 1, message = "文件大小必须大于0")
    @Schema(description = "文件总大小(字节)")
    private Long fileSize;

    @Builder.Default
    @Schema(description = "分片大小(字节),默认5MB")
    private Integer chunkSize = 5 * 1024 * 1024; // 默认5MB
}
