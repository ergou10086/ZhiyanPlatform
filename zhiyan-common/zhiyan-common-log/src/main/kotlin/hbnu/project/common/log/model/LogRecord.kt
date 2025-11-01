package hbnu.project.common.log.model

import java.time.LocalDateTime

/**
 * 日志记录实体
 *
 * @author ErgouTree
 */
data class LogRecord(
    /** 日志ID */
    var logId: String? = null,

    /** 日志类型 */
    var logType: LogType = LogType.ACCESS,

    /** 应用名称 */
    var appName: String? = null,

    /** 服务器 IP */
    var serverIp: String? = null,

    /** 服务器主机名 */
    var serverHost: String? = null,

    /** 请求 URI */
    var requestUri: String? = null,

    /** 请求方法 */
    var requestMethod: String? = null,

    /** 请求参数 */
    var requestParams: String? = null,

    /** 请求头 */
    var requestHeaders: Map<String, String>? = null,

    /** 请求体 */
    var requestBody: String? = null,

    /** 客户端 IP */
    var clientIp: String? = null,

    /** 用户代理 */
    var userAgent: String? = null,

    /** 操作用户ID */
    var userId: String? = null,

    /** 操作用户名 */
    var username: String? = null,

    /** 操作模块 */
    var module: String? = null,

    /** 操作类型 */
    var operationType: String? = null,

    /** 操作描述 */
    var description: String? = null,

    /** 类名 */
    var className: String? = null,

    /** 方法名 */
    var methodName: String? = null,

    /** 响应结果 */
    var responseResult: String? = null,

    /** 响应状态码 */
    var responseStatus: Int? = null,

    /** 是否成功 */
    var success: Boolean = true,

    /** 异常信息 */
    var exception: String? = null,

    /** 堆栈信息 */
    var stackTrace: String? = null,

    /** 执行时间（毫秒） */
    var executionTime: Long? = null,

    /** 是否慢请求 */
    var slowRequest: Boolean = false,

    /** 创建时间 */
    var createTime: LocalDateTime = LocalDateTime.now(),

    /** 扩展信息 */
    var extraInfo: Map<String, Any>? = null
) {
    /**
     * 转换为 JSON 字符串
     */
    fun toJson(): String {
        return com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(this)
    }

    /**
     * 转换为简单的日志信息
     */
    fun toSimpleLog(): String {
        return buildString {
            append("[${logType.desc}] ")
            append("$requestMethod $requestUri ")
            append("| 用户: ${username ?: "匿名"} ")
            append("| IP: $clientIp ")
            append("| 耗时: ${executionTime}ms ")
            if (slowRequest) append("| ⚠️慢请求 ")
            if (!success) append("| ❌失败 ")
        }
    }
}

/**
 * 日志类型
 */
enum class LogType(val desc: String) {
    /** 访问日志 */
    ACCESS("访问日志"),

    /** 操作日志 */
    OPERATION("操作日志"),

    /** 异常日志 */
    EXCEPTION("异常日志"),

    /** 系统日志 */
    SYSTEM("系统日志"),

    /** 安全日志 */
    SECURITY("安全日志")
}

