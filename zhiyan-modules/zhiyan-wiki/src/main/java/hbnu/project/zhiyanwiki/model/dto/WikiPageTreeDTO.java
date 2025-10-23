package hbnu.project.zhiyanwiki.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Wiki页面树状结构DTO
 * 用于前端展示Wiki的树形目录结构
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WikiPageTreeDTO {

    /**
     * 页面ID（转为String避免前端精度丢失）
     */
    private String id;

    /**
     * 页面标题
     */
    private String title;

    /**
     * 父页面ID
     */
    private String parentId;

    /**
     * 页面路径
     */
    private String path;

    /**
     * 排序序号
     */
    private Integer sortOrder;

    /**
     * 是否公开
     */
    private Boolean isPublic;

    /**
     * 是否有子节点
     */
    private Boolean hasChildren;

    /**
     * 子节点数量
     */
    private Integer childrenCount;

    /**
     * 子节点列表
     */
    private List<WikiPageTreeDTO> children;

    /**
     * 页面类型（DIRECTORY/DOCUMENT）
     */
    private String pageType;

    /**
     * 创建时间
     */
    private String createdAt;

    /**
     * 更新时间
     */
    private String updatedAt;

    /**
     * 内容摘要（仅文档类型）
     */
    private String contentSummary;

    /**
     * 当前版本号（仅文档类型）
     */
    private Integer currentVersion;
}
