package hbnu.project.zhiyanproject.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户任务统计DTO
 * 用于用户首页仪表盘展示
 *
 * @author System
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户任务统计信息")
public class UserTaskStatisticsDTO {

    /**
     * 用户总任务数（有效任务）
     */
    @Schema(description = "总任务数", example = "15")
    private Long totalTasks;

    /**
     * 被分配的任务数（ASSIGNED类型）
     */
    @Schema(description = "被分配的任务数", example = "10")
    private Long assignedTasks;

    /**
     * 主动接取的任务数（CLAIMED类型）
     */
    @Schema(description = "主动接取的任务数", example = "5")
    private Long claimedTasks;

    /**
     * 待办任务数（TODO状态）
     */
    @Schema(description = "待办任务数", example = "3")
    private Long todoTasks;

    /**
     * 进行中任务数（IN_PROGRESS状态）
     */
    @Schema(description = "进行中任务数", example = "8")
    private Long inProgressTasks;

    /**
     * 已完成任务数（DONE状态）
     */
    @Schema(description = "已完成任务数", example = "4")
    private Long doneTasks;

    /**
     * 已逾期任务数
     */
    @Schema(description = "已逾期任务数", example = "2")
    private Long overdueTasks;

    /**
     * 即将到期任务数（未来3天内）
     */
    @Schema(description = "即将到期任务数", example = "3")
    private Long upcomingTasks;

    /**
     * 参与的项目数量
     */
    @Schema(description = "参与的项目数量", example = "5")
    private Long projectCount;
}

