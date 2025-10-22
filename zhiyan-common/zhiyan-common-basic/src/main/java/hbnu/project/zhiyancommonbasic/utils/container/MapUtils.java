package hbnu.project.zhiyancommonbasic.utils.container;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * Map 工具类
 * 提供从 Map 中安全提取各种类型数据的方法
 *
 * @author ErgouTree
 */
@Slf4j
public class MapUtils {

    // ==================== 新增：Jackson 实例（全局单例，避免重复创建） ====================
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    // 默认日期格式（可根据项目需求调整，如 "yyyy-MM-dd HH:mm:ss"）
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 私有构造函数，防止实例化
     */
    private MapUtils() {
        throw new IllegalStateException("Utility class");
    }

    // ==================== String 相关方法 ====================

    /**
     * 从 Map 中安全获取 String 值
     *
     * @param map Map 对象
     * @param key 键
     * @return String 值，如果不存在则返回 null
     */
    public static String getString(Map<String, Object> map, String key) {
        if (map == null || key == null) {
            return null;
        }
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 从 Map 中安全获取 String 值，如果不存在则返回默认值
     *
     * @param map          Map 对象
     * @param key          键
     * @param defaultValue 默认值
     * @return String 值，如果不存在则返回默认值
     */
    public static String getString(Map<String, Object> map, String key, String defaultValue) {
        String value = getString(map, key);
        return value != null ? value : defaultValue;
    }

    // ==================== Boolean 相关方法 ====================

    /**
     * 从 Map 中安全获取 Boolean 值
     *
     * @param map Map 对象
     * @param key 键
     * @return Boolean 值，如果不存在或无法转换则返回 false
     */
    public static Boolean getBoolean(Map<String, Object> map, String key) {
        return getBoolean(map, key, false);
    }

    /**
     * 从 Map 中安全获取 Boolean 值，如果不存在则返回默认值
     *
     * @param map          Map 对象
     * @param key          键
     * @param defaultValue 默认值
     * @return Boolean 值，如果不存在或无法转换则返回默认值
     */
    public static Boolean getBoolean(Map<String, Object> map, String key, Boolean defaultValue) {
        if (map == null || key == null) {
            return defaultValue;
        }
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        try {
            return Boolean.parseBoolean(value.toString());
        } catch (Exception e) {
            log.warn("无法将 {} 转换为 Boolean: {}", key, value);
            return defaultValue;
        }
    }

    // ==================== Integer 相关方法 ====================

    /**
     * 从 Map 中安全获取 Integer 值
     *
     * @param map Map 对象
     * @param key 键
     * @return Integer 值，如果不存在或无法转换则返回 null
     */
    public static Integer getInteger(Map<String, Object> map, String key) {
        return getInteger(map, key, null);
    }

    /**
     * 从 Map 中安全获取 Integer 值，如果不存在则返回默认值
     *
     * @param map          Map 对象
     * @param key          键
     * @param defaultValue 默认值
     * @return Integer 值，如果不存在或无法转换则返回默认值
     */
    public static Integer getInteger(Map<String, Object> map, String key, Integer defaultValue) {
        if (map == null || key == null) {
            return defaultValue;
        }
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            log.warn("无法将 {} 转换为 Integer: {}", key, value);
            return defaultValue;
        }
    }

    // ==================== Long 相关方法 ====================

    /**
     * 从 Map 中安全获取 Long 值
     *
     * @param map Map 对象
     * @param key 键
     * @return Long 值，如果不存在或无法转换则返回 null
     */
    public static Long getLong(Map<String, Object> map, String key) {
        return getLong(map, key, null);
    }

