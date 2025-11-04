package hbnu.project.zhiyanproject.repository;

import hbnu.project.zhiyanproject.model.entity.TaskUser;
import hbnu.project.zhiyanproject.model.enums.RoleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 任务用户关联Repository
 *
 * @author System
 */
@Repository
public interface TaskUserRepository extends JpaRepository<TaskUser, Long> {

    /**
     * 查询任务的当前执行者
     * 注意：暂时不使用role_type过滤，所有有效成员都是执行者
     *
     * @param taskId 任务ID
     * @return 执行者列表
     */
    @Query("SELECT tu FROM TaskUser tu WHERE tu.taskId = :taskId " +
           "AND tu.isActive = true " +
           "ORDER BY tu.assignedAt ASC")
    List<TaskUser> findActiveExecutorsByTaskId(@Param("taskId") Long taskId);


    /**
     * 查询用户的所有有效任务（分页）
     *
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 任务分页列表
     */
    @Query("SELECT tu FROM TaskUser tu WHERE tu.userId = :userId " +
           "AND tu.isActive = true ORDER BY tu.assignedAt DESC")
    Page<TaskUser> findActiveTasksByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 查询用户在项目中的任务（使用project_id加速查询）
     *
     * @param userId 用户ID
     * @param projectId 项目ID
     * @return 任务列表
     */
    @Query("SELECT tu FROM TaskUser tu WHERE tu.userId = :userId " +
           "AND tu.projectId = :projectId AND tu.isActive = true " +
           "ORDER BY tu.assignedAt DESC")
    List<TaskUser> findActiveTasksByUserAndProject(
        @Param("userId") Long userId,
        @Param("projectId") Long projectId
    );

    /**
     * 检查用户是否已是任务的有效执行者
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return true-已是执行者，false-不是
     */
    @Query("SELECT CASE WHEN COUNT(tu) > 0 THEN true ELSE false END " +
           "FROM TaskUser tu WHERE tu.taskId = :taskId " +
           "AND tu.userId = :userId AND tu.isActive = true")
    boolean isUserActiveExecutor(@Param("taskId") Long taskId, @Param("userId") Long userId);

    /**
     * 查询指定任务、用户的记录（用于避免重复分配）
     * 注意：暂时不使用role_type，前端未实现
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 任务用户关联记录
     */
    Optional<TaskUser> findByTaskIdAndUserId(Long taskId, Long userId);

    /**
     * 统计用户的有效任务数量
     *
     * @param userId 用户ID
     * @return 任务数量
     */
    @Query("SELECT COUNT(tu) FROM TaskUser tu WHERE tu.userId = :userId " +
           "AND tu.isActive = true")
    long countActiveTasksByUserId(@Param("userId") Long userId);

    /**
     * 统计项目的任务分配情况
     *
     * @param projectId 项目ID
     * @return 分配数量
     */
    @Query("SELECT COUNT(tu) FROM TaskUser tu WHERE tu.projectId = :projectId " +
           "AND tu.isActive = true")
    long countActiveAssignmentsByProjectId(@Param("projectId") Long projectId);

    /**
     * 查询任务的分配历史（包括已移除的）
     *
     * @param taskId 任务ID
     * @return 历史记录列表
     */
    @Query("SELECT tu FROM TaskUser tu WHERE tu.taskId = :taskId " +
           "ORDER BY tu.assignedAt DESC")
    List<TaskUser> findTaskAssignHistory(@Param("taskId") Long taskId);

    /**
     * 软删除任务的所有有效执行者
     *
     * @param taskId 任务ID
     * @param removedAt 移除时间
     * @param removedBy 移除操作人ID
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE TaskUser tu SET tu.isActive = false, " +
           "tu.removedAt = :removedAt, tu.removedBy = :removedBy, " +
           "tu.updatedAt = :removedAt " +
           "WHERE tu.taskId = :taskId AND tu.isActive = true")
    int deactivateTaskAssignees(
        @Param("taskId") Long taskId,
        @Param("removedAt") Instant removedAt,
        @Param("removedBy") Long removedBy
    );

    /**
     * 软删除指定的任务用户关联
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @param removedAt 移除时间
     * @param removedBy 移除操作人ID
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE TaskUser tu SET tu.isActive = false, " +
           "tu.removedAt = :removedAt, tu.removedBy = :removedBy, " +
           "tu.updatedAt = :removedAt " +
           "WHERE tu.taskId = :taskId AND tu.userId = :userId " +
           "AND tu.isActive = true")
    int deactivateTaskUser(
        @Param("taskId") Long taskId,
        @Param("userId") Long userId,
        @Param("removedAt") Instant removedAt,
        @Param("removedBy") Long removedBy
    );

    /**
     * 同步更新project_id（任务转移项目时使用）
     *
     * @param taskId 任务ID
     * @param newProjectId 新项目ID
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE TaskUser tu SET tu.projectId = :newProjectId, " +
           "tu.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE tu.taskId = :taskId")
    int updateProjectIdByTaskId(
        @Param("taskId") Long taskId,
        @Param("newProjectId") Long newProjectId
    );

    /**
     * 批量查询任务的执行者（用于批量加载）
     *
     * @param taskIds 任务ID列表
     * @return 任务用户关联列表
     */
    @Query("SELECT tu FROM TaskUser tu WHERE tu.taskId IN :taskIds " +
           "AND tu.isActive = true " +
           "ORDER BY tu.taskId, tu.assignedAt")
    List<TaskUser> findActiveExecutorsByTaskIds(@Param("taskIds") List<Long> taskIds);
}

