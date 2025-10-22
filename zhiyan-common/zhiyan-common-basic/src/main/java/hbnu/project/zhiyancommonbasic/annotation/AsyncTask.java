package hbnu.project.zhiyancommonbasic.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 异步任务处理注解，标记需要异步执行的方法
 * 比Spring的@Async更强大，支持更多自定义配置
 *
 * @author 树上的二狗
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AsyncTask {
    /**
     * 任务名称，用于日志和监控
     */
    String name() default "";

    /**
     * 线程池名称，为空则使用默认线程池
     */
    String threadPool() default "";

    /**
     * 任务超时时间
     */
    long timeout() default 0;

    /**
     * 超时时间单位
     */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    /**
     * 是否等待任务完成
     */
    boolean waitForTaskEnd() default false;

    /**
     * 任务执行失败时的策略
     */
    FailurePolicy failurePolicy() default FailurePolicy.LOG;

    /**
     * 失败策略枚举
     */
    enum FailurePolicy {
        IGNORE,  // 忽略错误
        LOG,     // 记录日志
        RETRY,   // 重试
        THROW    // 抛出异常
    }
}