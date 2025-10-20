package hbnu.project.zhiyanknowledge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Wiki页面历史版本DTO
 * 用于返回Wiki页面的历史版本信息
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WikiPageHistoryDTO {

    /**
     * 历史记录ID
     */
    private String id;

    /**
     * Wiki页面ID
     */
    private String wikiPageId;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 页面标题（历史版本）
     */
    private String title;

    /**
     * Markdown内容（历史版本）
     */
    private String content;

    /**
     * 修改说明
     */
    private String changeDescription;

    /**
     * 编辑者ID
     */
    private String editorId;

    /**
     * 编辑者名称（需从auth服务获取）
     */
    private String editorName;

    /**
     * 内容大小（字符数）
     */
    private Integer contentSize;

    /**
     * 创建时间（版本创建时间）
     */
    private LocalDateTime createdAt;
}

