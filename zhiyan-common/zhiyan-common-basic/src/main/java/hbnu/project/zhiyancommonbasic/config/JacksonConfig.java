package hbnu.project.zhiyancommonbasic.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Jackson配置类
 * 解决雪花ID精度丢失问题：将Long类型ID序列化为String
 * 
 * 提供两种配置方式：
 * 1. 全局配置：所有Long类型都序列化为String（默认启用）
 * 2. 注解配置：只有使用@LongToString注解的字段才序列化为String
 * 
 * 可通过配置项 zhiyan.jackson.long-to-string-global=false 来禁用全局配置
 *
 * @author ErgouTree
 */
@Configuration
public class JacksonConfig {

    /**
     * 全局Long转String配置
     * 将所有Long类型序列化为String，避免JavaScript精度丢失问题
     * 
     * 可通过配置项 zhiyan.jackson.long-to-string-global=false 来禁用
     *
     * @return 配置好的ObjectMapper
     */
    @Bean("globalLongToStringObjectMapper")
    @Primary
    @ConditionalOnMissingBean(ObjectMapper.class)
    @ConditionalOnProperty(name = "zhiyan.jackson.long-to-string-global", havingValue = "true", matchIfMissing = true)
    public ObjectMapper globalLongToStringObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // 创建自定义模块
        SimpleModule simpleModule = new SimpleModule("LongToStringModule");
        
        // 将Long类型和long基本类型都序列化为String
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        
        // 注册模块
        objectMapper.registerModule(simpleModule);
        
        return objectMapper;
    }

    
    /**
     * 默认ObjectMapper配置
     * 当禁用全局Long转String时使用，只有标注@LongToString注解的字段才会转换
     *
     * @return 默认的ObjectMapper
     */
    @Bean("defaultObjectMapper")
    @Primary
    @ConditionalOnMissingBean(ObjectMapper.class)
    @ConditionalOnProperty(name = "zhiyan.jackson.long-to-string-global", havingValue = "false")
    public ObjectMapper defaultObjectMapper() {
        return new ObjectMapper();
    }
}
