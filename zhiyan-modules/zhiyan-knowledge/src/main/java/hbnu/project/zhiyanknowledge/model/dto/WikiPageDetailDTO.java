package hbnu.project.zhiyanknowledge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Wiki页面详细信息DTO
 * 用于返回Wiki页面的完整信息，包括完整内容
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
     * 所属项目ID
     */
    private String projectId;

    /**
     * 项目名称（需从project服务获取）
     */
    private String projectName;

    /**
     * 页面标题
     */
    private String title;

    /**
     * Markdown内容（完整内容）
     */
    private String content;

    /**
     * 渲染后的HTML内容（可选，由前端渲染）
     */
    private String htmlContent;

    /**
     * 父页面ID
     */
    private String parentId;

    /**
     * 父页面标题
     */
    private String parentTitle;

    /**
     * 页面路径
     */
    private String path;

    /**
     * 面包屑导航（路径中的所有页面）
     */
    private List<BreadcrumbItem> breadcrumbs;

    /**
     * 排序序号
     */
    private Integer sortOrder;

    /**
     * 是否公开
     */
    private Boolean isPublic;

    /**
     * 创建者ID
     */
    private String creatorId;

    /**
     * 创建者名称（需从auth服务获取）
     */
    private String creatorName;

    /**
     * 最后编辑者ID
     */
    private String lastEditorId;

    /**
     * 最后编辑者名称（需从auth服务获取）
     */
    private String lastEditorName;

    /**
     * 子页面列表
     */
    private List<WikiPageDTO> children;

    /**
     * 历史版本数量
     */
    private Integer historyCount;

    /**
     * 最近的几个历史版本（最多5个）
     */
    private List<WikiPageHistoryDTO> recentHistories;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 版本号（乐观锁）
     */
    private Integer version;

    /**
     * 面包屑导航项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BreadcrumbItem {
        private String id;
        private String title;
        private String path;
    }
}

