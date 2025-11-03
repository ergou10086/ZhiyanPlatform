package hbnu.project.zhiyanwiki.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wiki附件查询DTO
 * 用于附件列表查询的条件封装
 *
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WikiAttachmentQueryDTO {

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * Wiki页面ID（可选）
     */
    private Long wikiPageId;

    /**
     * 附件类型（IMAGE或FILE，可选）
     */
    private String attachmentType;

    /**
     * 文件名关键字（可选）
     */
    private String fileName;

    /**
     * 上传者ID（可选）
     */
    private Long uploadBy;

    /**
     * 页码（从0开始）
     */
    @Builder.Default
    private Integer page = 0;

    /**
     * 每页大小
     */
    @Builder.Default
    private Integer size = 20;

    /**
     * 排序字段（uploadAt/fileSize/fileName）
     */
    @Builder.Default
    private String sortBy = "uploadAt";

    /**
     * 排序方向（ASC/DESC）
     */
    @Builder.Default
    private String sortDirection = "DESC";
}


