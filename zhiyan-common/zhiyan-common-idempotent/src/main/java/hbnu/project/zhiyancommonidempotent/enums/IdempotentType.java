package hbnu.project.zhiyancommonidempotent.enums;

/**
 * 幂等类型枚举
 *
 * @author yui
 * @rewrite ErgouTree
 * @Re_rewrite yui
 */
public enum IdempotentType {

    /**
     * 防重复提交（基于请求参数）
     * 适用场景：表单提交、订单创建等
     */
    PARAM,

    /**
     * 幂等Token（基于申请-消费模式）
     * 适用场景：支付、转账等关键操作
     */
    TOKEN,

    /**
     * 基于SpEL表达式的自定义key
     * 适用场景：需要自定义唯一标识的场景
     * @yui: spEl 会冲突，真别用
     */
    SPEL
}

