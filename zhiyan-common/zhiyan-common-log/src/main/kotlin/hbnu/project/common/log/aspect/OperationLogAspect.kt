package hbnu.project.common.log.aspect

import hbnu.project.common.log.annotation.OperationLog
import hbnu.project.common.log.config.LogProperties
import hbnu.project.common.log.handler.LogHandler
import hbnu.project.common.log.model.LogRecord
import hbnu.project.common.log.model.LogType
import hbnu.project.common.log.util.LogUtils
import kotlinx.coroutines.*
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.annotation.Order
import java.time.LocalDateTime

/**
 * 操作日志切面
 *
 * 拦截 @OperationLog 注解，自动记录操作日志
 *
 * @author ErgouTree
 */
@Aspect
@Order(1)
class OperationLogAspect(
    private val logProperties: LogProperties,
    private val logHandlers: List<LogHandler>,
    @Value("\${spring.application.name:unknown}") private val appName: String
) {

    private val logger = LoggerFactory.getLogger(OperationLogAspect::class.java)
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @Around("@annotation(operationLog)")
    fun around(joinPoint: ProceedingJoinPoint, operationLog: OperationLog): Any? {
        // 如果未启用操作日志，直接执行方法
        if (!logProperties.enabled || !logProperties.operationEnabled) {
            return joinPoint.proceed()
        }

        val startTime = System.currentTimeMillis()
        val request = LogUtils.getCurrentRequest()
        
        // 创建日志记录
        val logRecord = LogUtils.createLogRecord(LogType.OPERATION, request).apply {
            this.appName = this@OperationLogAspect.appName
            this.module = operationLog.module.ifEmpty { null }
            this.operationType = operationLog.type.name
            this.description = operationLog.description.ifEmpty { null }
            
            // 获取方法信息
            val signature = joinPoint.signature as MethodSignature
            this.className = joinPoint.target.javaClass.name
            this.methodName = signature.method.name
            
            // 记录请求参数
            if (operationLog.recordParams && logProperties.recordRequestParams) {
                val params = LogUtils.getRequestParams(request!!)
                val args = joinPoint.args.joinToString { LogUtils.toJson(it) }
                this.requestParams = LogUtils.truncate(
                    if (params.isNotEmpty()) params else args,
                    logProperties.maxRequestLength
                )
            }
        }

        var result: Any? = null
        try {
            // 执行目标方法
            result = joinPoint.proceed()
            
            logRecord.apply {
                this.success = true
                this.responseStatus = 200
                
                // 记录响应结果
                if (operationLog.recordResult && logProperties.recordResponseResult) {
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
                if (operationLog.recordException) {
                    this.stackTrace = LogUtils.getStackTrace(e)
                }
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
     * 异步处理日志
     */
    private fun handleLogAsync(logRecord: LogRecord) {
        if (logProperties.async) {
            scope.launch {
                try {
                    processLog(logRecord)
                } catch (e: Exception) {
                    logger.error("处理操作日志失败", e)
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

