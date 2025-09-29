package hbnu.project.zhiyancommonsecurity.annotation;

import java.lang.annotation.*;

/**
 * 登录验证注解
 * 用于标记需要登录才能访问的方法或类
 *
 * @author ErgouTree
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresLogin {
    
    /**
     * 验证失败时的提示消息
     */
    String message() default "请先登录";
}