package hbnu.project.zhiyanwiki.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wiki附件上传DTO
 * 用于接收附件上传请求
 *
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WikiAttachmentUploadDTO {

    /**
     * Wiki页面ID
     */
    @NotNull(message = "Wiki页面ID不能为空")
    private Long wikiPageId;

    /**
     * 项目ID
     */
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    /**
     * 附件类型（IMAGE或FILE）
     * 如果不指定，会根据文件扩展名自动判断
     */
    private String attachmentType;

    /**
     * 文件描述/备注
     */
    private String description;

    /**
     * 上传者ID（从安全上下文获取）
     */
    private Long uploadBy;
}


