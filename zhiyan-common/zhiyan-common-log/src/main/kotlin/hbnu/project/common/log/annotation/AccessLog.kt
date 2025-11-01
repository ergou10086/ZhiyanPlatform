package hbnu.project.common.log.annotation

/**
 * 访问日志注解
 *
 * 标注在 Controller 类或方法上，自动记录访问日志
 *
 * @author ErgouTree
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class AccessLog(
    /**
     * 日志描述
     */
    val value: String = "",

    /**
     * 是否记录请求参数
     */
    val recordParams: Boolean = true,

    /**
     * 是否记录响应结果
     */
    val recordResult: Boolean = false,

    /**
     * 是否记录请求头
     */
    val recordHeaders: Boolean = false
)

