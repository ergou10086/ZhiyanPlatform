package hbnu.project.zhiyanknowledge.repository;

import hbnu.project.zhiyanknowledge.model.entity.AchievementDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 成果详情数据访问层
 * 处理成果详细信息的存储和查询，包括自定义字段的JSON数据
 *
 * @author ErgouTree
 */
@Repository
public interface AchievementDetailRepository extends JpaRepository<AchievementDetail, Long>,
        JpaSpecificationExecutor<AchievementDetail> {

    /**
     * 根据成果ID查询详情，用于前端展示
     *
     * @param achievementId 成果ID
     * @return 成果详情
     */
    Optional<AchievementDetail> findByAchievementId(Long achievementId);

    /**
     * 检查成果是否存在其详情信息，用于避免悬空成果
     *
     * @param achievementId 成果ID
     * @return 是否存在
     */
    boolean existsByAchievementId(Long achievementId);

    /**
     * 删除成果详情
     * @param achievementId 成果ID
     * @return 删除的记录数
     */
    @Modifying
    @Transactional
    int deleteByAchievementId(Long achievementId);

    /**
     * 批量查询成果详情
     *
     * @param achievementIds 成果ID列表
     * @return 成果详情列表
     */
    @Query("SELECT ad FROM AchievementDetail ad WHERE ad.achievementId IN :achievementIds")
    List<AchievementDetail> findByAchievementIdIn(@Param("achievementIds") List<Long> achievementIds);
}

