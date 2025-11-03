package hbnu.project.zhiyanwiki.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Wiki全文搜索结果DTO
 * 包含搜索评分和匹配片段
 *
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WikiSearchResultDTO {

    /**
     * Wiki页面ID
     */
    private String wikiPageId;

    /**
     * 页面标题
     */
    private String title;

    /**
     * 页面路径
     */
    private String path;

    /**
     * 匹配的内容片段（高亮关键字）
     */
    private String matchedSnippet;

    /**
     * 搜索评分（相关性分数）
     */
    private Double score;

    /**
     * 匹配关键字数量
     */
    private Integer matchCount;

    /**
     * 最后更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 最后编辑者ID
     */
    private String lastEditorId;

    /**
     * 项目ID
     */
    private String projectId;
}

