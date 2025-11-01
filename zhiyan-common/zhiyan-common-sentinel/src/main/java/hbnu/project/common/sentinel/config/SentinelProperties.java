package hbnu.project.common.sentinel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * Sentinel 配置属性
 *
 * @author ErgouTree
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = "zhiyan.sentinel")
public class SentinelProperties {

    /**
     * 是否启用 Sentinel
     */
    private Boolean enabled = true;

    /**
     * 是否开启饥饿加载（立即初始化）
     */
    private Boolean eager = true;

    /**
     * Dashboard 配置
     */
    private Dashboard dashboard = new Dashboard();

    /**
     * 日志配置
     */
    private Log log = new Log();

    /**
     * 全局配置
     */
    private Global global = new Global();

    /**
     * Nacos 数据源配置
     */
    private Nacos nacos = new Nacos();

    @Data
    public static class Dashboard {
        /**
         * Sentinel Dashboard 地址
         */
        private String host = "localhost";

        /**
         * Sentinel Dashboard 端口
         */
        private Integer port = 8858;

        /**
         * 与 Dashboard 通信的端口
         */
        private Integer clientPort = 8719;

        /**
         * 心跳发送周期（毫秒）
         */
        private Long heartbeatIntervalMs = 5000L;

        public String getAddress() {
            return host + ":" + port;
        }
    }

    @Data
    public static class Log {
        /**
         * 日志目录
         */
        private String dir = "logs/sentinel";

        /**
         * 是否使用 PID 作为日志文件名
         */
        private Boolean switchPid = true;
    }

    @Data
    public static class Global {
        /**
         * 统一 Web Context
         */
        private Boolean webContextUnify = false;

        /**
         * 是否记录 HTTP 方法
         */
        private Boolean httpMethodSpecify = true;

        /**
         * 全局流控 QPS 阈值（-1 表示不限制）
         */
        private Integer globalQps = -1;

        /**
         * 全局线程数阈值（-1 表示不限制）
         */
        private Integer globalThread = -1;
    }

    @Data
    public static class Nacos {
        /**
         * 是否启用 Nacos 数据源
         */
        private Boolean enabled = true;

        /**
         * Nacos 服务器地址
         */
        private String serverAddr = "localhost:8848";

        /**
         * Nacos 命名空间
         */
        private String namespace = "";

        /**
         * 分组ID
         */
        private String groupId = "SENTINEL_GROUP";

        /**
         * Nacos 用户名
         */
        private String username = "nacos";

        /**
         * Nacos 密码
         */
        private String password = "nacos";

        /**
         * 数据ID后缀
         */
        private String dataIdSuffix = "json";
    }
}

