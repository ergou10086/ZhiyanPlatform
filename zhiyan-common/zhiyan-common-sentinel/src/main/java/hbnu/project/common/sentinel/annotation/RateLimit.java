package hbnu.project.common.sentinel.annotation;

import java.lang.annotation.*;

/**
 * 限流注解
 * <p>
 * 简化版的限流注解，封装 Sentinel 的 @SentinelResource
 * </p>
 *
 * @author ErgouTree
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 资源名称，默认为方法全限定名
     */
    String value() default "";

    /**
     * QPS 阈值，-1 表示不限制
     */
    int qps() default -1;

    /**
     * 并发线程数阈值，-1 表示不限制
     */
    int thread() default -1;

    /**
     * 限流后的提示信息
     */
    String message() default "访问过于频繁，请稍后再试";

    /**
     * 降级后的提示信息
     */
    String fallbackMessage() default "服务暂时不可用，请稍后重试";
}

