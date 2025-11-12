package hbnu.project.common.log.util

import kotlin.jvm.JvmName
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Logger 扩展函数
 * 
 * 提供优雅的日志记录 API，支持 DSL 风格的懒加载日志
 */

/**
 * 获取 Logger
 * 使用方式：在类中使用 `private val log = logger()` 或 `private val log = this.logger()`
 * 
 * 注意：使用 @JvmName 注解避免与其他可能的 logger 扩展函数冲突
 */
@JvmName("getLoggerForReifiedType")
inline fun <reified T : Any> T.logger(): Logger {
    return LoggerFactory.getLogger(T::class.java)
}

/**
 * Info 日志 - DSL 风格（懒加载）
 * 使用 @JvmName 注解避免与 SLF4J Logger 的方法签名冲突
 * 
 * 使用方式：log.info { "消息: $message" }
 */
@JvmName("infoLazy")
inline fun Logger.info(message: () -> String) {
    if (this.isInfoEnabled) {
        this.info(message())
    }
}

/**
 * Debug 日志 - DSL 风格（懒加载）
 * 使用 @JvmName 注解避免与 SLF4J Logger 的方法签名冲突
 * 
 * 使用方式：log.debug { "详情: ${obj.toJson()}" }
 */
@JvmName("debugLazy")
inline fun Logger.debug(message: () -> String) {
    if (this.isDebugEnabled) {
        this.debug(message())
    }
}

/**
 * Warn 日志 - DSL 风格（懒加载）
 * 使用 @JvmName 注解避免与 SLF4J Logger 的方法签名冲突
 * 
 * 使用方式：log.warn { "警告: $warning" }
 */
@JvmName("warnLazy")
inline fun Logger.warn(message: () -> String) {
    if (this.isWarnEnabled) {
        this.warn(message())
    }
}

/**
 * Error 日志 - DSL 风格（懒加载）
 * 使用 @JvmName 注解避免与 SLF4J Logger 的方法签名冲突
 * 
 * 使用方式：log.error(exception) { "错误: $error" } 或 log.error { "错误: $error" }
 */
@JvmName("errorLazy")
inline fun Logger.error(throwable: Throwable? = null, message: () -> String) {
    if (this.isErrorEnabled) {
        if (throwable != null) {
            this.error(message(), throwable)
        } else {
            this.error(message())
        }
    }
}

