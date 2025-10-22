package hbnu.project.zhiyanknowledge.model.dto;

import hbnu.project.zhiyanknowledge.model.enums.AchievementStatus;
import hbnu.project.zhiyanknowledge.model.enums.AchievementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 成果详细信息DTO
 * 用于返回成果的完整信息，包括详情和文件列表
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementDetailDTO {

    /**
     * 成果ID
     */
    private String id;

    /**
     * 所属项目ID
     */
    private String projectId;

    /**
     * 项目名称（需从project服务获取）
     */
    private String projectName;

    /**
     * 成果标题
     */
    private String title;

    /**
     * 成果类型
     */
    private AchievementType type;

    /**
     * 成果类型名称（中文）
     */
    private String typeName;

    /**
     * 成果状态
     */
    private AchievementStatus status;

    /**
     * 创建者ID
     */
    private String creatorId;

    /**
     * 创建者名称（需从auth服务获取）
     */
    private String creatorName;

    /**
     * 摘要/描述
     */
    private String abstractText;

    /**
     * 标签数组
     */
    private List<String> tags;

    /**
     * 详细信息（自定义字段）
     * 根据类型不同，包含不同的字段：
     * - 论文: authors, journal, doi, publishYear 等
     * - 专利: patentNo, inventors, applicationDate 等
     * - 数据集: description, version, format, size 等
     * - 自定义: 用户自定义的任意key-value对
     */
    private Map<String, Object> detailData;

    /**
     * 关联的文件列表
     */
    private List<AchievementFileDTO> files;

    /**
     * 文件数量
     */
    private Integer fileCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 创建人
     */
    private Long createdBy;

    /**
     * 更新人
     */
    private Long updatedBy;

    /**
     * 版本号（乐观锁）
     */
    private Integer version;
}

