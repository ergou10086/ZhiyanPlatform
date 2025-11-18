package hbnu.project.zhiyanknowledge.repository;

import hbnu.project.zhiyanknowledge.model.entity.AchievementTaskRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 成果-任务关联Repository
 * 
 * 作用说明：
 * 1. 提供成果与任务关联关系的基础CRUD操作
 * 2. 支持按成果ID查询关联的任务列表
 * 3. 支持按任务ID查询关联的成果列表
 * 4. 支持批量查询和删除操作
 * 
 * 注意：
 * - 此Repository只管理关联关系本身，不涉及任务详情的查询
 * - 任务详情需要通过调用项目服务API获取
 * 
 * @author Tokito
 */
@Repository
public interface AchievementTaskRefRepository extends JpaRepository<AchievementTaskRef, Long> {
    
    /**
     * 根据成果ID查询所有关联的任务ID列表
     * 
     * @param achievementId 成果ID
     * @return 关联关系列表
     */
    List<AchievementTaskRef> findByAchievementId(Long achievementId);
    
    /**
     * 根据任务ID查询所有关联的成果ID列表
     * 
     * @param taskId 任务ID
     * @return 关联关系列表
     */
    List<AchievementTaskRef> findByTaskId(Long taskId);
    
    /**
     * 查询特定的关联关系
     * 
     * @param achievementId 成果ID
     * @param taskId 任务ID
     * @return 关联关系（如果存在）
     */
    Optional<AchievementTaskRef> findByAchievementIdAndTaskId(Long achievementId, Long taskId);
    
    /**
     * 删除成果的所有关联关系
     * 
     * @param achievementId 成果ID
     */
    void deleteByAchievementId(Long achievementId);
    
    /**
     * 删除特定的关联关系
     * 
     * @param achievementId 成果ID
     * @param taskId 任务ID
     */
    void deleteByAchievementIdAndTaskId(Long achievementId, Long taskId);
    
    /**
     * 批量查询成果关联的任务ID列表
     * 
     * @param achievementIds 成果ID列表
     * @return 关联关系列表
     */
    @Query("SELECT r FROM AchievementTaskRef r WHERE r.achievementId IN :achievementIds")
    List<AchievementTaskRef> findByAchievementIdIn(@Param("achievementIds") List<Long> achievementIds);
    
    /**
     * 统计成果关联的任务数量
     * 
     * @param achievementId 成果ID
     * @return 关联任务数量
     */
    long countByAchievementId(Long achievementId);
    
    /**
     * 统计任务关联的成果数量
     * 
     * @param taskId 任务ID
     * @return 关联成果数量
     */
    long countByTaskId(Long taskId);
}

