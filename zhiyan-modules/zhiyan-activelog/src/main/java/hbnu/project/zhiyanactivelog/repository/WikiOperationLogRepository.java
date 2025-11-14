package hbnu.project.zhiyanactivelog.repository;

import hbnu.project.zhiyanactivelog.model.entity.WikiOperationLog;
import hbnu.project.zhiyanactivelog.model.enums.WikiOperationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Wiki操作日志数据访问层
 *
 * @author ErgouTree
 */
@Repository
public interface WikiOperationLogRepository extends JpaRepository<WikiOperationLog, Long>, JpaSpecificationExecutor<WikiOperationLog> {

    /**
     * 根据项目ID查询操作日志（分页）
     *
     * @param projectId 项目ID
     * @param pageable 分页参数
     * @return 操作日志分页列表
     */
    Page<WikiOperationLog> findByProjectIdOrderByOperationTimeDesc(Long projectId, Pageable pageable);

    /**
     * 根据项目ID和Wiki页面ID查询操作日志（分页）
     *
     * @param projectId 项目ID
     * @param wikiPageId Wiki页面ID
     * @param pageable 分页参数
     * @return 操作日志分页列表
     */
    Page<WikiOperationLog> findByProjectIdAndWikiPageIdOrderByOperationTimeDesc(Long projectId, Long wikiPageId, Pageable pageable);

    /**
     * 根据项目ID和用户ID查询操作日志（分页）
     *
     * @param projectId 项目ID
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 操作日志分页列表
     */
    Page<WikiOperationLog> findByProjectIdAndUserIdOrderByOperationTimeDesc(Long projectId, Long userId, Pageable pageable);

    /**
     * 根据用户ID查询所有Wiki操作日志（分页）
     *
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 操作日志分页列表
     */
    Page<WikiOperationLog> findByUserIdOrderByOperationTimeDesc(Long userId, Pageable pageable);

    /**
     * 根据项目ID和操作类型查询操作日志（分页）
     *
     * @param projectId 项目ID
     * @param operationType 操作类型
     * @param pageable 分页参数
     * @return 操作日志分页列表
     */
    Page<WikiOperationLog> findByProjectIdAndOperationTypeOrderByOperationTimeDesc(Long projectId, WikiOperationType operationType, Pageable pageable);

    /**
     * 根据时间范围查询Wiki操作日志（分页）
     *
     * @param projectId 项目ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 操作日志分页列表
     */
    @Query("SELECT w FROM WikiOperationLog w WHERE w.projectId = :projectId " +
            "AND w.operationTime BETWEEN :startTime AND :endTime ORDER BY w.operationTime DESC")
    Page<WikiOperationLog> findByProjectIdAndOperationTimeBetween(@Param("projectId") Long projectId,
                                                                  @Param("startTime") LocalDateTime startTime,
                                                                  @Param("endTime") LocalDateTime endTime,
                                                                  Pageable pageable);

    /**
     * 统计项目Wiki操作次数
     *
     * @param projectId 项目ID
     * @return 操作次数
     */
    long countByProjectId(Long projectId);

    /**
     * 统计用户在项目中的Wiki操作次数
     *
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return 操作次数
     */
    long countByProjectIdAndUserId(Long projectId, Long userId);
}
