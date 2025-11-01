package hbnu.project.common.log.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 日志配置属性
 *
 * @author ErgouTree
 */
@ConfigurationProperties(prefix = "zhiyan.log")
data class LogProperties(
    /**
     * 是否启用日志系统
     */
    var enabled: Boolean = true,

    /**
     * 是否启用操作日志
     */
    var operationEnabled: Boolean = true,

    /**
     * 是否启用访问日志
     */
    var accessEnabled: Boolean = true,

    /**
     * 是否启用异常日志
     */
    var exceptionEnabled: Boolean = true,

    /**
     * 是否记录请求参数
     */
    var recordRequestParams: Boolean = true,

    /**
     * 是否记录响应结果
     */
    var recordResponseResult: Boolean = true,

    /**
     * 是否记录请求头
     */
    var recordHeaders: Boolean = false,

    /**
     * 响应结果最大长度（超过则截断）
     */
    var maxResponseLength: Int = 2000,

    /**
     * 请求参数最大长度（超过则截断）
     */
    var maxRequestLength: Int = 2000,

    /**
     * 排除的 URL 路径（不记录日志）
     */
    var excludedPaths: MutableList<String> = mutableListOf(
        "/actuator/**",
        "/health",
        "/favicon.ico",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/doc.html",
        "/webjars/**"
    ),

    /**
     * 慢请求阈值（毫秒）
     */
    var slowRequestThreshold: Long = 3000L,

    /**
     * 是否异步处理日志
     */
    var async: Boolean = true,

    /**
     * 日志处理线程池配置
     */
    var threadPool: ThreadPool = ThreadPool()
) {
    data class ThreadPool(
        var coreSize: Int = 2,
        var maxSize: Int = 5,
        var queueCapacity: Int = 100,
        var threadNamePrefix: String = "zhiyan-log-"
    )
}

