package hbnu.project.zhiyancommonbasic.annotation;

import java.lang.annotation.*;

/**
 * API 版本控制注解，实现 URL 级别的版本控制
 * @author 树上的二狗
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiVersion {
    int value(); // API版本号
}