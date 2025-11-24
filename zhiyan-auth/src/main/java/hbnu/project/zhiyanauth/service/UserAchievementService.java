package hbnu.project.zhiyanauth.service;

import hbnu.project.zhiyanauth.model.dto.UserAchievementDTO;
import hbnu.project.zhiyanauth.model.form.AchievementLinkBody;
import hbnu.project.zhiyanauth.model.form.UpdateAchievementLinkBody;
import hbnu.project.zhiyancommonbasic.domain.R;

import java.util.List;

/**
 * 用户成果关联服务接口
 *
 * @author ErgouTree
 */
public interface UserAchievementService {

    /**
     * 关联学术成果
     *
     * @param userId 用户ID
     * @param linkBody 关联请求
     * @return 关联结果
     */
    R<UserAchievementDTO> linkAchievement(Long userId, AchievementLinkBody linkBody);


    /**
     * 取消关联学术成果
     *
     * @param userId 用户ID
     * @param achievementId 成果ID
     * @return 操作结果
     */
    R<Void> unlinkAchievement(Long userId, Long achievementId);


    /**
     * 更新成果关联信息（排序、备注）
     *
     * @param userId 用户ID
     * @param achievementId 成果ID
     * @param updateBody 更新内容
     * @return 更新结果
     */
    R<UserAchievementDTO> updateAchievementLink(Long userId, Long achievementId, UpdateAchievementLinkBody updateBody);

    /**
     * 查询用户关联的所有成果
     *
     * @param userId 用户ID
     * @return 成果列表
     */
    R<List<UserAchievementDTO>> getUserAllAchievements(Long userId);
}
