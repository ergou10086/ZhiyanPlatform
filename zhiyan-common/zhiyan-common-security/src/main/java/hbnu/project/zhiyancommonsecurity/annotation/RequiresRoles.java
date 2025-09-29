package hbnu.project.zhiyancommonsecurity.annotation;

import hbnu.project.zhiyancommonbasic.enums.Logical;

import java.lang.annotation.*;

/**
 * 角色验证注解
 * 用于方法级角色控制，验证当前用户是否拥有指定角色
 *
 * @author ErgouTree
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresRoles {

    /**
     * 需要验证的角色列表
     */
    String[] value();

    /**
     * 验证逻辑：AND-所有角色都必须拥有，OR-拥有任意一个角色即可
     * 默认为AND逻辑
     */
    Logical logical() default Logical.AND;
}