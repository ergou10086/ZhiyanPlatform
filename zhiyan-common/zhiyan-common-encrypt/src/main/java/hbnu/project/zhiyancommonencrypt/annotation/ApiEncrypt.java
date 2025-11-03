package hbnu.project.zhiyancommonencrypt.annotation;

import java.lang.annotation.*;

/**
 * API 接口加密注解
 * 标注在 Controller 方法上，自动对请求和响应进行加解密
 *
 * @author ErgouTree
 * @version 2.0.0
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiEncrypt {

    /**
     * 是否加密请求数据
     * 默认不加密请求
     */
    boolean requestEncrypt() default false;

    /**
     * 是否加密响应数据
     * 默认加密响应
     */
    boolean responseEncrypt() default true;
}