    /**
     * 从 Map 中安全获取 Long 值，如果不存在则返回默认值
     *
     * @param map          Map 对象
     * @param key          键
     * @param defaultValue 默认值
     * @return Long 值，如果不存在或无法转换则返回默认值
     */
    public static Long getLong(Map<String, Object> map, String key, Long defaultValue) {
        if (map == null || key == null) {
            return defaultValue;
        }
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            log.warn("无法将 {} 转换为 Long: {}", key, value);
            return defaultValue;
        }
    }

    // ==================== Double 相关方法 ====================

    /**
     * 从 Map 中安全获取 Double 值
     *
     * @param map Map 对象
     * @param key 键
     * @return Double 值，如果不存在或无法转换则返回 null
     */
    public static Double getDouble(Map<String, Object> map, String key) {
        return getDouble(map, key, null);
    }

    /**
     * 从 Map 中安全获取 Double 值，如果不存在则返回默认值
     *
     * @param map          Map 对象
     * @param key          键
     * @param defaultValue 默认值
     * @return Double 值，如果不存在或无法转换则返回默认值
     */
    public static Double getDouble(Map<String, Object> map, String key, Double defaultValue) {
        if (map == null || key == null) {
            return defaultValue;
        }
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            log.warn("无法将 {} 转换为 Double: {}", key, value);
            return defaultValue;
        }
    }

    // ==================== BigDecimal 相关方法 ====================

    /**
     * 从 Map 中安全获取 BigDecimal 值
     *
     * @param map Map 对象
     * @param key 键
     * @return BigDecimal 值，如果不存在或无法转换则返回 null
     */
    public static BigDecimal getBigDecimal(Map<String, Object> map, String key) {
        return getBigDecimal(map, key, null);
    }

    /**
     * 从 Map 中安全获取 BigDecimal 值，如果不存在则返回默认值
     *
     * @param map          Map 对象
     * @param key          键
     * @param defaultValue 默认值
     * @return BigDecimal 值，如果不存在或无法转换则返回默认值
     */
    public static BigDecimal getBigDecimal(Map<String, Object> map, String key, BigDecimal defaultValue) {
        if (map == null || key == null) {
            return defaultValue;
        }
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            log.warn("无法将 {} 转换为 BigDecimal: {}", key, value);
            return defaultValue;
        }
    }

    // ==================== Map 检查方法 ====================

    /**
     * 检查 Map 是否为空
     *
     * @param map Map 对象
     * @return 如果 Map 为 null 或为空则返回 true
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * 检查 Map 是否不为空
     *
     * @param map Map 对象
     * @return 如果 Map 不为 null 且不为空则返回 true
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * 检查 Map 中是否包含指定的键
     *
     * @param map Map 对象
     * @param key 键
     * @return 如果包含则返回 true
     */
    public static boolean containsKey(Map<String, Object> map, String key) {
        return map != null && key != null && map.containsKey(key);
    }

    /**
     * 检查 Map 中指定键的值是否不为 null
     *
     * @param map Map 对象
     * @param key 键
     * @return 如果值不为 null 则返回 true
     */
    public static boolean hasValue(Map<String, Object> map, String key) {
        return containsKey(map, key) && map.get(key) != null;
    }

    // ==================== 泛型方法 ====================

    /**
     * 从 Map 中获取指定类型的值
     *
     * @param map   Map 对象
     * @param key   键
     * @param clazz 目标类型
     * @param <T>   泛型类型
     * @return 指定类型的值，如果不存在或类型不匹配则返回 null
     */
    @SuppressWarnings("unchecked")
    public static <T> T getValue(Map<String, Object> map, String key, Class<T> clazz) {
        if (map == null || key == null || clazz == null) {
            return null;
        }
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (clazz.isInstance(value)) {
            return (T) value;
        }
        log.warn("键 {} 的值类型不匹配，期望: {}, 实际: {}", key, clazz.getName(), value.getClass().getName());
        return null;
    }

    /**
     * 从 Map 中获取指定类型的值，如果不存在则返回默认值
     *
     * @param map          Map 对象
     * @param key          键
     * @param clazz        目标类型
     * @param defaultValue 默认值
     * @param <T>          泛型类型
     * @return 指定类型的值，如果不存在或类型不匹配则返回默认值
     */
    public static <T> T getValue(Map<String, Object> map, String key, Class<T> clazz, T defaultValue) {
        T value = getValue(map, key, clazz);
        return value != null ? value : defaultValue;
    }

    // ==================== 新增：Map 转 JSON 相关方法 ====================

    /**
     * 基础方法：将 Map 转换为紧凑格式的 JSON 字符串
     *
     * @param map 待转换的 Map（key 建议为 String，value 为 JSON 可序列化类型）
     * @return 紧凑格式 JSON 字符串；若 Map 为空或转换失败，返回 null
     */
    public static String toJson(Map<?, ?> map) {
        if (isEmpty(map)) {
            log.warn("Map 为空，无法转换为 JSON");
            return null;
        }
        try {
            // 紧凑格式（无缩进、无换行）
            return OBJECT_MAPPER.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.error("Map 转换 JSON 失败，Map: {}", map, e);
            return null;
        }
    }

    /**
     * 格式化方法：将 Map 转换为带缩进的美观 JSON 字符串（便于日志打印/调试）
     *
     * @param map 待转换的 Map
     * @return 带缩进的 JSON 字符串；若 Map 为空或转换失败，返回 null
     */
    public static String toPrettyJson(Map<?, ?> map) {
        if (isEmpty(map)) {
            log.warn("Map 为空，无法转换为格式化 JSON");
            return null;
        }
        try {
            // 启用缩进格式化，禁用默认日期转时间戳（转为可读字符串）
            ObjectMapper prettyMapper = OBJECT_MAPPER.copy()
                    .enable(SerializationFeature.INDENT_OUTPUT)
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    .setDateFormat(new SimpleDateFormat(DEFAULT_DATE_FORMAT));

            return prettyMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.error("Map 转换格式化 JSON 失败，Map: {}", map, e);
            return null;
        }
    }

    /**
     * 自定义方法：指定日期格式，将 Map 转换为 JSON 字符串
     *
     * @param map         待转换的 Map
     * @param dateFormat  自定义日期格式（如 "yyyy-MM-dd HH:mm:ss.SSS"）
     * @param isPretty    是否启用缩进格式化（true=美观格式，false=紧凑格式）
     * @return 自定义格式的 JSON 字符串；若 Map 为空或转换失败，返回 null
     */
    public static String toJsonWithDateFormat(Map<?, ?> map, String dateFormat, boolean isPretty) {
        if (isEmpty(map)) {
            log.warn("Map 为空，无法转换为自定义格式 JSON");
            return null;
        }
        if (dateFormat == null || dateFormat.trim().isEmpty()) {
            log.warn("日期格式为空，使用默认格式: {}", DEFAULT_DATE_FORMAT);
            dateFormat = DEFAULT_DATE_FORMAT;
        }

        try {
            ObjectMapper customMapper = OBJECT_MAPPER.copy()
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    .setDateFormat(new SimpleDateFormat(dateFormat));

            // 根据 isPretty 决定是否启用缩进
            if (isPretty) {
                customMapper.enable(SerializationFeature.INDENT_OUTPUT);
            }
            return customMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.error("Map 转换自定义格式 JSON 失败，Map: {}, 日期格式: {}", map, dateFormat, e);
            return null;
        }
    }

}
