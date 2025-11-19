package hbnu.project.zhiyanknowledge.service.impl;

import hbnu.project.zhiyancommonbasic.exception.ServiceException;
import hbnu.project.zhiyanknowledge.message.KnowledgeMessageService;
import hbnu.project.zhiyanknowledge.model.entity.Achievement;
import hbnu.project.zhiyanknowledge.model.enums.AchievementStatus;
import hbnu.project.zhiyanknowledge.repository.AchievementRepository;
import hbnu.project.zhiyanknowledge.service.AchievementFileService;
import hbnu.project.zhiyanknowledge.service.AchievementService;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 成果管理核心服务实现
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementServiceImpl implements AchievementService {

    @Autowired
    private final AchievementRepository achievementRepository;

    @Autowired
    private final AchievementFileService achievementFileService;

    @Autowired
    private final KnowledgeMessageService knowledgeMessageService;

    /**
     * 更新成果状态
     *
     * @param achievementId 成果ID
     * @param status        新状态
     * @param userId        操作用户ID
     */
    @Override
    public void updateAchievementStatus(Long achievementId, AchievementStatus status, Long userId) {
        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ServiceException("成果不存在"));

        AchievementStatus oldStatus = achievement.getStatus();
        if (oldStatus == status) {
            log.info("成果状态未变化: id={}, status={}", achievementId, status);
            return;
        }

        achievement.setStatus(status);
        achievementRepository.save(achievement);

        knowledgeMessageService.notifyAchievementStatusChange(achievement, oldStatus, status, userId);
        log.info("成果状态更新: id={}, newStatus={}", achievementId, status);
    }


    /**
     * 统计项目成果数量
     *
     * @param projectId 项目ID
     * @return 统计信息
     */
    @Override
    public Map<String, Object> getProjectAchievementStats(Long projectId) {
        Map<String, Object> stats = new HashMap<>();

        // 总数
        long totalCount = achievementRepository.countByProjectId(projectId);
        stats.put("totalCount", totalCount);

        // 按状态统计
        Map<AchievementStatus, Long> statusStats = countByStatus(projectId);
        stats.put("byStatus", statusStats);

        // 文件总数
        List<Achievement> achievements = achievementRepository.findByProjectId(projectId);
        long fileCount = achievements.stream()
                .mapToLong(a -> achievementFileService.countFilesByAchievementId(a.getId()))
                .sum();
        stats.put("fileCount", fileCount);

        return stats;
    }

    /**
     * 按状态统计成果数量
     *
     * @param projectId 项目ID
     * @return 状态统计Map
     */
    @Override
    public Map<AchievementStatus, Long> countByStatus(Long projectId) {
        Map<AchievementStatus, Long> result = new HashMap<>();
        for (AchievementStatus status : AchievementStatus.values()) {
            long count = achievementRepository.countByProjectIdAndStatus(projectId, status);
            result.put(status, count);
        }
        return result;
    }
}
