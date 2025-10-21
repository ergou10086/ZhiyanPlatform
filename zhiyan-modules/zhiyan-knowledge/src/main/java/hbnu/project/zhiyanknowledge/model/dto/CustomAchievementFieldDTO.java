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
     * 字段键（英文，用于存储）
     */
    @NotBlank(message = "字段键不能为空")
    private String fieldKey;

    /**
     * 字段名称（中文，用于显示）
     */
    @NotBlank(message = "字段名称不能为空")
    private String fieldLabel;

    /**
     * 字段类型：text, number, date, textarea, select, multiselect
     */
    @NotBlank(message = "字段类型不能为空")
    private String fieldType;

    /**
     * 是否必填
     */
    @Builder.Default
    private Boolean required = false;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 占位符
     */
    private String placeholder;

    /**
     * 选项（当fieldType为select或multiselect时使用）
     */
    private String[] options;

    /**
     * 验证规则（正则表达式）
     */
    private String validationRule;

    /**
     * 提示信息
     */
    private String helpText;

    /**
     * 排序顺序
     */
    private Integer sortOrder;
}
