package hbnu.project.zhiyanproject.repository;

import hbnu.project.zhiyanproject.model.entity.TaskUser;
import hbnu.project.zhiyanproject.model.entity.Tasks;
import hbnu.project.zhiyanproject.model.enums.AssignType;
import hbnu.project.zhiyanproject.model.enums.TaskPriority;
import hbnu.project.zhiyanproject.model.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 任务数据访问层
 *
 * @author Tokito
 */
@Repository
public interface TaskRepository extends JpaRepository<Tasks, Long> {

    /**
     * 根据项目ID查询任务列表（不包含已删除的任务）
     *
     * @param projectId 项目ID
     * @param isDeleted 是否删除标记
     * @param pageable 分页参数
     * @return 任务分页列表
     */
    Page<Tasks> findByProjectIdAndIsDeleted(Long projectId, Boolean isDeleted, Pageable pageable);

    /**
     * 根据项目ID查询任务列表（不包含已删除的任务）
     *
     * @param projectId 项目ID
     * @param isDeleted 是否删除标记
     * @return 任务列表
     */
    List<Tasks> findByProjectIdAndIsDeleted(Long projectId, Boolean isDeleted);

    /**
     * 根据项目ID和状态查询任务（不包含已删除的任务）
     *
     * @param projectId 项目ID
     * @param status 任务状态
     * @param isDeleted 是否删除标记
     * @param pageable 分页参数
     * @return 任务分页列表
     */
    Page<Tasks> findByProjectIdAndStatusAndIsDeleted(Long projectId, TaskStatus status, Boolean isDeleted, Pageable pageable);

    /**
     * 根据项目ID和优先级查询任务（不包含已删除的任务）
     *
     * @param projectId 项目ID
     * @param priority 优先级
     * @param isDeleted 是否删除标记
     * @param pageable 分页参数
     * @return 任务分页列表
     */
    Page<Tasks> findByProjectIdAndPriorityAndIsDeleted(Long projectId, TaskPriority priority, Boolean isDeleted, Pageable pageable);

    /**
     * 根据创建者查询任务（不包含已删除的任务）
     *
     * @param createdBy 创建者ID
     * @param isDeleted 是否删除标记
     * @param pageable 分页参数
     * @return 任务分页列表
     */
    Page<Tasks> findByCreatedByAndIsDeleted(Long createdBy, Boolean isDeleted, Pageable pageable);

