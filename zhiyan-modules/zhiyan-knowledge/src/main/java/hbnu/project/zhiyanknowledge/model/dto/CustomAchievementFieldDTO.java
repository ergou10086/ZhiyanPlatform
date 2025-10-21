package hbnu.project.zhiyanknowledge.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 自定义字段DTO
 * 用于定义成果的自定义字段
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomAchievementFieldDTO {
    /**
     * 成果ID
     */
    @NotNull(message = "成果ID不能为空")
    private Long achievementId;

    /**
     * 详细信息（自定义字段的键值对）
     */
    private Map<String, Object> detailData;

    /**
     * 摘要/描述
     */
    private String abstractText;

//    /**
//     * 操作用户ID
//     */
//    private Long userId;
}
