package hbnu.project.zhiyanproject.model.dto;

import hbnu.project.zhiyanproject.model.enums.ProjectStatus;
import hbnu.project.zhiyanproject.model.enums.ProjectVisibility;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 项目数据传输对象
 *
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "项目信息")
public class ProjectDTO {

    /**
     * 项目ID
     */
    @Schema(description = "项目ID", example = "1977989681735929856")
    private String id;

    /**
     * 项目名称
     */
    @Schema(description = "项目名称", example = "AI智能分析平台")
    private String name;

    /**
     * 项目描述
     */
    @Schema(description = "项目描述", example = "基于深度学习的数据分析平台")
    private String description;

    /**
     * 项目状态
     */
    @Schema(description = "项目状态", example = "IN_PROGRESS")
    private ProjectStatus status;

    /**
     * 项目可见性
     */
    @Schema(description = "项目可见性", example = "PUBLIC")
    private ProjectVisibility visibility;

    /**
     * 项目开始日期
     */
    @Schema(description = "项目开始日期", example = "2025-01-01")
    private LocalDate startDate;

    /**
     * 项目结束日期
     */
    @Schema(description = "项目结束日期", example = "2025-12-31")
    private LocalDate endDate;

    /**
     * 项目图片URL
     */
    @Schema(description = "项目封面图片URL", example = "https://example.com/project.jpg")
    private String imageUrl;

    /**
     * 创建者ID
     */
    @Schema(description = "创建者ID", example = "1977989681735929856")
    private String creatorId;

    /**
     * 创建者名称
     */
    @Schema(description = "创建者名称", example = "张三")
    private String creatorName;

    /**
     * 成员数量
     */
    @Schema(description = "项目成员数量", example = "10")
    private Integer memberCount;

    /**
     * 任务数量
     */
    @Schema(description = "项目任务数量", example = "25")
    private Integer taskCount;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-01-01T10:00:00")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Schema(description = "最后更新时间", example = "2025-10-31T15:30:00")
    private LocalDateTime updatedAt;
}
