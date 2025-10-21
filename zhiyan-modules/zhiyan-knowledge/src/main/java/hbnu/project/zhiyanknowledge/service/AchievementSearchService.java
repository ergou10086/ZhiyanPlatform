package hbnu.project.zhiyanknowledge.service;


import hbnu.project.zhiyanknowledge.model.dto.AchievementDTO;
import hbnu.project.zhiyanknowledge.model.dto.AchievementQueryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
}
