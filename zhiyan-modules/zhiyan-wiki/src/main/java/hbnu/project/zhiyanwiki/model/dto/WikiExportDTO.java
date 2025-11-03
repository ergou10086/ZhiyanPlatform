package hbnu.project.zhiyanwiki.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Wiki导出DTO
 * 用于导出请求参数
 *
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WikiExportDTO {

    /**
     * 导出格式（MARKDOWN, PDF, WORD）
     */
    private String format;

    /**
     * 是否包含子页面（用于目录导出）
     */
    @Builder.Default
    private Boolean includeChildren = false;

    /**
     * 是否包含附件
     */
    @Builder.Default
    private Boolean includeAttachments = false;

    /**
     * 是否包含图片
     */
    @Builder.Default
    private Boolean includeImages = true;

    /**
     * 要导出的页面ID列表（批量导出）
     */
    private List<Long> pageIds;

    /**
     * 导出文件名（可选，不指定则使用页面标题）
     */
    private String fileName;
}

