package hbnu.project.zhiyanknowledge.service.impl;

import hbnu.project.zhiyancommonbasic.exception.ServiceException;
import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import hbnu.project.zhiyanknowledge.mapper.AchievementConverter;
import hbnu.project.zhiyanknowledge.model.dto.*;
import hbnu.project.zhiyanknowledge.model.entity.Achievement;
import hbnu.project.zhiyanknowledge.model.enums.AchievementStatus;
import hbnu.project.zhiyanknowledge.model.enums.AchievementType;
import hbnu.project.zhiyanknowledge.repository.AchievementRepository;
import hbnu.project.zhiyanknowledge.service.AchievementDetailsService;
import hbnu.project.zhiyanknowledge.service.AchievementFileService;
import hbnu.project.zhiyanknowledge.service.AchievementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 成果管理核心服务实现
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementServiceImpl implements AchievementService {

    private final AchievementRepository achievementRepository;
    private final AchievementFileService achievementFileService;
    private final AchievementDetailsService achievementDetailsService;
    private final AchievementConverter achievementConverter;


    /**
     * 分页查询成果列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @Override
    public PageResultDTO<AchievementDTO> queryAchievements(AchievementQueryDTO queryDTO) {
        log.info("分页查询成果: projectId={}, type={}, status={}", 
                queryDTO.getProjectId(), queryDTO.getType(), queryDTO.getStatus());

        // 构建分页参数
        Sort sort = buildSort(queryDTO.getSortBy(), queryDTO.getSortOrder());
        Pageable pageable = PageRequest.of(queryDTO.getPage(), queryDTO.getSize(), sort);

        Page<Achievement> achievementPage;

        // 根据不同条件组合查询
        if (queryDTO.getProjectId() != null && queryDTO.getType() != null && queryDTO.getStatus() != null) {
            achievementPage = achievementRepository.findByProjectIdAndTypeAndStatus(
                    queryDTO.getProjectId(), queryDTO.getType(), queryDTO.getStatus(), pageable);
        } else if (queryDTO.getProjectId() != null && queryDTO.getType() != null) {
            achievementPage = achievementRepository.findByProjectIdAndType(
                    queryDTO.getProjectId(), queryDTO.getType(), pageable);
        } else if (queryDTO.getProjectId() != null && queryDTO.getStatus() != null) {
            achievementPage = achievementRepository.findByProjectIdAndStatus(
                    queryDTO.getProjectId(), queryDTO.getStatus(), pageable);
        } else if (queryDTO.getProjectId() != null) {
            achievementPage = achievementRepository.findByProjectId(queryDTO.getProjectId(), pageable);
        } else if (queryDTO.getCreatorId() != null) {
            achievementPage = achievementRepository.findByCreatorId(queryDTO.getCreatorId(), pageable);
        } else if (Boolean.TRUE.equals(queryDTO.getOnlyPublished())) {
            achievementPage = achievementRepository.findPublishedAchievements(pageable);
        } else {
            achievementPage = achievementRepository.findAll(pageable);
        }

        return achievementConverter.toPageDTO(achievementPage);
    }

    /**
     * 根据项目ID查询成果列表
     *
     * @param projectId 项目ID
     * @return 成果列表
     */
    @Override
    public List<AchievementDTO> getAchievementsByProjectId(Long projectId) {
        log.info("查询项目成果列表: projectId={}", projectId);
        List<Achievement> achievements = achievementRepository.findByProjectId(projectId);
        return achievementConverter.toDTOList(achievements);
    }

    /**
     * 根据创建者ID查询成果列表（分页）
     *
     * @param creatorId 创建者ID
     * @param page      页码
     * @param size      每页数量
     * @return 分页结果
     */
    @Override
    public PageResultDTO<AchievementDTO> getAchievementsByCreatorId(Long creatorId, Integer page, Integer size) {
        log.info("查询创建者成果列表: creatorId={}", creatorId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Achievement> achievementPage = achievementRepository.findByCreatorId(creatorId, pageable);
        return achievementConverter.toPageDTO(achievementPage);
    }

    /**
     * 更新成果基本信息
     *
     * @param updateDTO 更新DTO
     * @param userId    操作用户ID
     * @return 更新后的成果信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AchievementDTO updateAchievement(UpdateAchievementDTO updateDTO, Long userId) {
        log.info("更新成果基本信息: achievementId={}, userId={}", updateDTO.getId(), userId);

        // 1. 查询成果
        Achievement achievement = achievementRepository.findById(updateDTO.getId())
                .orElseThrow(() -> new ServiceException("成果不存在"));

        // 2. 更新基本字段
        if (StringUtils.isNotEmpty(updateDTO.getTitle())) {
            achievement.setTitle(updateDTO.getTitle());
        }
        if (updateDTO.getType() != null) {
            achievement.setType(updateDTO.getType());
        }
        if (updateDTO.getStatus() != null) {
            achievement.setStatus(updateDTO.getStatus());
        }

        // 3. 保存成果主表
        achievement = achievementRepository.save(achievement);

        // 4. 如果有摘要更新，更新详情表
        if (StringUtils.isNotEmpty(updateDTO.getAbstractText())) {
            achievementDetailsService.updateAbstract(achievement.getId(), updateDTO.getAbstractText());
        }

        // 5. 如果有详情数据更新
        if (updateDTO.getDetailData() != null && !updateDTO.getDetailData().isEmpty()) {
            UpdateDetailDataDTO detailUpdateDTO = UpdateDetailDataDTO.builder()
                    .achievementId(achievement.getId())
                    .detailData(updateDTO.getDetailData())
                    .build();
            achievementDetailsService.updateDetailData(detailUpdateDTO);
        }

        log.info("成果更新成功: achievementId={}", achievement.getId());
        return achievementConverter.toDTO(achievement);
    }

    /**
     * 更新成果标题
     *
     * @param achievementId 成果ID
     * @param title         新标题
     * @param userId        操作用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAchievementTitle(Long achievementId, String title, Long userId) {
        log.info("更新成果标题: achievementId={}, title={}", achievementId, title);

        if (StringUtils.isEmpty(title)) {
            throw new ServiceException("标题不能为空");
        }

        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ServiceException("成果不存在"));

        achievement.setTitle(title);
        achievementRepository.save(achievement);

        log.info("标题更新成功: achievementId={}", achievementId);
    }

    /**
     * 更新成果状态
     *
     * @param achievementId 成果ID
     * @param status        新状态
     * @param userId        操作用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAchievementStatus(Long achievementId, AchievementStatus status, Long userId) {
        log.info("更新成果状态: achievementId={}, status={}", achievementId, status);

        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ServiceException("成果不存在"));

        achievement.setStatus(status);
        achievementRepository.save(achievement);

        log.info("成果状态更新成功: id={}, newStatus={}", achievementId, status);
    }

    /**
     * 删除成果
     *
     * @param achievementId 成果ID
     * @param userId        操作用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAchievement(Long achievementId, Long userId) {
        log.info("删除成果: achievementId={}, userId={}", achievementId, userId);

        // 1. 检查成果是否存在
        if (!achievementRepository.existsById(achievementId)) {
            throw new ServiceException("成果不存在");
        }

        // 2. 删除成果（会级联删除详情和文件记录）
        achievementRepository.deleteById(achievementId);

        log.info("成果删除成功: achievementId={}", achievementId);
    }

    /**
     * 批量删除成果
     *
     * @param achievementIds 成果ID列表
     * @param userId         操作用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteAchievements(List<Long> achievementIds, Long userId) {
        log.info("批量删除成果: count={}, userId={}", achievementIds.size(), userId);

        if (achievementIds == null || achievementIds.isEmpty()) {
            throw new ServiceException("成果ID列表不能为空");
        }

        // 逐个删除
        for (Long id : achievementIds) {
            if (achievementRepository.existsById(id)) {
                achievementRepository.deleteById(id);
            }
        }

        log.info("批量删除成功: count={}", achievementIds.size());
    }

    /**
     * 检查成果是否存在
     *
     * @param achievementId 成果ID
     * @return 是否存在
     */
    @Override
    public boolean existsById(Long achievementId) {
        return achievementRepository.existsById(achievementId);
    }

    /**
     * 统计项目成果数量
     *
     * @param projectId 项目ID
     * @return 统计信息
     */
    @Override
    public Map<String, Object> getProjectAchievementStats(Long projectId) {
        log.info("统计项目成果: projectId={}", projectId);

        Map<String, Object> stats = new HashMap<>();

        // 总数
        long totalCount = achievementRepository.countByProjectId(projectId);
        stats.put("totalCount", totalCount);

        // 按状态统计
        Map<AchievementStatus, Long> statusStats = countByStatus(projectId);
        stats.put("byStatus", statusStats);

        // 按类型统计
        Map<AchievementType, Long> typeStats = countByType(projectId);
        stats.put("byType", typeStats);

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

    /**
     * 按类型统计成果数量
     *
     * @param projectId 项目ID
     * @return 类型统计Map
     */
    @Override
    public Map<AchievementType, Long> countByType(Long projectId) {
        Map<AchievementType, Long> result = new HashMap<>();
        for (AchievementType type : AchievementType.values()) {
            long count = achievementRepository.countByProjectIdAndType(projectId, type);
            result.put(type, count);
        }
        return result;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 构建排序对象
     *
     * @param sortBy    排序字段
     * @param sortOrder 排序方向
     * @return Sort对象
     */
    private Sort buildSort(String sortBy, String sortOrder) {
        // 默认按创建时间降序
        String field = StringUtils.isNotEmpty(sortBy) ? sortBy : "createdAt";
        Sort.Direction direction = "ASC".equalsIgnoreCase(sortOrder) 
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC;

        return Sort.by(direction, field);
    }
}
