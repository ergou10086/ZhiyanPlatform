package hbnu.project.zhiyancommonsecurity.annotation;

import hbnu.project.zhiyancommonbasic.enums.Logical;

import java.lang.annotation.*;

/**
 * 权限验证注解
 * 用于方法级权限控制，验证当前用户是否拥有指定权限
 *
 * @author ErgouTree
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPermissions {

    /**
     * 需要验证的权限列表
     */
    String[] value();

    /**
     * 验证逻辑：AND-所有权限都必须拥有，OR-拥有任意一个权限即可
     * 默认为AND逻辑
     */
    Logical logical() default Logical.AND;
}

