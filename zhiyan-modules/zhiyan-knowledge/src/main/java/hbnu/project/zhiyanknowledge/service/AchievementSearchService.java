package hbnu.project.zhiyanknowledge.service;

import hbnu.project.zhiyanknowledge.model.dto.AchievementDTO;
import hbnu.project.zhiyanknowledge.model.dto.AchievementFileDTO;
import hbnu.project.zhiyanknowledge.model.dto.AchievementQueryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 成果文件服务接口
 * 提供成果文件的搜索功能
 *
 * @author ErgouTree
 */
public interface AchievementSearchService {

    /**
     * 分页查询成果列表
     *
     * @param queryDTO 查询条件
     * @param pageable 分页参数
     * @return 成果分页列表
     */
    Page<AchievementDTO> queryAchievements(AchievementQueryDTO queryDTO, Pageable pageable);

    /**
     * 根据项目ID查询成果列表
     *
     * @param projectId 项目ID
     * @param pageable  分页参数
     * @return 成果分页列表
     */
    Page<AchievementDTO> getAchievementsByProjectId(Long projectId, Pageable pageable);

    /**
     * 根据成果名模糊查询成果
     *
     * @param achievementName 成果名
     * @return 成果
     */
    AchievementDTO getAchievementByName(String achievementName);

    /**
     * 根据成果中的文件名模糊查询文件
     *
     * @param achievementFileName 文件名
     * @return 文件
     */
    AchievementFileDTO getAchievementFileByName(String achievementFileName);

    /**
     * 统计查询：按类型统计成果数量
     *
     * @param projectId 项目ID
     * @return 统计结果Map
     */
    Map<String, Long> statisticsByType(Long projectId);

    /**
     * 根据创建时间范围查询成果
     *
     * @param projectId 项目ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param pageable  分页参数
     * @return 成果分页列表
     */
    Page<AchievementDTO> searchByDateRange(Long projectId,
                                           LocalDateTime startTime,
                                           LocalDateTime endTime,
                                           Pageable pageable);


    /**
     * 组合搜索：多关键字搜索
     *
     * @param keyword  搜索关键字
     * @param pageable 分页参数
     * @return 成果分页列表
     */
    public Page<AchievementDTO> combinationSearch(String keyword, Pageable pageable);
}
