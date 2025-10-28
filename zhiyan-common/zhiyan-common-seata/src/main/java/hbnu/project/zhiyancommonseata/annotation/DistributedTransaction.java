package hbnu.project.zhiyancommonseata.annotation;

import org.springframework.core.annotation.AliasFor;
import io.seata.spring.annotation.GlobalTransactional;

import java.lang.annotation.*;

/**
 * 分布式事务注解
 * 封装 Seata 的 @GlobalTransactional，提供更友好的使用方式
 *
 * @author ErgouTree
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@GlobalTransactional
public @interface DistributedTransaction {

    /**
     * 事务超时时间（毫秒）
     */
    @AliasFor(annotation = GlobalTransactional.class)
    int timeoutMills() default 60000;

    /**
     * 事务名称
     */
    @AliasFor(annotation = GlobalTransactional.class)
    String name() default "";

    /**
     * 需要回滚的异常
     */
    @AliasFor(annotation = GlobalTransactional.class)
    Class<? extends Throwable>[] rollbackFor() default {};

    /**
     * 不需要回滚的异常
     */
    @AliasFor(annotation = GlobalTransactional.class)
    String[] noRollbackFor() default {};
}
