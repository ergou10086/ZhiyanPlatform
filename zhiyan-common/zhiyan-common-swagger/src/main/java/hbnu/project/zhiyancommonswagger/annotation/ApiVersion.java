package hbnu.project.zhiyancommonswagger.annotation;

import java.lang.annotation.*;

/**
 * API 版本注解
 * 用于标识接口的版本信息
 * 
 * @author ErgouTree
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiVersion {

    /**
     * 版本号
     */
    String value() default "1.0";

    /**
     * 是否已弃用
     */
    boolean deprecated() default false;

    /**
     * 描述信息
     */
    String description() default "";
}

