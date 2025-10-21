package hbnu.project.zhiyanknowledge.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传DTO
 * 用于接收文件上传请求的元数据
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadFileDTO {

    /**
     * 所属成果ID（必填）
     */
    @NotNull(message = "成果ID不能为空")
    private Long achievementId;

//    /**
//     * 是否覆盖已存在的同名文件（默认false）
//     * - true: 覆盖模式，将旧文件标记为历史版本，新文件作为最新版本
//     * - false: 如果同名文件存在则报错
//     */
//    @Builder.Default
//    private Boolean overwrite = false;

    /**
     * 上传者ID（由后端从上下文获取）
     */
    private Long uploadBy;
}

