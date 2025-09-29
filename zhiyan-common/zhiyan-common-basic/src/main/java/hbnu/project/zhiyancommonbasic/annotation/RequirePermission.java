package hbnu.project.zhiyancommonbasic.annotation;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限验证注解
 * 用于标记需要特定权限的接口
 *
 * @author ErgouTree
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@SecurityRequirement(name = "bearerAuth")
public @interface RequirePermission {
    /**
     * 需要的权限
     */
    String[] value() default {};

    /**
     * 权限验证逻辑（AND/OR）
     */
    String logic() default "AND";

    /**
     * 权限描述
     */
    String description() default "";
}
