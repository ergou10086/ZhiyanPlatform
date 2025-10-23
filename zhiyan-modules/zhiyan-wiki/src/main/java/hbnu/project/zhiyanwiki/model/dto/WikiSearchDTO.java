package hbnu.project.zhiyanwiki.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Wiki搜索结果DTO
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WikiSearchDTO {

    /**
     * 页面ID
     */
    private String id;

    /**
     * 页面标题
     */
    private String title;

    /**
     * 页面路径
     */
    private String path;

    /**
     * 页面类型
     */
    private String pageType;

    /**
     * 内容摘要（高亮匹配部分）
     */
    private String contentSummary;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 最后编辑者ID
     */
    private String lastEditorId;
}