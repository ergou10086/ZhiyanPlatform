package hbnu.project.zhiyanknowledge.model.dto;

import hbnu.project.zhiyanknowledge.model.enums.AchievementStatus;
import hbnu.project.zhiyanknowledge.model.enums.AchievementType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 更新成果DTO
 * 用于接收前端更新成果的请求数据
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAchievementDTO {

    /**
     * 成果ID（必填）
     */
    @NotNull(message = "成果ID不能为空")
    private Long id;

    /**
     * 成果标题（可选）
     */
    @Size(min = 1, max = 200, message = "成果标题长度必须在1-200字符之间")
    private String title;

    /**
     * 成果类型（可选）
     */
    private AchievementType type;

    /**
     * 成果状态（可选）
     */
    private AchievementStatus status;

    /**
     * 摘要/描述（可选）
     */
    @Size(max = 5000, message = "摘要长度不能超过5000字符")
    private String abstractText;

    /**
     * 标签（可选，逗号分隔）
     */
    @Size(max = 500, message = "标签总长度不能超过500字符")
    private String tags;

    /**
     * 详细信息JSON（支持自定义字段）
     * 传入的Map会与现有数据合并，若要删除某个字段需明确传null
     */
    private Map<String, Object> detailData;

    /**
     * 是否完全替换详细信息（默认false为合并模式，true为替换模式）
     */
    @Builder.Default
    private Boolean replaceDetailData = false;

    /**
     * 版本号（乐观锁，用于并发控制）
     */
    private Integer version;
}

