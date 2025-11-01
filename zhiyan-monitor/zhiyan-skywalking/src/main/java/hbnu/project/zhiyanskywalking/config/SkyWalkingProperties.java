package hbnu.project.zhiyanskywalking.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * SkyWalking 配置属性
 *
 * @author ErgouTree
 */
@Data
@Component
@ConfigurationProperties(prefix = "skywalking")
public class SkyWalkingProperties {

    /**
     * OAP 服务器配置
     */
    private OapConfig oap = new OapConfig();

    /**
     * UI 配置
     */
    private UiConfig ui = new UiConfig();

    @Data
    public static class OapConfig {
        /**
         * OAP 服务器地址
         */
        private String address = "http://localhost:11800";

        /**
         * GraphQL 查询端点
         */
        private String graphqlEndpoint = "http://localhost:12800/graphql";
    }

    @Data
    public static class UiConfig {
        /**
         * SkyWalking UI 地址
         */
        private String address = "http://localhost:8080";
    }
}

