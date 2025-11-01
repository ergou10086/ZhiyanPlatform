package hbnu.project.common.log.handler

import hbnu.project.common.log.model.LogRecord

/**
 * 日志处理器接口
 *
 * 自定义实现该接口以定制日志的处理逻辑（如存储到数据库、发送到 MQ 等）
 *
 * @author ErgouTree
 */
interface LogHandler {
    
    /**
     * 处理日志记录
     *
     * @param logRecord 日志记录
     */
    fun handle(logRecord: LogRecord)
    
    /**
     * 处理器优先级（数字越小优先级越高）
     */
    fun getOrder(): Int = 0
}

/**
 * 默认日志处理器
 *
 * 将日志输出到控制台
 */
class DefaultLogHandler : LogHandler {
    
    private val logger = org.slf4j.LoggerFactory.getLogger(DefaultLogHandler::class.java)
    
    override fun handle(logRecord: LogRecord) {
        when {
            !logRecord.success -> {
                logger.error("""
                    ╔════════════════════════════════════════════════════════════════
                    ║ ❌ 异常日志
                    ║ 请求: ${logRecord.requestMethod} ${logRecord.requestUri}
                    ║ 用户: ${logRecord.username ?: "匿名"} (${logRecord.userId ?: "N/A"})
                    ║ IP: ${logRecord.clientIp}
                    ║ 模块: ${logRecord.module ?: "N/A"}
                    ║ 操作: ${logRecord.description ?: "N/A"}
                    ║ 耗时: ${logRecord.executionTime}ms
                    ║ 异常: ${logRecord.exception}
                    ╚════════════════════════════════════════════════════════════════
                """.trimIndent())
            }
            logRecord.slowRequest -> {
                logger.warn("""
                    ╔════════════════════════════════════════════════════════════════
                    ║ ⚠️ 慢请求
                    ║ 请求: ${logRecord.requestMethod} ${logRecord.requestUri}
                    ║ 用户: ${logRecord.username ?: "匿名"} (${logRecord.userId ?: "N/A"})
                    ║ IP: ${logRecord.clientIp}
                    ║ 耗时: ${logRecord.executionTime}ms
                    ║ 参数: ${logRecord.requestParams}
                    ╚════════════════════════════════════════════════════════════════
                """.trimIndent())
            }
            else -> {
                logger.info(logRecord.toSimpleLog())
            }
        }
    }
    
    override fun getOrder(): Int = Int.MAX_VALUE
}

