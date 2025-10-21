package hbnu.project.zhiyanknowledge.model.dto;

import hbnu.project.zhiyanknowledge.model.enums.AchievementStatus;
import hbnu.project.zhiyanknowledge.model.enums.AchievementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 成果DTO
 * 用于列表展示和简单查询返回
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementDTO {

    /**
     * 成果ID
     */
    private String id;

    /**
     * 所属项目ID
     */
    private String projectId;

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
     * 摘要（截取前200字符用于列表展示）
     */
    private String abstractText;

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
     * 版本号
     */
    private Integer version;
}

