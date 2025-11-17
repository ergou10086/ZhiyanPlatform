package hbnu.project.zhiyancommonoauth.config;

import hbnu.project.zhiyancommonoauth.config.properties.OAuth2Properties;
import hbnu.project.zhiyancommonoauth.provider.OAuth2Provider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * OAuth2自动配置类
 * 自动注册OAuth2相关的Bean
 *
 * @author ErgouTree
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(OAuth2Properties.class)
@ComponentScan(basePackages = "hbnu.project.zhiyancommonoauth")
@ConditionalOnProperty(prefix = "zhiyan.oauth2", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OAuth2AutoConfiguration {

    /**
     * RestTemplate Bean 用户 Http 的请求
     */
    @Bean
    public RestTemplate oauth2RestTemplate() {
        return new RestTemplate();
    }

    /**
     * OAuth2ClientImpl 已经通过 @Service 注解自动注册
     * 这里只需要提供 RestTemplate Bean，providers 会通过 List<OAuth2Provider> 自动注入
     */
    @Bean
    public Map<String, OAuth2Provider> oauth2Providers(List<OAuth2Provider> providers) {
        Map<String, OAuth2Provider> providerMap = providers.stream()
                .filter(OAuth2Provider::isEnabled)
                .collect(Collectors.toMap(
                        OAuth2Provider::getProviderName,
                        provider -> provider,
                        (existing, replacement) -> existing
                ));

        log.info("OAuth2模块初始化完成，已启用{}个提供商: {}",
                providerMap.size(), providerMap.keySet());

        return providerMap;
    }
}
