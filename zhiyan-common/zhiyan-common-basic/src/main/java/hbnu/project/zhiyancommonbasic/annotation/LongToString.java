package hbnu.project.zhiyancommonbasic.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import hbnu.project.zhiyancommonbasic.serializer.LongToStringSerializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Long转String注解
 * 用于标记需要将Long类型序列化为String的字段（主要用于ID字段）
 * 
 * 使用示例：
 * @LongToString
 * private Long id;
 *
 * @author asddjv
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = LongToStringSerializer.class)
public @interface LongToString {
}
