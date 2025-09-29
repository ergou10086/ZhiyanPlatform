package hbnu.project.zhiyancommonbasic.utils.id;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.system.SystemUtil;
import cn.hutool.core.net.NetUtil;

/**
 * 雪花ID工具类
 * 基于Hutool的Snowflake算法实现
 * 提供分布式环境下的唯一ID生成
 *
 * @author ErgouTree
 * @date 2025-09-23
 */
public class SnowflakeIdUtil {

    /**
     * 默认的雪花ID生成器
     */
    private static volatile Snowflake defaultSnowflake;

    /**
     * 数据中心ID (5位，0-31)
     */
    private static final long DEFAULT_DATACENTER_ID = 1L;

    /**
     * 工作机器ID (5位，0-31)
     */
    private static final long DEFAULT_WORKER_ID = getWorkerId();


    static {
        // 初始化默认雪花ID生成器
        defaultSnowflake = createSnowflake(DEFAULT_DATACENTER_ID, DEFAULT_WORKER_ID);
    }

    /**
     * 获取默认的雪花ID (Long类型)
     *
     * @return 雪花ID
     */
    public static long nextId() {
        return defaultSnowflake.nextId();
    }


    /**
     * 获取默认的雪花ID (String类型)
     *
     * @return 雪花ID字符串
     */
    public static String nextIdStr() {
        return String.valueOf(defaultSnowflake.nextId());
    }


    /**
     * 创建雪花ID生成器
     *
     * @param datacenterId 数据中心ID (0-31)
     * @param workerId 工作机器ID (0-31)
     * @return Snowflake实例
     */
    public static Snowflake createSnowflake(long datacenterId, long workerId) {
        return IdUtil.getSnowflake(workerId, datacenterId);
    }


    /**
     * 使用指定的数据中心ID和工作机器ID生成雪花ID
     *
     * @param datacenterId 数据中心ID (0-31)
     * @param workerId 工作机器ID (0-31)
     * @return 雪花ID
     */
    public static long nextId(long datacenterId, long workerId) {
        return createSnowflake(datacenterId, workerId).nextId();
    }


    /**
     * 使用指定的数据中心ID和工作机器ID生成雪花ID字符串
     *
     * @param datacenterId 数据中心ID (0-31)
     * @param workerId 工作机器ID (0-31)
     * @return 雪花ID字符串
     */
    public static String nextIdStr(long datacenterId, long workerId) {
        return String.valueOf(nextId(datacenterId, workerId));
    }


    /**
     * 解析雪花ID，获取其组成信息
     *
     * @param snowflakeId 雪花ID
     * @return 雪花ID信息
     */
    public static SnowflakeInfo parseSnowflakeId(long snowflakeId) {
        // 雪花ID的位数分配：
        // 1位符号位(固定0) + 41位时间戳 + 5位数据中心ID + 5位工作机器ID + 12位序列号

        long timestamp = (snowflakeId >> 22) + 1288834974657L; // 获取时间戳
        long datacenterId = (snowflakeId >> 17) & 0x1F; // 获取数据中心ID (5位)
        long workerId = (snowflakeId >> 12) & 0x1F; // 获取工作机器ID (5位)
        long sequence = snowflakeId & 0xFFF; // 获取序列号 (12位)

        return new SnowflakeInfo(timestamp, datacenterId, workerId, sequence);
    }


    /**
     * 自动获取工作机器ID
     * 基于本机IP地址的最后一个字节
     *
     * @return 工作机器ID (0-31)
     */
    private static long getWorkerId() {
        try {
            // 获取本机IP地址
            String ipAddress = NetUtil.getLocalhostStr();
            if (ipAddress != null && !ipAddress.isEmpty()) {
                String[] parts = ipAddress.split("\\.");
                if (parts.length == 4) {
                    // 使用IP地址最后一位作为工作机器ID
                    int lastPart = Integer.parseInt(parts[3]);
                    return lastPart & 0x1F; // 确保在0-31范围内
                }
            }
        } catch (Exception e) {
            // 如果获取IP失败，使用进程ID
            return SystemUtil.getCurrentPID() & 0x1F;
        }

        // 默认返回1
        return 1L;
    }


    /**
     * 重置默认雪花ID生成器
     *
     * @param datacenterId 数据中心ID
     * @param workerId 工作机器ID
     */
    public static void resetDefaultSnowflake(long datacenterId, long workerId) {
        synchronized (SnowflakeIdUtil.class) {
            defaultSnowflake = createSnowflake(datacenterId, workerId);
        }
    }

    /**
     * 雪花ID信息类
     */
    public static class SnowflakeInfo {
        private final long timestamp;
        private final long datacenterId;
        private final long workerId;
        private final long sequence;

        public SnowflakeInfo(long timestamp, long datacenterId, long workerId, long sequence) {
            this.timestamp = timestamp;
            this.datacenterId = datacenterId;
            this.workerId = workerId;
            this.sequence = sequence;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public long getDatacenterId() {
            return datacenterId;
        }

        public long getWorkerId() {
            return workerId;
        }

        public long getSequence() {
            return sequence;
        }

        @Override
        public String toString() {
            return "SnowflakeInfo{" +
                    "timestamp=" + timestamp +
                    ", datacenterId=" + datacenterId +
                    ", workerId=" + workerId +
                    ", sequence=" + sequence +
                    '}';
        }
    }
}
