package hbnu.project.zhiyanknowledge.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 成果类型枚举
 *
 * @author ErgouTree
 */
@Getter
@AllArgsConstructor
public enum AchievementType {
    /**
     * 论文
     */
    PAPER("paper", "论文"),

    /**
     * 专利
     */
    PATENT("patent", "专利"),

    /**
     * 著作
     */
    MONOGRAPH("monograph", "专著"),

    /**
     * 数据集
     */
    DATASET("dataset", "数据集"),

    /**
     * 模型
     */
    MODEL("model", "算法模型"),

    /**
     * 数据
     */
    DATA("data", "数据"),

    /**
     * 报告
     */
    REPORT("report", "报告"),

    /**
     * 自定义
     */
    CUSTOM("custom", "自定义成果");


    /**
     * 数据库存储值（与表中ENUM类型对应）
     */
    private final String code;

    /**
     * 中文名称（用于前端展示）
     */
    private final String name;


    /**
     * 序列化时返回code（前端交互时使用）
     */
    @JsonValue
    public String getCode() {
        return code;
    }

    /**
     * code到枚举的映射
     */
    private static final Map<String, AchievementType> CODE_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(AchievementType::getCode, e -> e));

    /**
     * 根据code获取枚举实例（用于数据库查询后转换）
     *
     * @param code 数据库存储的code值
     * @return 对应的枚举实例，若不存在则返回null
     */
    public static AchievementType getByCode(String code) {
        return CODE_MAP.get(code);
    }

    /**
     * 检查code是否有效
     *
     * @param code 待校验的code
     * @return 有效返回true，否则false
     */
    public static boolean isValidCode(String code) {
        return CODE_MAP.containsKey(code);
    }

    /**
     * 获取所有枚举的中文名称与code映射（用于前端下拉选择）
     *
     * @return 包含{name: code}的映射表
     */
    public static Map<String, String> getNameCodeMap() {
        return Arrays.stream(values())
                .collect(Collectors.toMap(AchievementType::getName, AchievementType::getCode));
    }
}
