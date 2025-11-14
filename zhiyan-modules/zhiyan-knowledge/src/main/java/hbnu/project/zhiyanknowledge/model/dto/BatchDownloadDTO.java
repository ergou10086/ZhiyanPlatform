package hbnu.project.zhiyanknowledge.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量上传DTO
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "批量下载请求")
public class BatchDownloadDTO {

    @NotEmpty(message = "文件ID列表不能为空")
    @Schema(description = "文件ID列表")
    private List<Long> fileIds;

    @Schema(description = "下载链接过期时间(秒),默认1小时")
    private Integer expirySeconds = 3600;
}
