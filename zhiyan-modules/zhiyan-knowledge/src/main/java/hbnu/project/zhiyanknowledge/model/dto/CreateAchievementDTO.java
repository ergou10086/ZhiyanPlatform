package hbnu.project.zhiyanknowledge.model.dto;

import hbnu.project.zhiyanknowledge.model.enums.AchievementStatus;
import hbnu.project.zhiyanknowledge.model.enums.AchievementType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 创建成果DTO
 * 用于接收前端创建成果的请求数据
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAchievementDTO {

    /**
     * 所属项目ID（前端在选择项目的时候会自动对应）
     */
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    /**
     * 成果标题（必填，1-200字符）
     */
    @NotBlank(message = "成果标题不能为空")
    @Size(min = 1, max = 200, message = "成果标题长度必须在1-200字符之间")
    private String title;

    /**
     * 成果类型（必填）
     */
    @NotNull(message = "成果类型不能为空")
    private AchievementType type;

    /**
     * 成果状态（可选，默认为草稿）
     */
    private AchievementStatus status;

    /**
     * 成果的公开性（可选，默认为私有）
     * true: 公开成果，所有人可见
     * false: 项目私有，仅项目成员可见
     */
    @Builder.Default
    private Boolean isPublic = false;

    /**
     * 摘要/描述（可选）
     */
    @Size(max = 5000, message = "摘要长度不能超过5000字符")
    private String abstractText;

//    /**
//     * 标签（可选，逗号分隔）
//     */
//    @Size(max = 500, message = "标签总长度不能超过500字符")
//    private String tags;

    /**
     * 关联的任务ID列表（创建时可一次性关联多个任务）
     */
    private List<Long> linkedTaskIds;

    /**
     * 详细信息JSON（支持自定义字段）
     * 示例：
     * - 论文: {"authors": ["张三", "李四"], "journal": "Nature", "doi": "10.1000/xyz", "publishYear": 2024}
     * - 专利: {"patentNo": "CN123456", "inventors": ["王五"], "applicationDate": "2024-01-01"}
     * - 数据集: {"description": "...", "version": "1.0", "format": "CSV", "size": "100MB"}
     * - 自定义: {"customField1": "value1", "customField2": "value2", ...}
     */
    private Map<String, Object> detailData;

    /**
     * 创建者ID（由后端从上下文获取，前端无需传递）
     */
    private Long creatorId;
}
