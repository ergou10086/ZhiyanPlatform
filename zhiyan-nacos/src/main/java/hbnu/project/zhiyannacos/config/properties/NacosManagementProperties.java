package hbnu.project.zhiyannacos.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Nacos 管理配置属性（用于 Nacos 配置管理模块）
 * yui：不要与 Spring Cloud Alibaba Nacos 的配置类冲突
 *
 * @author ErgouTree
 */
@Data
@Component
@ConfigurationProperties(prefix = "nacos.management")
public class NacosManagementProperties {

    /**
     * Nacos 服务器地址
     */
    private String serverAddr = "127.0.0.1:8848";

    /**
     * 命名空间
     */
    private String namespace = "public";

    /**
     * 分组
     */
    private String group = "DEFAULT_GROUP";

    /**
     * 用户名
     */
    private String username = "nacos";

    /**
     * 密码
     */
    private String password = "nacos";

    /**
     * 配置备份目录
     */
    private String backupDir = "./nacos-backup";

    /**
     * 是否启用配置备份
     */
    private Boolean backupEnabled = true;

    /**
     * 备份cron表达式（每天凌晨2点）
     */
    private String backupCron = "0 0 2 * * ?";

    /**
     * 配置变更监听器开关
     */
    private Boolean listenerEnabled = true;

    /**
     * 配置加密密钥
     */
    private String encryptionKey = "zhiyan-platform-secret-key";
}
