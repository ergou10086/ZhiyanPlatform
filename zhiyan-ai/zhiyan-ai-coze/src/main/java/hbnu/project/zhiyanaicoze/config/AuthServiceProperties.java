package hbnu.project.zhiyanaicoze.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * zhiyan-auth 服务配置属性
 * 用于配置认证服务的连接信息和降级策略
 *
 * @author ErgouTree
 */
@Data
@Component
@ConfigurationProperties(prefix = "zhiyan-auth")
public class AuthServiceProperties {

    /**
     * 备用服务URL（当Nacos服务发现失败时使用）
     * 格式: http://host:port
     * 示例: http://10.7.92.252:8091
     */
    private String fallbackUrl;

    /**
     * 是否启用服务发现降级（当服务发现失败时使用备用URL）
     * 默认: true
     */
    private boolean enableFallback = true;

    /**
     * 是否启用服务实例URL缓存（缓存上次成功的服务实例URL）
     * 默认: true
     */
    private boolean enableCache = true;
}

