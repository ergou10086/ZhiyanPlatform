package hbnu.project.common.log.handler

import hbnu.project.common.log.config.LogProperties
import hbnu.project.common.log.model.LogRecord
import hbnu.project.common.log.model.LogType
import hbnu.project.common.log.util.LogUtils
import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

/**
 * 全局异常处理器
 *
 * 捕获所有未处理的异常，记录异常日志
 *
 * @author ErgouTree
 */
@RestControllerAdvice
class GlobalExceptionHandler(
    private val logProperties: LogProperties,
    private val logHandlers: List<LogHandler>,
    @Value("\${spring.application.name:unknown}") private val appName: String
) {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    /**
     * 处理所有异常
     */
    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception, request: HttpServletRequest): Map<String, Any> {
        // 记录异常日志
        if (logProperties.enabled && logProperties.exceptionEnabled) {
            val logRecord = createExceptionLog(e, request)
            handleLogAsync(logRecord)
        }

        // 返回错误响应
        return mapOf(
            "success" to false,
            "code" to 500,
            "message" to (e.message ?: "服务器内部错误"),
            "timestamp" to System.currentTimeMillis()
        )
    }

    /**
     * 创建异常日志
     */
    private fun createExceptionLog(e: Exception, request: HttpServletRequest): LogRecord {
        return LogUtils.createLogRecord(LogType.EXCEPTION, request).apply {
            this.appName = this@GlobalExceptionHandler.appName
            this.success = false
            this.exception = e.message
            this.stackTrace = LogUtils.getStackTrace(e)
            this.requestParams = LogUtils.truncate(
                LogUtils.getRequestParams(request),
                logProperties.maxRequestLength
            )
            this.createTime = LocalDateTime.now()
        }
    }

    /**
     * 异步处理日志
     */
    private fun handleLogAsync(logRecord: LogRecord) {
        if (logProperties.async) {
            scope.launch {
                try {
                    processLog(logRecord)
                } catch (ex: Exception) {
                    logger.error("处理异常日志失败", ex)
                }
            }
        } else {
            processLog(logRecord)
        }
    }

    /**
     * 处理日志
     */
    private fun processLog(logRecord: LogRecord) {
        logHandlers
            .sortedBy { it.getOrder() }
            .forEach { handler ->
                try {
                    handler.handle(logRecord)
                } catch (e: Exception) {
                    logger.error("日志处理器执行失败: ${handler.javaClass.simpleName}", e)
                }
            }
    }
}

