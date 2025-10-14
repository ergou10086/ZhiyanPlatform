package hbnu.project.zhiyancommonbasic.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson配置类
 * 解决雪花ID精度丢失问题：将Long类型ID序列化为String
 * 支持Java 8日期时间类型序列化
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
     * 日期时间格式
     */
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "HH:mm:ss";

    /**
     * 全局Long转String配置
     * 将所有Long类型序列化为String，避免JavaScript精度丢失问题
     * 同时支持Java 8日期时间类型
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
        
        // 1. Long转String模块
        SimpleModule longToStringModule = new SimpleModule("LongToStringModule");
        longToStringModule.addSerializer(Long.class, ToStringSerializer.instance);
        longToStringModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        objectMapper.registerModule(longToStringModule);
        
        // 2. Java 8日期时间模块
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        // LocalDateTime
        javaTimeModule.addSerializer(LocalDateTime.class, 
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)));
        javaTimeModule.addDeserializer(LocalDateTime.class, 
                new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)));
        
        // LocalDate
        javaTimeModule.addSerializer(LocalDate.class, 
                new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_PATTERN)));
        javaTimeModule.addDeserializer(LocalDate.class, 
                new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE_PATTERN)));
        
        // LocalTime
        javaTimeModule.addSerializer(LocalTime.class, 
                new LocalTimeSerializer(DateTimeFormatter.ofPattern(TIME_PATTERN)));
        javaTimeModule.addDeserializer(LocalTime.class, 
                new LocalTimeDeserializer(DateTimeFormatter.ofPattern(TIME_PATTERN)));
        
        objectMapper.registerModule(javaTimeModule);
        
        // 3. 禁用将日期序列化为时间戳
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        return objectMapper;
    }

    
    /**
     * 默认ObjectMapper配置
     * 当禁用全局Long转String时使用，只有标注@LongToString注解的字段才会转换
     * 仍然支持Java 8日期时间类型
     *
     * @return 默认的ObjectMapper
     */
    @Bean("defaultObjectMapper")
    @ConditionalOnMissingBean(ObjectMapper.class)
    @ConditionalOnProperty(name = "zhiyan.jackson.long-to-string-global", havingValue = "false")
    public ObjectMapper defaultObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // Java 8日期时间模块
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        // LocalDateTime
        javaTimeModule.addSerializer(LocalDateTime.class, 
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)));
        javaTimeModule.addDeserializer(LocalDateTime.class, 
                new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)));
        
        // LocalDate
        javaTimeModule.addSerializer(LocalDate.class, 
                new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_PATTERN)));
        javaTimeModule.addDeserializer(LocalDate.class, 
                new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE_PATTERN)));
        
        // LocalTime
        javaTimeModule.addSerializer(LocalTime.class, 
                new LocalTimeSerializer(DateTimeFormatter.ofPattern(TIME_PATTERN)));
        javaTimeModule.addDeserializer(LocalTime.class, 
                new LocalTimeDeserializer(DateTimeFormatter.ofPattern(TIME_PATTERN)));
        
        objectMapper.registerModule(javaTimeModule);
        
        // 禁用将日期序列化为时间戳
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        return objectMapper;
    }
}
