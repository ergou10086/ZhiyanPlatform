package hbnu.project.zhiyancommonswagger.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * OpenAPI 自动配置类
 * 基于 SpringDoc OpenAPI 3.0 规范
 * 
 * @author ErgouTree
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(SwaggerProperties.class)
@ConditionalOnProperty(prefix = "swagger", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OpenApiAutoConfiguration {

    private final SwaggerProperties swaggerProperties;

    /**
     * 创建 OpenAPI 文档配置
     */
    @Bean
    public OpenAPI openAPI() {
        log.info("初始化 OpenAPI 配置: {}", swaggerProperties.getTitle());

        OpenAPI openAPI = new OpenAPI()
                .info(buildInfo())
                .components(buildComponents());

        // 添加服务器信息
        if (!CollectionUtils.isEmpty(swaggerProperties.getServers())) {
            List<Server> servers = new ArrayList<>();
            swaggerProperties.getServers().forEach(serverConfig -> {
                Server server = new Server();
                server.setUrl(serverConfig.getUrl());
                server.setDescription(serverConfig.getDescription());
                servers.add(server);
            });
            openAPI.servers(servers);
        }

        // 添加全局安全要求
        if (swaggerProperties.getAuthEnabled()) {
            SecurityRequirement securityRequirement = new SecurityRequirement();
            securityRequirement.addList(swaggerProperties.getAuthSchemeName());
            openAPI.addSecurityItem(securityRequirement);
        }

        log.info("OpenAPI 配置初始化完成");
        return openAPI;
    }

    /**
     * 构建 API 信息
     */
    private Info buildInfo() {
        Contact contact = new Contact()
                .name(swaggerProperties.getContact().getName())
                .email(swaggerProperties.getContact().getEmail())
                .url(swaggerProperties.getContact().getUrl());

        License license = new License()
                .name(swaggerProperties.getLicense().getName())
                .url(swaggerProperties.getLicense().getUrl());

        return new Info()
                .title(swaggerProperties.getTitle())
                .description(swaggerProperties.getDescription())
                .version(swaggerProperties.getVersion())
                .contact(contact)
                .license(license)
                .termsOfService(swaggerProperties.getTermsOfServiceUrl());
    }

    /**
     * 构建组件配置（安全方案等）
     */
    private Components buildComponents() {
        Components components = new Components();

        // 添加默认的 Bearer Token 认证方案
        if (swaggerProperties.getAuthEnabled()) {
            SecurityScheme securityScheme = new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .name(swaggerProperties.getAuthHeaderName())
                    .description(swaggerProperties.getAuthDescription())
                    .in(SecurityScheme.In.HEADER);

            components.addSecuritySchemes(swaggerProperties.getAuthSchemeName(), securityScheme);
        }

        // 添加自定义安全方案
        if (!CollectionUtils.isEmpty(swaggerProperties.getSecuritySchemes())) {
            swaggerProperties.getSecuritySchemes().forEach(schemeConfig -> {
                SecurityScheme securityScheme = new SecurityScheme();
                
                // 设置类型
                switch (schemeConfig.getType().toLowerCase()) {
                    case "http" -> securityScheme.type(SecurityScheme.Type.HTTP);
                    case "apikey" -> securityScheme.type(SecurityScheme.Type.APIKEY);
                    case "oauth2" -> securityScheme.type(SecurityScheme.Type.OAUTH2);
                    case "openidconnect" -> securityScheme.type(SecurityScheme.Type.OPENIDCONNECT);
                    default -> securityScheme.type(SecurityScheme.Type.HTTP);
                }

                if (schemeConfig.getScheme() != null) {
                    securityScheme.scheme(schemeConfig.getScheme());
                }
                if (schemeConfig.getBearerFormat() != null) {
                    securityScheme.bearerFormat(schemeConfig.getBearerFormat());
                }
                if (schemeConfig.getDescription() != null) {
                    securityScheme.description(schemeConfig.getDescription());
                }

                components.addSecuritySchemes(schemeConfig.getName(), securityScheme);
            });
        }

        return components;
    }

    /**
     * 创建默认分组（当没有配置分组时）
     */
    @Bean
    @ConditionalOnProperty(prefix = "swagger", name = "groups", matchIfMissing = true)
    public GroupedOpenApi defaultGroup() {
        log.info("创建默认 API 分组");
        return GroupedOpenApi.builder()
                .group("default")
                .displayName("默认分组")
                .pathsToMatch("/**")
                .pathsToExclude("/error", "/actuator/**")
                .build();
    }

    /**
     * 根据配置创建自定义分组
     */
    @Bean
    @ConditionalOnProperty(prefix = "swagger", name = "groups")
    public List<GroupedOpenApi> customGroups() {
        List<GroupedOpenApi> groups = new ArrayList<>();

        if (!CollectionUtils.isEmpty(swaggerProperties.getGroups())) {
            swaggerProperties.getGroups().forEach(groupConfig -> {
                log.info("创建自定义 API 分组: {}", groupConfig.getName());

                GroupedOpenApi.Builder builder = GroupedOpenApi.builder()
                        .group(groupConfig.getName())
                        .displayName(groupConfig.getTitle());

                // 设置包扫描路径
                if (groupConfig.getBasePackage() != null) {
                    builder.packagesToScan(groupConfig.getBasePackage());
                }

                // 设置路径匹配规则
                if (!CollectionUtils.isEmpty(groupConfig.getPathsToMatch())) {
                    builder.pathsToMatch(groupConfig.getPathsToMatch().toArray(new String[0]));
                }

                // 设置排除路径
                if (!CollectionUtils.isEmpty(groupConfig.getPathsToExclude())) {
                    builder.pathsToExclude(groupConfig.getPathsToExclude().toArray(new String[0]));
                }

                groups.add(builder.build());
            });
        }

        log.info("自定义 API 分组创建完成，共 {} 个分组", groups.size());
        return groups;
    }
}

