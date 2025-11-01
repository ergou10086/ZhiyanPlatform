package hbnu.project.common.sentinel.handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * Sentinel 异常处理器接口
 * <p>
 * 自定义实现该接口以定制限流、降级等异常的处理逻辑
 * </p>
 *
 * @author ErgouTree
 */
public interface SentinelExceptionHandler {

    /**
     * 处理限流、降级等异常
     *
     * @param request   请求对象
     * @param exception 限流异常
     * @return 响应结果
     */
    Object handle(Object request, BlockException exception);

    /**
     * 处理业务异常（fallback）
     *
     * @param request   请求对象
     * @param throwable 异常
     * @return 响应结果
     */
    Object handleFallback(Object request, Throwable throwable);
}

