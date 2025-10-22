package hbnu.project.zhiyancommonbasic.annotation;

import java.lang.annotation.*;

/**
 * 标记当前元素（类/方法/字段）处于弃用状态
 * 此注解用于忽略被标记元素的编译检查和代码审查
 * 被标记的元素将被编译器和静态分析工具忽略
 *
 * @see Deprecated （Java标准弃用注解）
 * @author 树上的二狗
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)  // 改为RUNTIME，以便在运行时也能被检测工具识别
@Documented
@SuppressWarnings({"all"})  // 抑制所有警告
public @interface Disabled {

    /**
     * 弃用原因（必选）
     */
    String reason();

    /**
     * 弃用时间（格式：YYYY-MM-DD）
     */
    String since() default "";

    /**
     * 推荐替代方案（可选）
     */
    String replacement() default "";

    /**
     * 计划移除时间（可选）
     */
    String removalDate() default "";

    /**
     * 是否忽略编译检查（默认为true）
     */
    boolean ignoreCompilerCheck() default true;

    /**
     * 是否忽略代码审查（默认为true）
     */
    boolean ignoreCodeReview() default true;
}