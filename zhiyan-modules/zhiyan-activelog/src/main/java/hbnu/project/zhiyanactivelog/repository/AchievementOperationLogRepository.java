package hbnu.project.zhiyanactivelog.repository;

import hbnu.project.zhiyanactivelog.model.entity.AchievementOperationLog;
import hbnu.project.zhiyanactivelog.model.enums.AchievementOperationType;
import hbnu.project.zhiyanactivelog.model.enums.OperationResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 成果操作日志数据访问层
 *
 * @author ErgouTree
 */
@Repository
public interface AchievementOperationLogRepository {

    /**
     * 根据项目ID查询操作日志（分页）
     *
     * @param projectId 项目ID
     * @param pageable 分页参数
     * @return 操作日志分页列表
     */
    Page<AchievementOperationLog> findByProjectIdOrderByOperationTimeDesc(Long projectId, Pageable pageable);

    /**
     * 根据项目ID和成果ID查询操作日志（分页）
     *
     * @param projectId 项目ID
     * @param achievementId 成果ID
     * @param pageable 分页参数
     * @return 操作日志分页列表
     */
    Page<AchievementOperationLog> findByProjectIdAndAchievementIdOrderByOperationTimeDesc(Long projectId, Long achievementId, Pageable pageable);

    /**
     * 根据项目ID和用户ID查询操作日志（分页）
     *
     * @param projectId 项目ID
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 操作日志分页列表
     */
    Page<AchievementOperationLog> findByProjectIdAndUserIdOrderByOperationTimeDesc(Long projectId, Long userId, Pageable pageable);

    /**
     * 根据用户ID查询所有成果操作日志（分页）
     *
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 操作日志分页列表
     */
    Page<AchievementOperationLog> findByUserIdOrderByOperationTimeDesc(Long userId, Pageable pageable);

    /**
     * 根据项目ID和操作类型查询操作日志（分页）
     *
     * @param projectId 项目ID
     * @param operationType 操作类型
     * @param pageable 分页参数
     * @return 操作日志分页列表
     */
    Page<AchievementOperationLog> findByProjectIdAndOperationTypeOrderByOperationTimeDesc(Long projectId, AchievementOperationType operationType, Pageable pageable);

    /**
     * 根据时间范围查询成果操作日志（分页）
     *
     * @param projectId 项目ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 操作日志分页列表
     */
    @Query("SELECT a FROM AchievementOperationLog a WHERE a.projectId = :projectId " +
            "AND a.operationTime BETWEEN :startTime AND :endTime ORDER BY a.operationTime DESC")
    Page<AchievementOperationLog> findByProjectIdAndOperationTimeBetween(@Param("projectId") Long projectId,
                                                                         @Param("startTime") LocalDateTime startTime,
                                                                         @Param("endTime") LocalDateTime endTime,
                                                                         Pageable pageable);

    /**
     * 统计项目成果操作次数
     *
     * @param projectId 项目ID
     * @return 操作次数
     */
    long countByProjectId(Long projectId);

    /**
     * 统计用户在项目中的成果操作次数
     *
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return 操作次数
     */
    long countByProjectIdAndUserId(Long projectId, Long userId);
}
