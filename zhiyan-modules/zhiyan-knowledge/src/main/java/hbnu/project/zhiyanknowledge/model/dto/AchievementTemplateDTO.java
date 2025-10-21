package hbnu.project.zhiyanknowledge.model.dto;

import hbnu.project.zhiyanknowledge.model.enums.AchievementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 成果模板DTO
 * 定义不同类型成果的字段模板
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementTemplateDTO {
    /**
     * 模板ID（可选，用于保存自定义模板）
     */
    private String templateId;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板类型
     */
    private AchievementType type;

    /**
     * 摘要/描述
     */
    private String description;

    /**
     * 字段定义列表
     */
    private List<CustomAchievementFieldDTO> fields;

    /**
     * 是否为系统预设模板，现在没有预设，默认为false，该字段为日后功能扩展
     */
    @Builder.Default
    private Boolean isSystem = false;

    /**
     * 创建者ID（自定义模板）
     */
    private Long creatorId;
}
