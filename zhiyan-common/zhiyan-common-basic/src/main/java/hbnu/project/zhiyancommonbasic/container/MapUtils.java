package hbnu.project.zhiyancommonbasic.container;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Map 工具类
 * 提供从 Map 中安全提取各种类型数据的方法
 *
 * @author ErgouTree
 */
@Slf4j
public class MapUtils {

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
}
