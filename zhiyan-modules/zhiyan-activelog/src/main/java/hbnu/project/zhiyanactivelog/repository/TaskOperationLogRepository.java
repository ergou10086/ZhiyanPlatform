package hbnu.project.zhiyanactivelog.repository;

import hbnu.project.zhiyanactivelog.model.entity.TaskOperationLog;
import hbnu.project.zhiyanactivelog.model.enums.TaskOperationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * 任务操作日志数据访问层
 *
 * @author ErgouTree
 */
@Repository
public interface TaskOperationLogRepository extends JpaRepository<TaskOperationLog, Long> , JpaSpecificationExecutor<TaskOperationLog> {

    /**
     * 根据项目ID查询操作日志（分页）
     *
     * @param projectId 项目ID
     * @param pageable  分页参数
     * @return 操作日志分页列表
     */
    Page<TaskOperationLog> findByProjectIdOrderByOperationTimeDesc(Long projectId, Pageable pageable);

    /**
     * 根据项目ID和任务ID查询操作日志（分页）
     *
     * @param projectId 项目ID
     * @param taskId 任务ID
     * @param pageable 分页参数
     * @return 操作日志分页列表
     */
    Page<TaskOperationLog> findByProjectIdAndTaskIdOrderByOperationTimeDesc(Long projectId, Long taskId, Pageable pageable);

    /**
     * 根据项目ID和用户ID查询操作日志（分页）
     *
     * @param projectId 项目ID
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 操作日志分页列表
     */
    Page<TaskOperationLog> findByProjectIdAndUserIdOrderByOperationTimeDesc(Long projectId, Long userId, Pageable pageable);

    /**
     * 根据用户ID查询所有任务操作日志（分页）
     *
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 操作日志分页列表
     */
    Page<TaskOperationLog> findByUserIdOrderByOperationTimeDesc(Long userId, Pageable pageable);

    /**
     * 根据项目ID和操作类型查询操作日志（分页）
     *
     * @param projectId 项目ID
     * @param operationType 操作类型
     * @param pageable 分页参数
     * @return 操作日志分页列表
     */
    Page<TaskOperationLog> findByProjectIdAndOperationTypeOrderByOperationTimeDesc(Long projectId, TaskOperationType operationType, Pageable pageable);


    /**
     * 根据时间范围查询任务操作日志（分页）
     *
     * @param projectId 项目ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 操作日志分页列表
     */
    @Query("SELECT t FROM TaskOperationLog t WHERE t.projectId = :projectId " +
                  "AND t.operationTime BETWEEN :startTime AND :endTime ORDER BY t.operationTime DESC")
    Page<TaskOperationLog> findByProjectIdAndOperationTimeBetween(@Param("projectId") Long projectId,
                                                                  @Param("startTime") LocalDateTime startTime,
                                                                  @Param("endTime") LocalDateTime endTime,
                                                                  Pageable pageable);

    /**
     * 统计项目任务操作次数
     *
     * @param projectId 项目ID
     * @return 操作次数
     */
    long countByProjectId(Long projectId);

    /**
     * 统计用户在项目中的任务操作次数
     *
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return 操作次数
     */
    long countByProjectIdAndUserId(Long projectId, Long userId);
}
