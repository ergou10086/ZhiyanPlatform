package hbnu.project.zhiyancommonswagger.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Knife4j 自动配置类
 * Knife4j 是 Swagger 的增强工具，提供更美观的文档界面
 * 
 * @author ErgouTree
 */
@Slf4j
@Configuration
@EnableKnife4j
@ConditionalOnClass(EnableKnife4j.class)
@ConditionalOnProperty(prefix = "swagger", name = "enabled", havingValue = "true", matchIfMissing = true)
public class Knife4jAutoConfiguration implements WebMvcConfigurer {

    /**
     * 添加静态资源处理器
     * 确保 Knife4j 的静态资源可以被正确访问
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("配置 Knife4j 静态资源处理器");
        
        // Knife4j 文档资源
        registry.addResourceHandler("/doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        
        // Swagger UI 资源
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springdoc-openapi-ui/");
        
        // OpenAPI 资源
        registry.addResourceHandler("/v3/api-docs/**")
                .addResourceLocations("classpath:/META-INF/resources/");
    }
}