    /**
     * 根据项目ID和标题或描述搜索任务（不包含已删除的任务）
     *
     * @param projectId 项目ID
     * @param keyword 关键字
     * @param pageable 分页参数
     * @return 任务分页列表
     */
    @Query("SELECT t FROM Tasks t WHERE t.projectId = :projectId AND t.isDeleted = false AND (t.title LIKE %:keyword% OR t.description LIKE %:keyword%)")
    Page<Tasks> searchByKeyword(@Param("projectId") Long projectId, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 统计项目任务数量
     *
     * @param projectId 项目ID
     * @return 任务数量
     */
    long countByProjectId(Long projectId);

    /**
     * 统计项目中指定状态的任务数量
     *
     * @param projectId 项目ID
     * @param status 任务状态
     * @return 任务数量
     */
    long countByProjectIdAndStatus(Long projectId, TaskStatus status);

    /**
     * 统计创建者的任务数量
     *
     * @param createdBy 创建者ID
     * @return 任务数量
     */
    long countByCreatedBy(Long createdBy);

    /**
     * 根据项目ID删除所有任务
     *
     * @param projectId 项目ID
     * @return 删除的记录数
     */
    int deleteByProjectId(Long projectId);

    /**
     * 根据截止日期范围查询任务（不包含已删除的任务）
     *
     * @param projectId 项目ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param pageable 分页参数
     * @return 任务分页列表
     */
    @Query("SELECT t FROM Tasks t WHERE t.projectId = :projectId AND t.isDeleted = false AND t.dueDate BETWEEN :startDate AND :endDate")
    Page<Tasks> findByDueDateBetween(@Param("projectId") Long projectId,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate,
                                     Pageable pageable);

    /**
     * 查询即将到期的任务（不包含已删除的任务）
     *
     * @param projectId 项目ID
     * @param dueDate 截止日期
     * @param pageable 分页参数
     * @return 任务分页列表
     */
    @Query("SELECT t FROM Tasks t WHERE t.projectId = :projectId AND t.isDeleted = false AND t.dueDate <= :dueDate AND t.status != 'DONE'")
    Page<Tasks> findUpcomingTasks(@Param("projectId") Long projectId,
                                  @Param("dueDate") LocalDate dueDate,
                                  Pageable pageable);

    /**
     * 查询已逾期的任务（不包含已删除的任务）
     *
     * @param projectId 项目ID
     * @param currentDate 当前日期
     * @param pageable 分页参数
     * @return 任务分页列表
     */
    @Query("SELECT t FROM Tasks t WHERE t.projectId = :projectId AND t.isDeleted = false AND t.dueDate < :currentDate AND t.status != 'DONE'")
    Page<Tasks> findOverdueTasks(@Param("projectId") Long projectId,
                                 @Param("currentDate") LocalDate currentDate,
                                 Pageable pageable);

    /**
     * 根据负责人ID查询任务（JSON字段模糊查询，不包含已删除的任务）
     *
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 任务分页列表
     */
    @Query(value = "SELECT * FROM tasks WHERE JSON_CONTAINS(assignee_id, :userId, '$') AND is_deleted = false", nativeQuery = true)
    Page<Tasks> findByAssigneeId(@Param("userId") String userId, Pageable pageable);

    /**
     * 查询用户在所有项目中即将到期的任务
     *
     * @param userId 用户ID
     * @param dueDate 截止日期
     * @param pageable 分页参数
     * @return 任务分页列表
     */
    @Query(value = "SELECT * FROM tasks WHERE JSON_CONTAINS(assignee_id, :userId, '$') " +
                   "AND due_date <= :dueDate AND due_date >= CURDATE() " +
                   "AND status != 'DONE' AND is_deleted = false " +
                   "ORDER BY due_date ASC", 
           nativeQuery = true)
    Page<Tasks> findMyUpcomingTasks(@Param("userId") String userId,
                                     @Param("dueDate") LocalDate dueDate,
                                     Pageable pageable);

    /**
     * 查询用户在所有项目中已逾期的任务
     *
     * @param userId 用户ID
     * @param currentDate 当前日期
     * @param pageable 分页参数
     * @return 任务分页列表
     */
    @Query(value = "SELECT * FROM tasks WHERE JSON_CONTAINS(assignee_id, :userId, '$') " +
                   "AND due_date < :currentDate " +
                   "AND status != 'DONE' AND is_deleted = false " +
                   "ORDER BY due_date DESC", 
           nativeQuery = true)
    Page<Tasks> findMyOverdueTasks(@Param("userId") String userId,
                                    @Param("currentDate") LocalDate currentDate,
                                    Pageable pageable);

    /**
     * 查询用户创建的任务，并排除已删除的项目
     *
     * @param createdBy 创建者ID
     * @param pageable 分页参数
     * @return 任务分页列表
     */
    @Query("SELECT t FROM Tasks t WHERE t.createdBy = :createdBy AND t.isDeleted = false " +
           "AND t.projectId IN (SELECT p.id FROM Project p WHERE p.isDeleted = false)")
    Page<Tasks> findMyCreatedTasksWithActiveProjects(@Param("createdBy") Long createdBy, 
                                                       Pageable pageable);


    /**
     * 查询任务的所有有效执行者（包括ASSIGNED和CLAIMED类型）
     *
     * @param taskId 任务ID
     * @return 执行者列表
     */
    @Query("SELECT tu FROM TaskUser tu WHERE tu.taskId = :taskId " +
            "AND tu.isActive = true " +
            "ORDER BY tu.assignedAt ASC")
    List<TaskUser> findActiveExecutorsByTaskId(@Param("taskId") Long taskId);


    /**
     * 查询任务的指定类型执行者
     *
     * @param taskId 任务ID
     * @param assignType 分配类型
     * @return 执行者列表
     */
    @Query("SELECT tu FROM TaskUser tu WHERE tu.taskId = :taskId " +
            "AND tu.assignType = :assignType " +
            "AND tu.isActive = true " +
            "ORDER BY tu.assignedAt ASC")
    List<TaskUser> findActiveExecutorsByTaskIdAndType(
            @Param("taskId") Long taskId,
            @Param("assignType") AssignType assignType
    );


    /**
     * 查询即将到期的任务（不包含已删除的任务）
     *
     * @param dueDate 截止日期
     * @return 任务列表
     */
    @Query("SELECT t FROM Tasks t WHERE t.isDeleted = false AND t.dueDate <= :dueDate AND t.status != 'DONE'")
    List<Tasks> findUpcomingTasks(@Param("dueDate") LocalDate dueDate);


    /**
     * 查询已逾期的任务（不包含已删除的任务）
     *
     * @param currentDate 当前日期
     * @return 任务列表
     */
    @Query("SELECT t FROM Tasks t WHERE t.isDeleted = false AND t.dueDate < :currentDate AND t.status != 'DONE'")
    List<Tasks> findOverdueTasks(@Param("currentDate") LocalDate currentDate);
}
