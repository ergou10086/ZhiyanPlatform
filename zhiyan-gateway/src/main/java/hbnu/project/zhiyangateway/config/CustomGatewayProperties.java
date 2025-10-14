package hbnu.project.zhiyangateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.ArrayList;

/**
 * 自定义gateway参数配置
 *
 * @author asddjv
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "spring.cloud.gateway")
public class CustomGatewayProperties {

    /**
     * 请求日志
     */
    private Boolean requestLog = true;

    /**
     * 认证配置
     */
    private AuthConfig auth = new AuthConfig();

    @Data
    public static class AuthConfig {
        /**
         * 是否启用认证
         */
        private Boolean enabled = true;

        /**
         * 跳过认证的路径列表
         */
        private List<String> skipPaths = new ArrayList<>();

        /**
         * Token自动刷新阈值（秒）
         */
        private Long refreshThreshold = 300L;

        /**
         * 认证失败重试次数
         */
        private Integer maxRetryCount = 3;
    }
}