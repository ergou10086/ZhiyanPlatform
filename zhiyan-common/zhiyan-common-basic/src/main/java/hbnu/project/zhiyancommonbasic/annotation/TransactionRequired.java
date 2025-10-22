package hbnu.project.zhiyancommonbasic.annotation;

import java.lang.annotation.*;

/**
 * 事务控制注解，标记需要事务支持的方法
 * 比Spring的@Transactional更灵活，可自定义事务处理逻辑
 * @author 树上的二狗
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TransactionRequired {
    /**
     * 事务名称，用于日志和监控
     */
    String name() default "";

    /**
     * 事务超时时间(秒)
     */
    int timeout() default 30;

    /**
     * 事务传播行为
     */
    Propagation propagation() default Propagation.REQUIRED;

    /**
     * 遇到异常是否回滚
     */
    Class<? extends Throwable>[] rollbackFor() default {};

    /**
     * 遇到异常不回滚
     */
    Class<? extends Throwable>[] noRollbackFor() default {};

    /**
     * 事务传播行为枚举
     */
    enum Propagation {
        REQUIRED,       // 必须在事务中执行
        SUPPORTS,       // 支持当前事务，如无事务则以非事务方式执行
        MANDATORY,      // 必须在当前事务中执行，否则抛异常
        REQUIRES_NEW,   // 创建新事务，挂起当前事务
        NOT_SUPPORTED,  // 以非事务方式执行，挂起当前事务
        NEVER           // 以非事务方式执行，如有事务则抛异常
    }
}
