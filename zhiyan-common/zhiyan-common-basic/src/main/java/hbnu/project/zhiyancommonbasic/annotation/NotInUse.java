package hbnu.project.zhiyancommonbasic.annotation;

import java.lang.annotation.*;

/**
 * 此注解不同于@Deprecated， 注解不会触发编译警告或错误
 * 标记暂时不使用的类、方法或字段等
 * @author 树上的二狗
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.CONSTRUCTOR})
// 仅保留在源代码中，编译时会被丢弃
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface NotInUse {
    /**
     * 不使用的原因说明
     */
    String reason() default "Temporarily not in use";

    /**
     * 计划使用的版本/时间
     */
    String plannedFor() default "";
}
