package hbnu.project.zhiyansentineldashboard.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Sentinel 配置属性
 *
 * @author ErgouTree
 */
@Data
@Component
@ConfigurationProperties(prefix = "sentinel")
public class SentinelProperties {

    /**
     * Dashboard 配置
     */
    private DashboardConfig dashboard = new DashboardConfig();

    /**
     * Transport 配置
     */
    private TransportConfig transport = new TransportConfig();

    /**
     * Nacos 数据源配置
     */
    private DatasourceConfig datasource = new DatasourceConfig();

    @Data
    public static class DashboardConfig {
        /**
         * Sentinel Dashboard 地址
         */
        private String address = "localhost:8858";

        /**
         * 是否启用
         */
        private Boolean enabled = true;
    }

    @Data
    public static class TransportConfig {
        /**
         * 与 Dashboard 通信端口
         */
        private Integer port = 8719;

        /**
         * Dashboard 地址
         */
        private String dashboard = "localhost:8858";
    }

    @Data
    public static class DatasourceConfig {
        /**
         * Nacos 配置
         */
        private NacosConfig nacos = new NacosConfig();
    }

    @Data
    public static class NacosConfig {
        /**
         * Nacos 服务器地址
         */
        private String serverAddr = "localhost:8848";

        /**
         * 命名空间
         */
        private String namespace = "";

        /**
         * 分组ID
         */
        private String groupId = "SENTINEL_GROUP";

        /**
         * 用户名
         */
        private String username = "nacos";

        /**
         * 密码
         */
        private String password = "nacos";
    }
}

