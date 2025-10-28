package hbnu.project.zhiyanknowledge.service;

import hbnu.project.zhiyanknowledge.model.dto.AchievementDTO;
import hbnu.project.zhiyanknowledge.model.enums.AchievementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * 成果主服务接口
 *
 * @author ErgouTree
 */
public interface AchievementService {

    /**
     * 更新成果状态
     *
     * @param achievementId 成果ID
     * @param status        新状态
     * @param userId        操作用户ID
     */
    void updateAchievementStatus(Long achievementId, AchievementStatus status, Long userId);


    /**
     * 统计项目成果数量
     *
     * @param projectId 项目ID
     * @return 统计信息
     */
    Map<String, Object> getProjectAchievementStats(Long projectId);


    /**
     * 按状态统计成果数量
     *
     * @param projectId 项目ID
     * @return 状态统计Map
     */
    Map<AchievementStatus, Long> countByStatus(Long projectId);
}
