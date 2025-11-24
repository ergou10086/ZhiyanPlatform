package hbnu.project.zhiyanauth.repository;

import hbnu.project.zhiyanauth.model.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户成果关联 Repository
 *
 * @author ErgouTree
 */
@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {

    /**
     * 查询用户关联的所有成果（按展示顺序排序）
     */
    List<UserAchievement> findByUserIdOrderByDisplayOrderAsc(Long userId);

    /**
     * 查询用户在指定项目下的成果关联
     */
    List<UserAchievement> findByUserIdAndProjectIdOrderByDisplayOrderAsc(Long userId, Long projectId);

    /**
     * 检查用户是否已关联某成果
     */
    boolean existsByUserIdAndAchievementId(Long userId, Long achievementId);

    /**
     * 查询用户对某成果的关联记录
     */
    Optional<UserAchievement> findByUserIdAndAchievementId(Long userId, Long achievementId);

    /**
     * 删除用户对某成果的关联
     */
    void deleteByUserIdAndAchievementId(Long userId, Long achievementId);

    /**
     * 统计用户关联的成果数量
     */
    long countByUserId(Long userId);

    /**
     * 查询关联了某成果的所有用户
     */
    List<UserAchievement> findByAchievementId(Long achievementId);
}
