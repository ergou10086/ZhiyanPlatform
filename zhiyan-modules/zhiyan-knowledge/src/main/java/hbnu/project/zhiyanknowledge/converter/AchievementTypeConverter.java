package hbnu.project.zhiyanknowledge.converter;

import hbnu.project.zhiyanknowledge.model.enums.AchievementType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * AchievementType枚举转换器
 * 用于将枚举的code值（小写）与数据库ENUM类型进行映射
 *
 * @author ErgouTree
 */
@Converter(autoApply = false)
public class AchievementTypeConverter implements AttributeConverter<AchievementType, String> {

    /**
     * 将枚举转换为数据库列值（使用code值）
     *
     * @param attribute 枚举对象
     * @return 数据库存储的字符串（code值，如：paper）
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
     *
     * @param dbData 数据库中的字符串（code值，如：paper）
     * @return 对应的枚举对象
     */
    @Override
    public AchievementType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        
        AchievementType type = AchievementType.getByCode(dbData);
        if (type == null) {
            throw new IllegalArgumentException("未知的成果类型: " + dbData);
        }
        
        return type;
    }
}

