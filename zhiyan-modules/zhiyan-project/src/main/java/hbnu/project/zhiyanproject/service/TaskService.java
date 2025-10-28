package hbnu.project.zhiyanproject.service;

import hbnu.project.zhiyanproject.model.dto.TaskBoardDTO;
import hbnu.project.zhiyanproject.model.dto.TaskDetailDTO;
import hbnu.project.zhiyanproject.model.entity.Tasks;
import hbnu.project.zhiyanproject.model.enums.TaskPriority;
import hbnu.project.zhiyanproject.model.enums.TaskStatus;
import hbnu.project.zhiyanproject.model.form.CreateTaskRequest;
import hbnu.project.zhiyanproject.model.form.UpdateTaskRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 任务服务接口
 * 根据产品设计文档完整实现任务管理功能
 *
 * @author ErgouTree
 */
public interface TaskService {

    // ==================== 任务创建与管理 ====================

    /**
     * 创建任务
     * 业务流程：项目成员创建任务并分配给项目成员
     *
     * @param request   创建任务请求
     * @param creatorId 创建人ID（当前登录用户）
     * @return 创建的任务
     */
    Tasks createTask(CreateTaskRequest request, Long creatorId);

    /**
     * 更新任务
     * 业务流程：项目成员更新任务信息
     *
     * @param taskId     任务ID
     * @param request    更新任务请求
     * @param operatorId 操作人ID（当前登录用户）
     * @return 更新后的任务
     */
    Tasks updateTask(Long taskId, UpdateTaskRequest request, Long operatorId);

    /**
     * 删除任务（软删除）
     * 业务流程：任务创建者或项目负责人可以删除任务
     *
     * @param taskId     任务ID
     * @param operatorId 操作人ID（当前登录用户）
     */
    void deleteTask(Long taskId, Long operatorId);

    /**
     * 更新任务状态
     * 业务流程：执行者更新任务状态（拖拽看板或手动修改）
     *
     * @param taskId     任务ID
     * @param newStatus  新状态
     * @param operatorId 操作人ID（当前登录用户）
     * @return 更新后的任务
     */
    Tasks updateTaskStatus(Long taskId, TaskStatus newStatus, Long operatorId);

    /**
     * 分配任务给成员
     * 业务流程：项目成员重新分配任务执行者
     *
     * @param taskId      任务ID
     * @param assigneeIds 新的执行者ID列表
     * @param operatorId  操作人ID（当前登录用户）
     * @return 更新后的任务
     */
    Tasks assignTask(Long taskId, List<Long> assigneeIds, Long operatorId);

    // ==================== 任务查询 ====================

    /**
     * 获取任务详情
     *
     * @param taskId 任务ID
     * @return 任务详细信息
     */
    TaskDetailDTO getTaskDetail(Long taskId);

    /**
     * 获取项目任务看板
     * 业务流程：在项目详情页的"任务"标签页展示看板视图
     *
     * @param projectId 项目ID
     * @return 任务看板数据（包含各状态的任务列表）
     */
    TaskBoardDTO getProjectTaskBoard(Long projectId);

    /**
     * 获取项目的所有任务（分页）
     *
     * @param projectId 项目ID
     * @param pageable  分页参数
     * @return 任务详情分页列表
     */
    Page<TaskDetailDTO> getProjectTasks(Long projectId, Pageable pageable);

    /**
     * 根据状态获取项目任务
     *
     * @param projectId 项目ID
     * @param status    任务状态
     * @param pageable  分页参数
     * @return 任务详情分页列表
     */
    Page<TaskDetailDTO> getTasksByStatus(Long projectId, TaskStatus status, Pageable pageable);

    /**
     * 根据优先级获取项目任务
     *
     * @param projectId 项目ID
     * @param priority  优先级
     * @param pageable  分页参数
     * @return 任务详情分页列表
     */
    Page<TaskDetailDTO> getTasksByPriority(Long projectId, TaskPriority priority, Pageable pageable);

    /**
     * 获取分配给用户的任务
     * 业务流程：用户查看自己的任务列表
     *
     * @param userId   用户ID
     * @param pageable 分页参数
     * @return 任务详情分页列表
     */
    Page<TaskDetailDTO> getMyAssignedTasks(Long userId, Pageable pageable);

    /**
     * 获取用户创建的任务
     *
     * @param userId   用户ID
     * @param pageable 分页参数
     * @return 任务详情分页列表
     */
    Page<TaskDetailDTO> getMyCreatedTasks(Long userId, Pageable pageable);

    /**
     * 搜索任务
     * 业务流程：根据关键词搜索项目任务
     *
     * @param projectId 项目ID
     * @param keyword   关键词
     * @param pageable  分页参数
     * @return 任务详情分页列表
     */
    Page<TaskDetailDTO> searchTasks(Long projectId, String keyword, Pageable pageable);

    /**
     * 获取即将到期的任务
     *
     * @param projectId 项目ID
     * @param days      未来天数
     * @param pageable  分页参数
     * @return 任务详情分页列表
     */
    Page<TaskDetailDTO> getUpcomingTasks(Long projectId, int days, Pageable pageable);

    /**
     * 获取已逾期的任务
     *
     * @param projectId 项目ID
     * @param pageable  分页参数
     * @return 任务详情分页列表
     */
    Page<TaskDetailDTO> getOverdueTasks(Long projectId, Pageable pageable);

    /**
     * 获取当前用户在所有参与项目中即将到期的任务
     * 业务场景：首页提醒用户快要截止的任务
     *
     * @param userId   用户ID
     * @param days     未来天数
     * @param pageable 分页参数
     * @return 任务详情分页列表
     */
    Page<TaskDetailDTO> getMyUpcomingTasks(Long userId, int days, Pageable pageable);

    /**
     * 获取当前用户在所有参与项目中已逾期的任务
     * 业务场景：首页提醒用户已逾期的任务
     *
     * @param userId   用户ID
     * @param pageable 分页参数
     * @return 任务详情分页列表
     */
    Page<TaskDetailDTO> getMyOverdueTasks(Long userId, Pageable pageable);

    // ==================== 统计相关 ====================

    /**
     * 统计项目任务数量
     *
     * @param projectId 项目ID
     * @return 任务数量
     */
    long countProjectTasks(Long projectId);

    /**
     * 统计项目中指定状态的任务数量
     *
     * @param projectId 项目ID
     * @param status    任务状态
     * @return 任务数量
     */
    long countTasksByStatus(Long projectId, TaskStatus status);

    /**
     * 统计项目逾期任务数量
     *
     * @param projectId 项目ID
     * @return 逾期任务数量
     */
    long countOverdueTasks(Long projectId);
}
