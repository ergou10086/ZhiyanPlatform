package hbnu.project.zhiyancommonswagger.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Swagger 配置属性
 * 
 * @author ErgouTree
 */
@Data
@Component
@ConfigurationProperties(prefix = "swagger")
public class SwaggerProperties {

    /**
     * 是否启用 Swagger
     */
    private Boolean enabled = true;

    /**
     * 标题
     */
    private String title = "智研平台 API 文档";

    /**
     * 描述
     */
    private String description = "智研平台微服务系统 RESTful APIs";

    /**
     * 版本
     */
    private String version = "1.0.0";

    /**
     * 服务条款 URL
     */
    private String termsOfServiceUrl = "";

    /**
     * 联系人信息
     */
    private Contact contact = new Contact();

    /**
     * 许可证信息
     */
    private License license = new License();

    /**
     * 服务器列表
     */
    private List<Server> servers = new ArrayList<>();

    /**
     * 全局安全方案
     */
    private List<SecurityScheme> securitySchemes = new ArrayList<>();

    /**
     * 是否启用认证
     */
    private Boolean authEnabled = true;

    /**
     * 认证 Header 名称
     */
    private String authHeaderName = "Authorization";

    /**
     * 认证方案名称
     */
    private String authSchemeName = "Bearer Authentication";

    /**
     * 认证描述
     */
    private String authDescription = "请输入 JWT Token，格式：Bearer {token}";

    /**
     * 分组配置
     */
    private List<GroupConfig> groups = new ArrayList<>();

    /**
     * 联系人信息
     */
    @Data
    public static class Contact {
        /**
         * 联系人姓名
         */
        private String name = "智研平台开发团队";

        /**
         * 联系人邮箱
         */
        private String email = "support@zhiyan.com";

        /**
         * 联系人 URL
         */
        private String url = "https://github.com/ergou10086/ZhiyanPlatform";
    }

    /**
     * 许可证信息
     */
    @Data
    public static class License {
        /**
         * 许可证名称
         */
        private String name = "Apache 2.0";

        /**
         * 许可证 URL
         */
        private String url = "https://www.apache.org/licenses/LICENSE-2.0";
    }

    /**
     * 服务器信息
     */
    @Data
    public static class Server {
        /**
         * 服务器 URL
         */
        private String url;

        /**
         * 服务器描述
         */
        private String description;
    }

    /**
     * 安全方案
     */
    @Data
    public static class SecurityScheme {
        /**
         * 方案名称
         */
        private String name;

        /**
         * 方案类型（http, apiKey, oauth2, openIdConnect）
         */
        private String type;

        /**
         * HTTP 方案（bearer, basic）
         */
        private String scheme;

        /**
         * Bearer 格式
         */
        private String bearerFormat;

        /**
         * 描述
         */
        private String description;
    }

    /**
     * 分组配置
     */
    @Data
    public static class GroupConfig {
        /**
         * 分组名称
         */
        private String name;

        /**
         * 分组标题
         */
        private String title;

        /**
         * 分组描述
         */
        private String description;

        /**
         * 扫描包路径
         */
        private String basePackage;

        /**
         * 路径匹配规则
         */
        private List<String> pathsToMatch = new ArrayList<>();

        /**
         * 排除路径
         */
        private List<String> pathsToExclude = new ArrayList<>();
    }
}

