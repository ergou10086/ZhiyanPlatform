package hbnu.project.zhiyanknowledge.service;

import hbnu.project.zhiyanknowledge.model.dto.AchievementDTO;
import hbnu.project.zhiyanknowledge.model.dto.AchievementQueryDTO;
import hbnu.project.zhiyanknowledge.model.dto.PageResultDTO;
import hbnu.project.zhiyanknowledge.model.dto.UpdateAchievementDTO;
import hbnu.project.zhiyanknowledge.model.enums.AchievementStatus;
import hbnu.project.zhiyanknowledge.model.enums.AchievementType;

import java.util.List;
import java.util.Map;

/**
 * 成果主服务接口
 *
 * @author ErgouTree
 */
public interface AchievementService {

    /**
     * 分页查询成果列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResultDTO<AchievementDTO> queryAchievements(AchievementQueryDTO queryDTO);

    /**
     * 根据项目ID查询成果列表
     *
     * @param projectId 项目ID
     * @return 成果列表
     */
    List<AchievementDTO> getAchievementsByProjectId(Long projectId);

    /**
     * 根据创建者ID查询成果列表（分页）
     *
     * @param creatorId 创建者ID
     * @param page      页码
     * @param size      每页数量
     * @return 分页结果
     */
    PageResultDTO<AchievementDTO> getAchievementsByCreatorId(Long creatorId, Integer page, Integer size);

    /**
     * 更新成果基本信息
     *
     * @param updateDTO 更新DTO
     * @param userId    操作用户ID
     * @return 更新后的成果信息
     */
    AchievementDTO updateAchievement(UpdateAchievementDTO updateDTO, Long userId);

    /**
     * 更新成果标题
     *
     * @param achievementId 成果ID
     * @param title         新标题
     * @param userId        操作用户ID
     */
    void updateAchievementTitle(Long achievementId, String title, Long userId);

    /**
     * 更新成果状态
     *
     * @param achievementId 成果ID
     * @param status        新状态
     * @param userId        操作用户ID
     */
    void updateAchievementStatus(Long achievementId, AchievementStatus status, Long userId);

    /**
     * 删除成果
     *
     * @param achievementId 成果ID
     * @param userId        操作用户ID
     */
    void deleteAchievement(Long achievementId, Long userId);

    /**
     * 批量删除成果
     *
     * @param achievementIds 成果ID列表
     * @param userId         操作用户ID
     */
    void batchDeleteAchievements(List<Long> achievementIds, Long userId);

    /**
     * 检查成果是否存在
     *
     * @param achievementId 成果ID
     * @return 是否存在
     */
    boolean existsById(Long achievementId);

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

    /**
     * 按类型统计成果数量
     *
     * @param projectId 项目ID
     * @return 类型统计Map
     */
    Map<AchievementType, Long> countByType(Long projectId);
}
