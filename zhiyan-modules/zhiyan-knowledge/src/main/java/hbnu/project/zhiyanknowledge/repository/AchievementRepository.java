package hbnu.project.zhiyanknowledge.repository;

import hbnu.project.zhiyanknowledge.model.entity.Achievement;
import hbnu.project.zhiyanknowledge.model.enums.AchievementStatus;
import hbnu.project.zhiyanknowledge.model.enums.AchievementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 成果数据访问层
 * 提供成果的基础CRUD和业务查询方法
 *
 * @author ErgouTree
 */
@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long>, JpaSpecificationExecutor<Achievement> {

    /**
     * 根据项目ID查询成果文件列表（分页）
     *
     * @param projectId 项目ID
     * @param pageable  分页参数
     * @return 成果分页列表
     */
    Page<Achievement> findByProjectId(Long projectId, Pageable pageable);
    
    /**
     * 根据项目ID查询成果列表（急加载files，避免懒加载异常）
     *
     * @param projectId 项目ID
     * @param pageable  分页参数
     * @return 成果分页列表
     */
    @Query("SELECT DISTINCT a FROM Achievement a LEFT JOIN FETCH a.files WHERE a.projectId = :projectId")
    List<Achievement> findByProjectIdWithFiles(@Param("projectId") Long projectId);

    /**
     * 根据项目ID查询成果列表
     *
     * @param projectId 项目ID
     * @return 成果列表
     */
    List<Achievement> findByProjectId(Long projectId);

    /**
     * 根据项目ID和成果类型查询成果列表（分页）
     *
     * @param projectId 项目ID
     * @param type      成果类型
     * @param pageable  分页参数
     * @return 成果分页列表
     */
    Page<Achievement> findByProjectIdAndType(Long projectId, AchievementType type, Pageable pageable);

    /**
     * 根据项目ID和状态查询成果列表（分页）
     *
     * @param projectId 项目ID
     * @param status    成果状态
     * @param pageable  分页参数
     * @return 成果分页列表
     */
    Page<Achievement> findByProjectIdAndStatus(Long projectId, AchievementStatus status, Pageable pageable);

    /**
     * 根据项目ID、类型和状态查询成果列表（分页）
     *
     * @param projectId 项目ID
     * @param type      成果类型
     * @param status    成果状态
     * @param pageable  分页参数
     * @return 成果分页列表
     */
    Page<Achievement> findByProjectIdAndTypeAndStatus(Long projectId, AchievementType type, 
                                                      AchievementStatus status, Pageable pageable);

    /**
     * 根据创建者ID查询成果列表（分页）
     *
     * @param creatorId 创建者ID
     * @param pageable  分页参数
     * @return 成果分页列表
     */
    Page<Achievement> findByCreatorId(Long creatorId, Pageable pageable);

    /**
     * 根据项目ID和标题模糊查询成果（分页）
     * 绝大多数情况下不使用，因为有es在
     *
     * @param projectId 项目ID
     * @param title     标题关键字
     * @param pageable  分页参数
     * @return 成果分页列表
     */
    Page<Achievement> findByProjectIdAndTitleContaining(Long projectId, String title, Pageable pageable);

    /**
     * 统计项目下的成果数量
     *
     * @param projectId 项目ID
     * @return 成果数量
     */
    long countByProjectId(Long projectId);

    /**
     * 统计项目下指定状态的成果数量
     *
     * @param projectId 项目ID
     * @param status    成果状态
     * @return 成果数量
     */
    long countByProjectIdAndStatus(Long projectId, AchievementStatus status);

    /**
     * 统计项目下指定类型的成果数量
     *
     * @param projectId 项目ID
     * @param type      成果类型
     * @return 成果数量
     */
    long countByProjectIdAndType(Long projectId, AchievementType type);

    /**
     * 查询项目下指定时间范围内创建的成果（分页）
     *
     * @param projectId 项目ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param pageable  分页参数
     * @return 成果分页列表
     */
    @Query("SELECT a FROM Achievement a WHERE a.projectId = :projectId " +
           "AND a.createdAt BETWEEN :startTime AND :endTime")
    Page<Achievement> findByProjectIdAndCreatedAtBetween(@Param("projectId") Long projectId,
                                                          @Param("startTime") LocalDateTime startTime,
                                                          @Param("endTime") LocalDateTime endTime,
                                                          Pageable pageable);

    /**
     * 查询已发布的成果（用于全局搜索和展示）
     * 备用，一般不使用，放着，因为有es
     *
     * @param pageable 分页参数
     * @return 成果分页列表
     */
    @Query("SELECT a FROM Achievement a WHERE a.status = 'published' ORDER BY a.createdAt DESC")
    Page<Achievement> findPublishedAchievements(Pageable pageable);

    /**
     * 批量删除项目下的所有成果
     *
     * @param projectId 项目ID
     */
    @Modifying
    @Transactional
    void deleteByProjectId(Long projectId);

    /**
     * 查询最近更新的成果（用于动态展示）
     *
     * @param limit 限制数量
     * @return 成果列表
     */
    @Query("SELECT a FROM Achievement a ORDER BY a.updatedAt DESC")
    List<Achievement> findRecentlyUpdatedAchievements(Pageable pageable, Integer limit);
}

