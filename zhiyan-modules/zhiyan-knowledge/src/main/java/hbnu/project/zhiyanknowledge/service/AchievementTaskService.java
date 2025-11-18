package hbnu.project.zhiyanknowledge.service;

import hbnu.project.zhiyanknowledge.model.dto.TaskResultTaskRefDTO;

import java.util.List;
import java.util.Map;

/**
 * 成果-任务关联服务
 * 负责管理成果与任务的关联关系（应用层关联）
 * 
 * 职责说明：
 * 1. 管理成果与任务的关联关系（存储在achievement_task_ref表）
 * 2. 通过调用项目服务API获取任务详情
 * 3. 支持批量关联和查询
 * 
 * @author Tokito
 */
public interface AchievementTaskService {
    
    /**
     * 关联任务到成果
     * 在创建成果时或后续编辑时关联任务
     * 
     * @param achievementId 成果ID
     * @param taskIds 任务ID列表
     * @param userId 操作用户ID
     */
    void linkTasksToAchievement(Long achievementId, List<Long> taskIds, Long userId);
    
    /**
     * 取消关联任务
     * 
     * @param achievementId 成果ID
     * @param taskIds 任务ID列表
     * @param userId 操作用户ID
     */
    void unlinkTasksFromAchievement(Long achievementId, List<Long> taskIds, Long userId);
    
    /**
     * 获取成果关联的任务列表（带详细信息）
     * 通过调用项目服务API获取任务详情
     * 
     * @param achievementId 成果ID
     * @return 任务列表（包含任务详情）
     */
    List<TaskResultTaskRefDTO> getLinkedTasks(Long achievementId);
    
    /**
     * 获取任务关联的成果ID列表
     * 
     * @param taskId 任务ID
     * @return 成果ID列表
     */
    List<Long> getLinkedAchievements(Long taskId);
    
    /**
     * 批量获取任务关联的成果ID列表
     * 
     * @param taskIds 任务ID列表
     * @return Map<任务ID, 成果ID列表>
     */
    Map<Long, List<Long>> getLinkedAchievementsBatch(List<Long> taskIds);
}




