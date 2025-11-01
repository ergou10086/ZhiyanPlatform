package hbnu.project.common.log.config

import hbnu.project.common.log.aspect.AccessLogAspect
import hbnu.project.common.log.aspect.OperationLogAspect
import hbnu.project.common.log.handler.DefaultLogHandler
import hbnu.project.common.log.handler.GlobalExceptionHandler
import hbnu.project.common.log.handler.LogHandler
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor
import java.util.concurrent.ThreadPoolExecutor

/**
 * 日志自动配置类
 *
 * @author ErgouTree
 */
@Configuration
@EnableAspectJAutoProxy
@EnableConfigurationProperties(LogProperties::class)
@ConditionalOnProperty(prefix = "zhiyan.log", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class LogAutoConfiguration(
    private val logProperties: LogProperties
) {

    private val logger = LoggerFactory.getLogger(LogAutoConfiguration::class.java)

    init {
        logger.info("========================================")
        logger.info("    智研平台日志系统初始化 (Kotlin)")
        logger.info("========================================")
        logger.info("  操作日志: {}", if (logProperties.operationEnabled) "✓ 已启用" else "✗ 未启用")
        logger.info("  访问日志: {}", if (logProperties.accessEnabled) "✓ 已启用" else "✗ 未启用")
        logger.info("  异常日志: {}", if (logProperties.exceptionEnabled) "✓ 已启用" else "✗ 未启用")
        logger.info("  异步处理: {}", if (logProperties.async) "✓ 已启用" else "✗ 未启用")
        logger.info("  慢请求阈值: {}ms", logProperties.slowRequestThreshold)
        logger.info("========================================")
    }

    /**
     * 默认日志处理器
     */
    @Bean
    @ConditionalOnMissingBean(LogHandler::class)
    fun defaultLogHandler(): LogHandler {
        logger.info("[日志系统] 注册默认日志处理器")
        return DefaultLogHandler()
    }

    /**
     * 操作日志切面
     */
    @Bean
    @ConditionalOnProperty(prefix = "zhiyan.log", name = ["operation-enabled"], havingValue = "true", matchIfMissing = true)
    fun operationLogAspect(
        logHandlers: List<LogHandler>,
        @Value("\${spring.application.name:unknown}") appName: String
    ): OperationLogAspect {
        logger.info("[日志系统] 注册操作日志切面")
        return OperationLogAspect(logProperties, logHandlers, appName)
    }

    /**
     * 访问日志切面
     */
    @Bean
    @ConditionalOnProperty(prefix = "zhiyan.log", name = ["access-enabled"], havingValue = "true", matchIfMissing = true)
    fun accessLogAspect(
        logHandlers: List<LogHandler>,
        @Value("\${spring.application.name:unknown}") appName: String
    ): AccessLogAspect {
        logger.info("[日志系统] 注册访问日志切面")
        return AccessLogAspect(logProperties, logHandlers, appName)
    }

    /**
     * 全局异常处理器
     */
    @Bean
    @ConditionalOnProperty(prefix = "zhiyan.log", name = ["exception-enabled"], havingValue = "true", matchIfMissing = true)
    fun globalExceptionHandler(
        logHandlers: List<LogHandler>,
        @Value("\${spring.application.name:unknown}") appName: String
    ): GlobalExceptionHandler {
        logger.info("[日志系统] 注册全局异常处理器")
        return GlobalExceptionHandler(logProperties, logHandlers, appName)
    }

    /**
     * 日志线程池
     */
    @Bean("logTaskExecutor")
    @ConditionalOnProperty(prefix = "zhiyan.log", name = ["async"], havingValue = "true", matchIfMissing = true)
    fun logTaskExecutor(): Executor {
        logger.info("[日志系统] 配置异步日志线程池")
        
        val threadPool = logProperties.threadPool
        return ThreadPoolTaskExecutor().apply {
            corePoolSize = threadPool.coreSize
            maxPoolSize = threadPool.maxSize
            queueCapacity = threadPool.queueCapacity
            setThreadNamePrefix(threadPool.threadNamePrefix)
            setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())
            setWaitForTasksToCompleteOnShutdown(true)
            setAwaitTerminationSeconds(60)
            initialize()
        }
    }
}

