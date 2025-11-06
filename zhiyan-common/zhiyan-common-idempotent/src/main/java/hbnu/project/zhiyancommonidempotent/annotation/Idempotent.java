package hbnu.project.zhiyancommonidempotent.annotation;

import hbnu.project.zhiyancommonidempotent.enums.IdempotentType;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 幂等注解
 * 支持多种幂等策略：参数防重、Token机制、自定义key
 *
 * @author yui
 * @rewrite ErgouTree
 */
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    /**
     * 幂等类型，默认基于参数
     */
    IdempotentType type() default IdempotentType.PARAM;

    /**
     * 幂等键的过期时间，默认5秒
     */
    long timeout() default 5;

    /**
     * 时间单位，默认秒
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 提示消息，支持国际化，格式为 {code}
     */
    String message() default "{idempotent.message}";

    /**
     * SpEL表达式，当type为SPEL时使用
     * 例如：#user.id 或 #request.orderId
     */
    String key() default "";

    /**
     * 是否在业务执行后删除key（仅对失败的情况）
     * true: 失败后删除，允许重试
     * false: 不删除，在过期时间内都不允许重复
     */
    boolean deleteOnError() default true;
}

