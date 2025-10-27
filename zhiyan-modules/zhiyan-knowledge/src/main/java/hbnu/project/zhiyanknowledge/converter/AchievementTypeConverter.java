package hbnu.project.zhiyanknowledge.converter;

import hbnu.project.zhiyanknowledge.model.enums.AchievementType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * 成果类型枚举转换器
 * 用于将 AchievementType 枚举的 code 值与数据库中的字符串值进行转换
 * 
 * @author ErgouTree
 */
@Converter(autoApply = true)
public class AchievementTypeConverter implements AttributeConverter<AchievementType, String> {

    /**
     * 将枚举转换为数据库列值（使用code值）
     */
    @Override
    public String convertToDatabaseColumn(AchievementType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getCode();
    }

    /**
     * 将数据库列值转换为枚举（根据code值查找）
     */
    @Override
    public AchievementType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        
        AchievementType type = AchievementType.getByCode(dbData);
        if (type == null) {
            throw new IllegalArgumentException(
                "Unknown AchievementType code: " + dbData + 
                ". Valid codes are: paper, patent, dataset, model, report, custom");
        }
        return type;
    }
}

