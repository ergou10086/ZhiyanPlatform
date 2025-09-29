package hbnu.project.zhiyancommonredis.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.util.Assert;

import java.nio.charset.Charset;

/**
 * Redis使用Jackson序列化
 * @author yui
 */
public class Jackson2JsonRedisSerializer<T> implements RedisSerializer<T> {

    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 配置Jackson特性
        // 1. 允许反序列化时识别多态类型（类似FastJson的autoType）
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        // 2. 忽略未知属性，提高兼容性
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 3. 空值处理（可选配置）
        objectMapper.setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);
    }

    private final Class<T> clazz;

    public Jackson2JsonRedisSerializer(Class<T> clazz) {
        super();
        Assert.notNull(clazz, "序列化的目标类不能为null");
        this.clazz = clazz;
    }

    @Override
    public byte[] serialize(T t) throws SerializationException {
        if (t == null) {
            return new byte[0];
        }
        try {
            // 将对象序列化为JSON字节数组，包含类型信息
            return objectMapper.writeValueAsBytes(t);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Could not serialize object to JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            // 将JSON字节数组反序列化为指定类型对象
            return objectMapper.readValue(bytes, clazz);
        } catch (Exception e) {
            throw new SerializationException("Could not deserialize JSON bytes: " + e.getMessage(), e);
        }
    }

    // 提供ObjectMapper的getter方法，方便外部自定义配置
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}

