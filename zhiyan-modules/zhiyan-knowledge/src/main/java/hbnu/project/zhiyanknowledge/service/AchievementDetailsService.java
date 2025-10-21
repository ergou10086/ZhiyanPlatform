package hbnu.project.zhiyanknowledge.service;

import hbnu.project.zhiyanknowledge.model.dto.AchievementDetailDTO;

/**
 * 成果的自定义类型服务
 * 用户可以自定义成果的类型，然后写入到成果的详情表中
 * 还有一些和成果的详情相关的内容也在这里
 *
 * @author ErgouTree
 */
public interface AchievementDetailsService {

    /**
     * 获取成果详情
     * 包含主表、详情、文件列表
     *
     * @param achievementId 成果ID
     * @return 成果完整信息
     */
    AchievementDetailDTO getAchievementDetail(Long achievementId);

    /**
     * 创建一种新的自定义的成果类型
     */
}
