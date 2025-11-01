package hbnu.project.common.log.annotation

/**
 * 操作日志注解
 *
 * 标注在方法上，自动记录操作日志
 *
 * @author ErgouTree
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class OperationLog(
    /**
     * 操作模块
     */
    val module: String = "",

    /**
     * 操作类型
     */
    val type: OperationType = OperationType.OTHER,

    /**
     * 操作描述
     */
    val description: String = "",

    /**
     * 是否记录请求参数
     */
    val recordParams: Boolean = true,

    /**
     * 是否记录响应结果
     */
    val recordResult: Boolean = true,

    /**
     * 是否记录异常
     */
    val recordException: Boolean = true
)


/**
 * 操作类型枚举
 */
enum class OperationType(val desc: String) {
    /** 查询 */
    QUERY("查询"),

    /** 新增 */
    INSERT("新增"),

    /** 更新 */
    UPDATE("更新"),

    /** 删除 */
    DELETE("删除"),

    /** 导出 */
    EXPORT("导出"),

    /** 导入 */
    IMPORT("导入"),

    /** 登录 */
    LOGIN("登录"),

    /** 登出 */
    LOGOUT("登出"),

    /** 授权 */
    GRANT("授权"),

    /** 上传 */
    UPLOAD("上传"),

    /** 下载 */
    DOWNLOAD("下载"),

    /** 其他 */
    OTHER("其他")
}

