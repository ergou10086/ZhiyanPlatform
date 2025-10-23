package hbnu.project.zhiyancommonswagger.annotation;

import java.lang.annotation.*;

/**
 * 忽略 Swagger 文档生成注解
 * 标注此注解的接口不会出现在 API 文档中
 * 
 * @author ErgouTree
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreSwagger {

    /**
     * 忽略原因
     */
    String reason() default "内部接口";
}

