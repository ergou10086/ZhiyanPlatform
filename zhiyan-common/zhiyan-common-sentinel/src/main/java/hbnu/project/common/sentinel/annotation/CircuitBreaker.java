package hbnu.project.common.sentinel.annotation;

import java.lang.annotation.*;

/**
 * 熔断降级注解
 * <p>
 * 简化版的熔断降级注解
 * </p>
 *
 * @author ErgouTree
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CircuitBreaker {

    /**
     * 资源名称，默认为方法全限定名
     */
    String value() default "";

    /**
     * 降级策略
     * 0 - 慢调用比例
     * 1 - 异常比例
     * 2 - 异常数
     */
    int strategy() default 0;

    /**
     * 阈值
     * strategy=0: 慢调用比例（0-1）
     * strategy=1: 异常比例（0-1）
     * strategy=2: 异常数
     */
    double threshold() default 0.5;

    /**
     * 熔断时长（秒）
     */
    int timeWindow() default 10;

    /**
     * 最小请求数
     */
    int minRequestAmount() default 5;

    /**
     * 慢调用 RT 阈值（毫秒）
     */
    int slowRtMs() default 500;

    /**
     * 降级后的提示信息
     */
    String message() default "服务熔断，请稍后重试";
}

