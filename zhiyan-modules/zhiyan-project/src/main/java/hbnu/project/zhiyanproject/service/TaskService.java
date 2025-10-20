package hbnu.project.zhiyanproject.service;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyanproject.model.entity.Tasks;
import hbnu.project.zhiyanproject.model.enums.TaskPriority;
import hbnu.project.zhiyanproject.model.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * 任务服务接口
 *
 * @author Tokito
 */
public interface TaskService {

    /**
     * 创建任务
     *
     * @param projectId 项目ID
     * @param title 任务标题
     * @param description 任务描述
     * @param priority 优先级
     * @param assigneeIds 负责人ID列表（JSON格式）
     * @param dueDate 截止日期
     * @param creatorId 创建者ID
     * @return 创建的任务
     */
    R<Tasks> createTask(Long projectId, String title, String description,
                        TaskPriority priority, String assigneeIds, LocalDate dueDate, Long creatorId);

    /**
     * 更新任务
     *
     * @param taskId 任务ID
     * @param title 任务标题
     * @param description 任务描述
     * @param status 任务状态
     * @param priority 优先级
     * @param assigneeIds 负责人ID列表
     * @param dueDate 截止日期
     * @return 更新后的任务
     */
    R<Tasks> updateTask(Long taskId, String title, String description,
                        TaskStatus status, TaskPriority priority, String assigneeIds, LocalDate dueDate);

    /**
     * 删除任务
     *
     * @param taskId 任务ID
     * @param userId 操作用户ID
     * @return 删除结果
     */
    R<Void> deleteTask(Long taskId, Long userId);

    /**
     * 根据ID获取任务
     *
     * @param taskId 任务ID
     * @return 任务信息
     */
    R<Tasks> getTaskById(Long taskId);

    /**
     * 获取项目的所有任务
     *
     * @param projectId 项目ID
     * @param pageable 分页参数
     * @return 任务列表
     */
    R<Page<Tasks>> getProjectTasks(Long projectId, Pageable pageable);

    /**
     * 根据状态获取项目任务
     *
     * @param projectId 项目ID
     * @param status 任务状态
     * @param pageable 分页参数
     * @return 任务列表
     */
    R<Page<Tasks>> getTasksByStatus(Long projectId, TaskStatus status, Pageable pageable);

    /**
     * 根据优先级获取项目任务
     *
     * @param projectId 项目ID
     * @param priority 优先级
     * @param pageable 分页参数
     * @return 任务列表
     */
    R<Page<Tasks>> getTasksByPriority(Long projectId, TaskPriority priority, Pageable pageable);

    /**
     * 获取用户创建的任务
     *
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 任务列表
     */
    R<Page<Tasks>> getUserCreatedTasks(Long userId, Pageable pageable);


    /**
     * 更新任务状态
     *
     * @param taskId 任务ID
     * @param status 新状态
     * @return 更新后的任务
     */
    R<Tasks> updateTaskStatus(Long taskId, TaskStatus status);



    /**
     * 搜索任务（按标题或描述）
     *
     * @param projectId 项目ID
     * @param keyword 关键字
     * @param pageable 分页参数
     * @return 任务列表
     */
    R<Page<Tasks>> searchTasks(Long projectId, String keyword, Pageable pageable);

    /**
     * 统计项目任务数量
     *
     * @param projectId 项目ID
     * @return 任务数量

    R<Long> countProjectTasks(Long projectId);

    /**
     * 统计项目中指定状态的任务数量
     *
     * @param projectId 项目ID
     * @param status 任务状态
     * @return 任务数量

    R<Long> countTasksByStatus(Long projectId, TaskStatus status);
    */
}
