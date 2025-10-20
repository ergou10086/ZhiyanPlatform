package hbnu.project.zhiyanknowledge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wiki页面查询条件DTO
 * 用于接收前端的查询条件
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WikiPageQueryDTO {

    /**
     * 项目ID（可选）
     */
    private Long projectId;

    /**
     * 父页面ID（可选，null表示查询根页面）
     */
    private Long parentId;

    /**
     * 标题关键字（模糊查询）
     */
    private String titleKeyword;

    /**
     * 内容关键字（模糊查询）
     */
    private String contentKeyword;

    /**
     * 创建者ID（可选）
     */
    private Long creatorId;

    /**
     * 是否只查询公开页面（默认false）
     */
    @Builder.Default
    private Boolean onlyPublic = false;

    /**
     * 是否构建树状结构（默认false）
     */
    @Builder.Default
    private Boolean buildTree = false;

    /**
     * 排序字段（默认：sortOrder）
     * 可选值：sortOrder, createdAt, updatedAt, title
     */
    @Builder.Default
    private String sortBy = "sortOrder";

    /**
     * 排序方向（默认：ASC）
     * 可选值：ASC, DESC
     */
    @Builder.Default
    private String sortOrder = "ASC";

    /**
     * 页码（从0开始，默认0）
     */
    @Builder.Default
    private Integer page = 0;

    /**
     * 每页数量（默认20）
     */
    @Builder.Default
    private Integer size = 20;
}

