package hbnu.project.zhiyanknowledge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Wiki页面DTO
 * 用于列表展示和树状结构返回
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WikiPageDTO {

    /**
     * 页面ID
     */
    private String id;

    /**
     * 所属项目ID
     */
    private String projectId;

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
     * 内容摘要（截取前200字符）
     */
    private String contentSummary;

    /**
     * 内容长度（字符数）
     */
    private Integer contentLength;

    /**
     * 子页面数量
     */
    private Integer childrenCount;

    /**
     * 子页面列表（用于树状结构）
     */
    private List<WikiPageDTO> children;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 版本号
     */
    private Integer version;
}

