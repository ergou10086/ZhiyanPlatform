package hbnu.project.zhiyancommonredis.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置类
 * 配置 Redis 序列化器，支持 Java 8 日期时间类型
 *
 * @author ErgouTree
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 创建支持 Java 8 日期时间的 JSON 序列化器
        GenericJackson2JsonRedisSerializer serializer = createJsonSerializer();

        // 设置 Key 的序列化方式为 String
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        // 设置 Value 的序列化方式为 JSON（支持 Java 8 日期时间）
        redisTemplate.setValueSerializer(serializer);

        // 设置 Hash Key 的序列化方式为 String
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        // 设置 Hash Value 的序列化方式为 JSON（支持 Java 8 日期时间）
        redisTemplate.setHashValueSerializer(serializer);

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * 创建支持 Java 8 日期时间类型的 JSON 序列化器
     */
    private GenericJackson2JsonRedisSerializer createJsonSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // 注册 JavaTimeModule 以支持 LocalDateTime、LocalDate 等 Java 8 日期时间类型
        objectMapper.registerModule(new JavaTimeModule());
        
        // 禁用将日期序列化为时间戳，使用 ISO-8601 格式
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // 启用类型信息，以便反序列化时能正确还原对象类型
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }
}