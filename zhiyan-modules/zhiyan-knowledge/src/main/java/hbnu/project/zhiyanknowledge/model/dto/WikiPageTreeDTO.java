package hbnu.project.zhiyanknowledge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Wiki页面树状结构DTO
 * 专用于返回树状结构的简化版本
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WikiPageTreeDTO {

    /**
     * 页面ID
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
     * 是否有子页面
     */
    private Boolean hasChildren;

    /**
     * 子页面数量
     */
    private Integer childrenCount;

    /**
     * 子页面列表（递归结构）
     */
    private List<WikiPageTreeDTO> children;

    /**
     * 节点图标（前端使用，可选）
     */
    private String icon;

    /**
     * 是否展开（前端使用，可选）
     */
    @Builder.Default
    private Boolean expanded = false;
}

