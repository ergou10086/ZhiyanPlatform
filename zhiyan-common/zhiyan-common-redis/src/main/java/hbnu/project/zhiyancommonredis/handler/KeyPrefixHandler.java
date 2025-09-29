package hbnu.project.zhiyancommonredis.handler;


import hbnu.project.zhiyancommonbasic.utils.StringUtils;

/**
 * Redis缓存Key前缀处理器（不依赖Redisson）
 * 用于统一给缓存Key添加/去除前缀，区分不同业务或环境
 *
 * @author yui
 * @rewrite ErgouTree
 */
public class KeyPrefixHandler {

    private final String keyPrefix;

    /**
     * 构造方法，初始化前缀
     * @param keyPrefix 前缀字符串，为空则使用空前缀
     */
    public KeyPrefixHandler(String keyPrefix) {
        // 前缀为空时使用空字符串，否则自动添加分隔符":"
        this.keyPrefix = StringUtils.isBlank(keyPrefix) ? "" : keyPrefix + ":";
    }

    /**
     * 给Key添加前缀
     * @param name 原始Key
     * @return 带前缀的Key
     */
    public String addPrefix(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        // 已包含前缀则直接返回，避免重复添加
        if (StringUtils.isNotBlank(keyPrefix) && !name.startsWith(keyPrefix)) {
            return keyPrefix + name;
        }
        return name;
    }

    /**
     * 从Key中去除前缀
     * @param name 带前缀的Key
     * @return 原始Key
     */
    public String removePrefix(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        // 包含前缀时才去除，否则直接返回
        if (StringUtils.isNotBlank(keyPrefix) && name.startsWith(keyPrefix)) {
            return name.substring(keyPrefix.length());
        }
        return name;
    }

    // 可选：添加getter方法，方便获取当前前缀
    public String getKeyPrefix() {
        return keyPrefix;
    }
}