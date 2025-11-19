package hbnu.project.zhiyanknowledge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 任务成果 detailData 中的任务引用信息。
 * 用于展示关联任务的详细信息（从任务服务查询后填充）。
 * 
 * 注意：
 * - 此DTO主要用于展示，不直接持久化到detailData JSON中
 * - 任务详情通过调用项目服务API获取
 * - 在查询成果详情时，通过应用层关联填充此信息
 *
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResultTaskRefDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务 ID（必填）。
     * 用于唯一标识任务。
     */
    private Long id;

    /**
     * 任务标题（必填）。
     * 用于展示任务名称。
     */
    private String title;

    /**
     * 任务描述（可选）。
     * 任务的详细描述信息。
     */
    private String description;

    /**
     * 任务状态（必填）。
     * 可选值：TODO, IN_PROGRESS, BLOCKED, PENDING_REVIEW, DONE
     */
    private String status;

    /**
     * 任务优先级（可选）。
     * 可选值：HIGH, MEDIUM, LOW
     */
    private String priority;

    /**
     * 任务所属项目ID（必填）。
     * 用于标识任务所属的项目。
     */
    private Long projectId;

    /**
     * 任务创建者ID（必填）。
     * 用于标识任务的创建人。
     */
    private Long creatorId;

    /**
     * 任务创建者名称（可选）。
     * 用于展示创建者姓名，通常从用户服务获取。
     */
    private String creatorName;

    /**
     * 任务负责人ID列表（可选，JSON格式）。
     * 存储多个负责人的ID，JSON数组格式。
     */
    private String assigneeIds;

    /**
     * 任务负责人名称列表（可选）。
     * 用于展示负责人姓名列表。
     */
    @Builder.Default
    private List<String> assigneeNames = new ArrayList<>();

    /**
     * 任务完成时间（可选）。
     * 当任务状态为DONE时，记录完成时间。
     */
    private LocalDateTime completedAt;

    /**
     * 最新提交记录ID（可选）。
     * 用于关联任务的最新提交记录。
     */
    private Long latestSubmissionId;

    /**
     * 最新提交时间（可选）。
     * 记录任务的最新提交时间。
     */
    private LocalDateTime latestSubmissionTime;

    /**
     * 提交人ID（可选）。
     * 最新提交记录的提交人ID。
     */
    private Long submitterId;

    /**
     * 提交人名称（可选）。
     * 最新提交记录的提交人姓名。
     */
    private String submitterName;
}
