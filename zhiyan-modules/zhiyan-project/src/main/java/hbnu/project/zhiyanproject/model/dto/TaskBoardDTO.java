package hbnu.project.zhiyanproject.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 任务看板DTO
 * 用于任务看板视图展示
 *
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskBoardDTO {

    /**
     * 待办任务列表
     */
    private List<TaskDetailDTO> todoTasks;

    /**
     * 进行中任务列表
     */
    private List<TaskDetailDTO> inProgressTasks;

    /**
     * 阻塞任务列表
     */
    private List<TaskDetailDTO> blockedTasks;

    /**
     * 已完成任务列表
     */
    private List<TaskDetailDTO> doneTasks;

    /**
     * 各状态任务数量统计
     */
    private TaskStatistics statistics;

    /**
     * 任务统计信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskStatistics {
        /**
         * 待办任务数量
         */
        private Long todoCount;

        /**
         * 进行中任务数量
         */
        private Long inProgressCount;

        /**
         * 阻塞任务数量
         */
        private Long blockedCount;

        /**
         * 已完成任务数量
         */
        private Long doneCount;

        /**
         * 总任务数量
         */
        private Long totalCount;

        /**
         * 逾期任务数量
         */
        private Long overdueCount;
    }
}