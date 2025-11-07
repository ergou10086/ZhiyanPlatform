package hbnu.project.common.log.util

import cn.hutool.extra.servlet.JakartaServletUtil
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import hbnu.project.common.log.model.LogRecord
import hbnu.project.common.log.model.LogType
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.io.PrintWriter
import java.io.StringWriter
import java.net.InetAddress
import java.time.LocalDateTime
import java.util.*

/**
 * 日志工具类
 *
 * @author ErgouTree
 */
object LogUtils {

    private val objectMapper = jacksonObjectMapper()

    /**
     * 获取当前请求
     */
    fun getCurrentRequest(): HttpServletRequest? {
        return try {
            val attributes = RequestContextHolder.getRequestAttributes()
            (attributes as? ServletRequestAttributes)?.request
        } catch (_: Exception) {
            null
        }
    }

    /**
     * 获取客户端 IP
     */
    fun getClientIp(request: HttpServletRequest?): String {
        return request?.let { JakartaServletUtil.getClientIP(it) } ?: "unknown"
    }

    /**
     * 获取服务器 IP
     */
    fun getServerIp(): String {
        return try {
            InetAddress.getLocalHost().hostAddress
        } catch (_: Exception) {
            "unknown"
        }
    }

    /**
     * 获取服务器主机名
     */
    fun getServerHost(): String {
        return try {
            InetAddress.getLocalHost().hostName
        } catch (_: Exception) {
            "unknown"
        }
    }

    /**
     * 获取请求头信息
     */
    fun getRequestHeaders(request: HttpServletRequest): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        request.headerNames?.toList()?.forEach { name ->
            headers[name] = request.getHeader(name) ?: ""
        }
        return headers
    }

    /**
     * 获取请求参数
     */
    fun getRequestParams(request: HttpServletRequest): String {
        val params = request.parameterMap
        return if (params.isNotEmpty()) {
            params.map { (key, value) ->
                "$key=${value.joinToString(",")}"
            }.joinToString("&")
        } else {
            ""
        }
    }

    /**
     * 获取异常堆栈信息
     */
    fun getStackTrace(throwable: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        return sw.toString()
    }

    /**
     * 截断字符串
     */
    fun truncate(str: String?, maxLength: Int): String? {
        if (str == null || str.length <= maxLength) {
            return str
        }
        return str.take(maxLength) + "...(已截断)"
    }

    /**
     * 对象转 JSON 字符串
     */
    fun toJson(obj: Any?): String {
        if (obj == null) return ""
        return try {
            objectMapper.writeValueAsString(obj)
        } catch (_: Exception) {
            obj.toString()
        }
    }

    /**
     * 生成日志 ID
     */
    fun generateLogId(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }

    /**
     * 创建日志记录
     */
    fun createLogRecord(
        logType: LogType,
        request: HttpServletRequest? = getCurrentRequest()
    ): LogRecord {
      // 尝试获取当前登录用户信息
        var userId: String? = null
        var username: String? = null
        try {
            // 使用反射调用SecurityUtils（避免直接依赖，保持模块独立）
            val securityUtilsClass = Class.forName("hbnu.project.zhiyancommonsecurity.utils.SecurityUtils")
            
            // 获取用户ID
            val getUserIdMethod = securityUtilsClass.getMethod("getUserId")
            val userIdObj = getUserIdMethod.invoke(null)
            if (userIdObj != null) {
                userId = userIdObj.toString()
            }
            
            // 获取用户名
            val getUsernameMethod = securityUtilsClass.getMethod("getUsername")
            val usernameObj = getUsernameMethod.invoke(null)
            if (usernameObj != null) {
                username = usernameObj.toString()
            }
        } catch (e: Exception) {
            // 如果获取失败（如SecurityUtils不存在），忽略异常，继续创建日志
            // 这样可以保证即使没有安全模块，日志功能仍然可用
        }
        
        return LogRecord(
            logId = generateLogId(),
            logType = logType,
            userId = userId,
            username = username,
            serverIp = getServerIp(),
            serverHost = getServerHost(),
            clientIp = request?.let { getClientIp(it) },
            requestUri = request?.requestURI,
            requestMethod = request?.method,
            userAgent = request?.getHeader("User-Agent"),
            createTime = LocalDateTime.now()
        )
    }

    /**
     * 判断是否需要排除
     */
    fun shouldExclude(uri: String, excludedPaths: List<String>): Boolean {
        return excludedPaths.any { pattern ->
            val regex = pattern
                .replace("**", ".*")
                .replace("*", "[^/]*")
            uri.matches(Regex(regex))
        }
    }
}

/**
 * Logger 扩展函数
 */

/**
 * 获取 Logger
 */
inline fun <reified T> T.logger(): Logger {
    return LoggerFactory.getLogger(T::class.java)
}

/**
 * Info 日志 - DSL 风格
 */
inline fun Logger.info(message: () -> String) {
    if (this.isInfoEnabled) {
        this.info(message())
    }
}

/**
 * Debug 日志 - DSL 风格
 */
inline fun Logger.debug(message: () -> String) {
    if (this.isDebugEnabled) {
        this.debug(message())
    }
}

/**
 * Warn 日志 - DSL 风格
 */
inline fun Logger.warn(message: () -> String) {
    if (this.isWarnEnabled) {
        this.warn(message())
    }
}

/**
 * Error 日志 - DSL 风格
 */
inline fun Logger.error(throwable: Throwable? = null, message: () -> String) {
    if (this.isErrorEnabled) {
        if (throwable != null) {
            this.error(message(), throwable)
        } else {
            this.error(message())
        }
    }
}

/**
 * 彩色日志输出
 */
object ColorLog {
    private const val RESET = "\u001B[0m"
    private const val RED = "\u001B[31m"
    private const val GREEN = "\u001B[32m"
    private const val YELLOW = "\u001B[33m"
    private const val BLUE = "\u001B[34m"
    private const val PURPLE = "\u001B[35m"
    private const val CYAN = "\u001B[36m"

    fun red(message: String) = "$RED$message$RESET"
    fun green(message: String) = "$GREEN$message$RESET"
    fun yellow(message: String) = "$YELLOW$message$RESET"
    fun blue(message: String) = "$BLUE$message$RESET"
    fun purple(message: String) = "$PURPLE$message$RESET"
    fun cyan(message: String) = "$CYAN$message$RESET"
}

