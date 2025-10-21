package hbnu.project.zhiyanknowledge.service.impl;

import hbnu.project.zhiyanknowledge.mapper.AchievementConverter;
import hbnu.project.zhiyanknowledge.model.dto.AchievementDTO;
import hbnu.project.zhiyanknowledge.model.dto.AchievementFileDTO;
import hbnu.project.zhiyanknowledge.model.dto.AchievementQueryDTO;
import hbnu.project.zhiyanknowledge.model.entity.Achievement;
import hbnu.project.zhiyanknowledge.repository.AchievementRepository;
import hbnu.project.zhiyanknowledge.service.AchievementSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * 成果搜索部分的服务实现
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementSearchServiceImpl implements AchievementSearchService {

    private final AchievementRepository achievementRepository;

    private final AchievementConverter achievementConverter;

    /**
     * 根据项目ID查询成果列表
     *
     * @param projectId 项目ID
     * @param pageable  分页参数
     * @return 成果分页列表
     */
    @Override
    public Page<AchievementDTO> getAchievementsByProjectId(Long projectId, Pageable pageable) {
        Page<Achievement> page = achievementRepository.findByProjectId(projectId, pageable);
        return page.map(achievementConverter::toDTO);
    }

    /**
     * 分页查询成果列表
     *
     * @param queryDTO 查询条件
     * @param pageable 分页参数
     * @return 成果分页列表
     */
    @Override
    public Page<AchievementDTO> queryAchievements(AchievementQueryDTO queryDTO, Pageable pageable) {
        return null;
    }

    /**
     * 根据成果名模糊查询成果
     *
     * @param achievementName 成果名
     * @return 成果
     */
    @Override
    public AchievementDTO getAchievementByName(String achievementName) {
        return null;
    }

    /**
     * 根据成果中的文件名模糊查询文件
     *
     * @param achievementFileName 文件名
     * @return 文件果
     */
    @Override
    public AchievementFileDTO getAchievementFileByName(String achievementFileName) {
        return null;
    }
}
