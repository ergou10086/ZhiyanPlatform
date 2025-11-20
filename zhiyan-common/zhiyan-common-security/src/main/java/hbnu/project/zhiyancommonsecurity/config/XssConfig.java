package hbnu.project.zhiyancommonsecurity.config;

import hbnu.project.zhiyancommonsecurity.filter.XssFilter;
import hbnu.project.zhiyancommonsecurity.xss.XssProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * XSS防护配置类
 * 统一管理XSS相关的Bean配置
 *
 * @author ErgouTree
 */
@Configuration
@ConditionalOnProperty(prefix = "security.xss", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(XssProperties.class)
public class XssConfig {

    /**
     * 注册XSS过滤器
     */
    @Bean
    public FilterRegistrationBean<XssFilter> xssFilterRegistration(XssProperties xssProperties) {
        FilterRegistrationBean<XssFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new XssFilter(xssProperties));
        registration.addUrlPatterns("/*");
        registration.setName("xssFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registration;
    }
}
