package hbnu.project.common.log.aspect

import hbnu.project.common.log.annotation.AccessLog
import hbnu.project.common.log.config.LogProperties
import hbnu.project.common.log.handler.LogHandler
import hbnu.project.common.log.model.LogRecord
import hbnu.project.common.log.model.LogType
import hbnu.project.common.log.util.LogUtils
import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.*
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.annotation.Order
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.time.LocalDateTime

/**
 * 访问日志切面
 *
 * 拦截 @AccessLog 注解或所有 Controller 方法，自动记录访问日志
 *
 * @author ErgouTree
 */
@Aspect
@Order(2)
class AccessLogAspect(
    private val logProperties: LogProperties,
    private val logHandlers: List<LogHandler>,
    @get:Value("\${spring.application.name:unknown}") private val appName: String
) {

    private val logger = LoggerFactory.getLogger(AccessLogAspect::class.java)
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    /**
     * 拦截 @AccessLog 注解
     */
    @Around("@annotation(accessLog)")
    fun aroundWithAnnotation(joinPoint: ProceedingJoinPoint, accessLog: AccessLog): Any? {
        return processLog(joinPoint, accessLog)
    }

    /**
     * 拦截所有 Controller 方法（没有 @AccessLog 注解的）
     */
    @Around("execution(* *..*Controller.*(..)) && !@annotation(hbnu.project.common.log.annotation.AccessLog)")
    fun aroundController(joinPoint: ProceedingJoinPoint): Any? {
        if (!logProperties.enabled || !logProperties.accessEnabled) {
            return joinPoint.proceed()
        }
        return processLog(joinPoint, null)
    }

    /**
     * 处理日志
     */
    private fun processLog(joinPoint: ProceedingJoinPoint, accessLog: AccessLog?): Any? {
        val startTime = System.currentTimeMillis()
        val request = getCurrentRequest() ?: return joinPoint.proceed()

        // 检查是否需要排除
        val uri = request.requestURI
        if (LogUtils.shouldExclude(uri, logProperties.excludedPaths)) {
            return joinPoint.proceed()
        }

        // 创建日志记录
        val logRecord = LogUtils.createLogRecord(LogType.ACCESS, request).apply {
            this.appName = this@AccessLogAspect.appName
            this.description = accessLog?.value?.ifEmpty { null }
            
            // 获取方法信息
            val signature = joinPoint.signature as MethodSignature
            this.className = joinPoint.target.javaClass.name
            this.methodName = signature.method.name
            
            // 记录请求参数
            val recordParams = accessLog?.recordParams ?: logProperties.recordRequestParams
            if (recordParams) {
                this.requestParams = LogUtils.truncate(
                    LogUtils.getRequestParams(request),
                    logProperties.maxRequestLength
                )
            }
            
            // 记录请求头
            val recordHeaders = accessLog?.recordHeaders ?: logProperties.recordHeaders
            if (recordHeaders) {
                this.requestHeaders = LogUtils.getRequestHeaders(request)
            }
        }

        var result: Any?
        try {
            // 执行目标方法
            result = joinPoint.proceed()
            
            logRecord.apply {
                this.success = true
                this.responseStatus = 200
                
                // 记录响应结果
                val recordResult = accessLog?.recordResult ?: logProperties.recordResponseResult
                if (recordResult) {
                    this.responseResult = LogUtils.truncate(
                        LogUtils.toJson(result),
                        logProperties.maxResponseLength
                    )
                }
            }
            
            return result
            
        } catch (e: Throwable) {
            // 记录异常
            logRecord.apply {
                this.success = false
                this.exception = e.message
                this.stackTrace = LogUtils.getStackTrace(e)
            }
            throw e
            
        } finally {
            // 计算执行时间
            val executionTime = System.currentTimeMillis() - startTime
            logRecord.apply {
                this.executionTime = executionTime
                this.slowRequest = executionTime > logProperties.slowRequestThreshold
                this.createTime = LocalDateTime.now()
            }
            
            // 异步处理日志
            handleLogAsync(logRecord)
        }
    }

    /**
     * 获取当前请求
     */
    private fun getCurrentRequest(): HttpServletRequest? {
        return try {
            val attributes = RequestContextHolder.getRequestAttributes()
            (attributes as? ServletRequestAttributes)?.request
        } catch (_: Exception) {
            null
        }
    }

    /**
     * 异步处理日志
     */
    private fun handleLogAsync(logRecord: LogRecord) {
        if (logProperties.async) {
            scope.launch {
                try {
                    processLogHandlers(logRecord)
                } catch (e: Exception) {
                    logger.error("处理访问日志失败", e)
                }
            }
        } else {
            processLogHandlers(logRecord)
        }
    }

    /**
     * 处理日志
     */
    private fun processLogHandlers(logRecord: LogRecord) {
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

