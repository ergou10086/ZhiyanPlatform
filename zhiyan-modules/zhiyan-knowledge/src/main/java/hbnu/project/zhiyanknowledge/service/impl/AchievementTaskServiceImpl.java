package hbnu.project.zhiyanknowledge.service.impl;

import hbnu.project.zhiyanknowledge.model.dto.TaskResultTaskRefDTO;
import hbnu.project.zhiyanknowledge.model.entity.AchievementTaskRef;
import hbnu.project.zhiyanknowledge.repository.AchievementTaskRefRepository;
import hbnu.project.zhiyanknowledge.service.AchievementTaskService;
import hbnu.project.zhiyancommonbasic.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 成果-任务关联服务实现
 * 
 * @author Tokito
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementTaskServiceImpl implements AchievementTaskService {

    private final AchievementTaskRefRepository achievementTaskRefRepository;
    
    // TODO: 注入项目服务客户端，用于获取任务详情
    // private final ProjectServiceClient projectServiceClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void linkTasksToAchievement(Long achievementId, List<Long> taskIds, Long userId) {
        log.info("关联任务到成果: achievementId={}, taskIds={}, userId={}", achievementId, taskIds, userId);
        
        if (taskIds == null || taskIds.isEmpty()) {
            log.warn("任务ID列表为空，跳过关联");
            return;
        }
        
        // TODO: 验证成果是否存在
        // TODO: 验证任务是否存在（调用项目服务API）
        // TODO: 验证用户权限（必须是项目成员）
        
        // 批量创建关联关系
        List<AchievementTaskRef> refs = new ArrayList<>();
        for (Long taskId : taskIds) {
            // 检查是否已存在关联
            if (achievementTaskRefRepository.findByAchievementIdAndTaskId(achievementId, taskId).isPresent()) {
                log.debug("任务已关联，跳过: achievementId={}, taskId={}", achievementId, taskId);
                continue;
            }
            
            AchievementTaskRef ref = AchievementTaskRef.builder()
                    .achievementId(achievementId)
                    .taskId(taskId)
                    .build();
            refs.add(ref);
        }
        
        if (!refs.isEmpty()) {
            achievementTaskRefRepository.saveAll(refs);
            log.info("成功关联{}个任务到成果: achievementId={}", refs.size(), achievementId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlinkTasksFromAchievement(Long achievementId, List<Long> taskIds, Long userId) {
        log.info("取消关联任务: achievementId={}, taskIds={}, userId={}", achievementId, taskIds, userId);
        
        if (taskIds == null || taskIds.isEmpty()) {
            log.warn("任务ID列表为空，跳过取消关联");
            return;
        }
        
        // TODO: 验证用户权限
        
        // 批量删除关联关系
        for (Long taskId : taskIds) {
            achievementTaskRefRepository.deleteByAchievementIdAndTaskId(achievementId, taskId);
        }
        
        log.info("成功取消关联{}个任务: achievementId={}", taskIds.size(), achievementId);
    }

    @Override
    public List<TaskResultTaskRefDTO> getLinkedTasks(Long achievementId) {
        log.info("获取成果关联的任务列表: achievementId={}", achievementId);
        
        // 1. 查询关联关系
        List<AchievementTaskRef> refs = achievementTaskRefRepository.findByAchievementId(achievementId);
        if (refs.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 2. 提取任务ID列表
        List<Long> taskIds = refs.stream()
                .map(AchievementTaskRef::getTaskId)
                .collect(Collectors.toList());
        
        // 3. TODO: 调用项目服务API批量获取任务详情
        // List<TaskDTO> tasks = projectServiceClient.getTasksByIds(taskIds);
        
        // 4. TODO: 转换为TaskResultTaskRefDTO
        // return tasks.stream()
        //         .map(this::convertToTaskRefDTO)
        //         .collect(Collectors.toList());
        
        // 临时返回空列表，等待项目服务接口实现
        log.warn("TODO: 需要实现调用项目服务获取任务详情");
        return new ArrayList<>();
    }

    @Override
    public List<Long> getLinkedAchievements(Long taskId) {
        log.info("获取任务关联的成果ID列表: taskId={}", taskId);
        
        List<AchievementTaskRef> refs = achievementTaskRefRepository.findByTaskId(taskId);
        return refs.stream()
                .map(AchievementTaskRef::getAchievementId)
                .collect(Collectors.toList());
    }

    @Override
    public Map<Long, List<Long>> getLinkedAchievementsBatch(List<Long> taskIds) {
        log.info("批量获取任务关联的成果ID列表: taskIds={}", taskIds);
        
        if (taskIds == null || taskIds.isEmpty()) {
            return new HashMap<>();
        }
        
        // 查询所有关联关系
        List<AchievementTaskRef> refs = achievementTaskRefRepository.findAll()
                .stream()
                .filter(ref -> taskIds.contains(ref.getTaskId()))
                .collect(Collectors.toList());
        
        // 按任务ID分组
        Map<Long, List<Long>> result = new HashMap<>();
        for (AchievementTaskRef ref : refs) {
            result.computeIfAbsent(ref.getTaskId(), k -> new ArrayList<>())
                    .add(ref.getAchievementId());
        }
        
        // 确保所有任务ID都在结果中（即使没有关联）
        for (Long taskId : taskIds) {
            result.putIfAbsent(taskId, new ArrayList<>());
        }
        
        return result;
    }
    
    /**
     * 将TaskDTO转换为TaskResultTaskRefDTO
     * TODO: 等待项目服务提供TaskDTO后实现
     */
    // private TaskResultTaskRefDTO convertToTaskRefDTO(TaskDTO task) {
    //     return TaskResultTaskRefDTO.builder()
    //             .id(task.getId())
    //             .title(task.getTitle())
    //             .description(task.getDescription())
    //             .status(task.getStatus())
    //             .priority(task.getPriority())
    //             .projectId(task.getProjectId())
    //             .creatorId(task.getCreatorId())
    //             .build();
    // }
}






