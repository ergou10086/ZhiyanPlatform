package hbnu.project.zhiyancommonbasic.annotation;

import java.lang.annotation.*;

/**
 * 运行时阻止注解
 * 被此注解标记的类或方法在运行时会被阻止执行并弹出日志
 * 被标记的不会被执行，会被AOP拦截
 *
 * @author 树上的二狗
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Blocked {
    /**
     * 阻止原因（必选）
     */
    String reason();

    /**
     * 阻止时间（格式：YYYY-MM-DD）
     */
    String since() default "";

    /**
     * 日志级别
     */
    LogLevel logLevel() default LogLevel.ERROR;

    /**
     * 是否抛出异常（默认为true）
     */
    boolean throwException() default true;

    /**
     * 自定义异常消息
     */
    String exceptionMessage() default "";

    /**
     * 日志级别枚举
     */
    enum LogLevel {
        DEBUG, INFO, WARN, ERROR
    }
}