package hbnu.project.zhiyanwiki.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Wiki页面详情DTO（包含完整内容）
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WikiPageDetailDTO {

    /**
     * 页面ID
     */
    private String id;

    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 页面标题
     */
    private String title;

    /**
     * 页面类型
     */
    private String pageType;

    /**
     * 父页面ID
     */
    private String parentId;

    /**
     * 页面路径
     */
    private String path;

    /**
     * Markdown内容（仅文档类型）
     */
    private String content;

    /**
     * 内容摘要
     */
    private String contentSummary;

    /**
     * 当前版本号
     */
    private Integer currentVersion;

    /**
     * 内容大小
     */
    private Integer contentSize;

    /**
     * 是否公开
     */
    private Boolean isPublic;

    /**
     * 是否被锁定
     */
    private Boolean isLocked;

    /**
     * 锁定者ID
     */
    private String lockedBy;

    /**
     * 创建者ID
     */
    private String creatorId;

    /**
     * 最后编辑者ID
     */
    private String lastEditorId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * MongoDB内容文档ID
     */
    private String mongoContentId;
}
