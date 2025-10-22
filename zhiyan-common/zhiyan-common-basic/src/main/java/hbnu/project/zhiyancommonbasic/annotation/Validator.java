package hbnu.project.zhiyancommonbasic.annotation;

import java.lang.annotation.*;

/**
 * 数据校验注解，用于自动验证请求参数或方法参数
 * 可用于Controller层或Service层参数校验
 *
 * @author 树上的二狗
 */
@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Validator {
    /**
     * 验证规则描述
     */
    String rule() default "";

    /**
     * 是否允许为空
     */
    boolean allowNull() default false;

    /**
     * 错误提示消息
     */
    String message() default "参数验证失败";

    /**
     * 验证失败时的处理策略
     */
    ValidatePolicy policy() default ValidatePolicy.EXCEPTION;

    /**
     * 校验失败处理策略枚举
     */
    enum ValidatePolicy {
        EXCEPTION,  // 抛出异常
        LOG_ONLY,   // 仅记录日志
        DEFAULT_VALUE  // 使用默认值
    }
}
