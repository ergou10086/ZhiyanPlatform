package hbnu.project.zhiyanknowledge.model.dto;

import hbnu.project.zhiyanknowledge.model.enums.AchievementStatus;
import hbnu.project.zhiyanknowledge.model.enums.AchievementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 成果查询条件DTO
 * 用于接收前端的查询条件，支持多条件组合查询
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementQueryDTO {

    /**
     * 项目ID（可选）
     */
    private Long projectId;

    /**
     * 成果类型（可选）
     */
    private AchievementType type;

    /**
     * 成果状态（可选）
     */
    private AchievementStatus status;

    /**
     * 创建者ID（可选）
     */
    private Long creatorId;

    /**
     * 标题关键字（模糊查询）
     */
    private String titleKeyword;

    /**
     * 标签（包含查询）
     */
    private String tag;

    /**
     * 摘要关键字（模糊查询）
     */
    private String abstractKeyword;

    /**
     * 创建时间开始（可选）
     */
    private LocalDateTime createdAtStart;

    /**
     * 创建时间结束（可选）
     */
    private LocalDateTime createdAtEnd;

    /**
     * 更新时间开始（可选）
     */
    private LocalDateTime updatedAtStart;

    /**
     * 更新时间结束（可选）
     */
    private LocalDateTime updatedAtEnd;

    /**
     * 排序字段（默认：createdAt）
     * 可选值：createdAt, updatedAt, title
     */
    @Builder.Default
    private String sortBy = "createdAt";

    /**
     * 排序方向（默认：DESC）
     * 可选值：ASC, DESC
     */
    @Builder.Default
    private String sortOrder = "DESC";

    /**
     * 页码（从0开始，默认0）
     */
    @Builder.Default
    private Integer page = 0;

    /**
     * 每页数量（默认10）
     */
    @Builder.Default
    private Integer size = 10;

    /**
     * 是否只查询已发布的成果（用于公开展示）
     */
    @Builder.Default
    private Boolean onlyPublished = false;
}

