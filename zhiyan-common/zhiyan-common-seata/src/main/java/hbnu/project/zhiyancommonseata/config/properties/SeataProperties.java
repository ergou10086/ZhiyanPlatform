package hbnu.project.zhiyancommonseata.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Seata 配置属性
 *
 * @author ErgouTree
 */
@Data
@Component
@ConfigurationProperties(prefix = "seata")
public class SeataProperties {

    /**
     * 是否启用 Seata
     */
    private boolean enabled = false;

    /**
     * 应用ID
     */
    private String applicationId;

    /**
     * 事务分组名称
     */
    private String txServiceGroup;

    /**
     * Seata Server 地址
     */
    private String serverAddr = "127.0.0.1:8091";

    /**
     * 命名空间
     */
    private String namespace = "";

    /**
     * 事务超时时间（毫秒）
     */
    private int timeout = 60000;
}