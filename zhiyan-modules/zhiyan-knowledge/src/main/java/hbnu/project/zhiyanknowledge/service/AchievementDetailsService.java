package hbnu.project.zhiyanknowledge.service;

import hbnu.project.zhiyanknowledge.model.dto.AchievementDetailDTO;
import hbnu.project.zhiyanknowledge.model.dto.AchievementTemplateDTO;
import hbnu.project.zhiyanknowledge.model.dto.UpdateDetailDataDTO;
import hbnu.project.zhiyanknowledge.model.enums.AchievementType;

import java.util.List;
import java.util.Map;

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
     * 创建自定义模板（用于custom类型）
     * 用户可以定义自己的字段结构
     *
     * @param templateDTO 模板定义
     * @return 创建的模板
     */
    AchievementTemplateDTO createCustomTemplate(AchievementTemplateDTO templateDTO);

    /**
     * 更新成果详情数据
     * 允许更新JSON字段和摘要
     *
     * @param updateDTO 更新DTO
     * @return 更新后的详情
     */
    AchievementDetailDTO updateDetailData(UpdateDetailDataDTO updateDTO);

    /**
     * 批量更新详情字段
     * 部分更新，只更新传入的字段
     *
     * @param achievementId 成果ID
     * @param fieldUpdates  字段更新Map
     * @param userId        操作用户ID
     * @return 更新后的详情
     */
    AchievementDetailDTO updateDetailFields(Long achievementId, Map<String, Object> fieldUpdates, Long userId);

    /**
     * 根据模板初始化详情数据
     * 为新成果创建初始的JSON数据结构
     *
     * @param achievementId 成果ID
     * @param type          成果类型
     * @param initialData   初始数据（可选）
     * @return 初始化后的详情
     */
    AchievementDetailDTO initializeDetailByTemplate(Long achievementId, AchievementType type, Map<String, Object> initialData);

    /**
     * 验证详情数据的合法性
     * 根据成果类型验证必填字段和数据格式
     *
     * @param achievementId 成果ID
     * @param detailData    详情数据
     * @return 验证结果（true=通过，false=不通过）
     */
    boolean validateDetailData(Long achievementId, Map<String, Object> detailData);

    /**
     * 获取所有预设模板
     *
     * @return 所有类型的模板列表
     */
    List<AchievementTemplateDTO> getAllSystemTemplates();

    /**
     * 获取成果类型的预设模板
     * 根据成果类型返回推荐的字段模板
     *
     * @param type 成果类型
     * @return 模板定义
     */
    AchievementTemplateDTO getTemplateByType(AchievementType type);

    /**
     * 更新摘要
     *
     * @param achievementId 成果ID
     * @param abstractText  摘要内容
     */
    void updateAbstract(Long achievementId, String abstractText);
}
