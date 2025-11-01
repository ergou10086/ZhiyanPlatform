package hbnu.project.common.sentinel.handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 默认的 Sentinel 异常处理器
 *
 * @author ErgouTree
 */
@Slf4j
public class DefaultBlockExceptionHandler implements SentinelExceptionHandler {

    @Override
    public Object handle(Object request, BlockException exception) {
        log.warn("[Sentinel] 请求被限流/降级，异常类型：{}，规则：{}", 
            exception.getClass().getSimpleName(), 
            exception.getRule());

        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("timestamp", System.currentTimeMillis());

        if (exception instanceof FlowException) {
            // 流控异常
            result.put("code", 429);
            result.put("message", "请求过于频繁，请稍后再试");
            result.put("type", "FLOW");
        } else if (exception instanceof DegradeException) {
            // 降级异常
            result.put("code", 503);
            result.put("message", "服务暂时不可用，请稍后重试");
            result.put("type", "DEGRADE");
        } else if (exception instanceof ParamFlowException) {
            // 热点参数限流异常
            result.put("code", 429);
            result.put("message", "访问过于频繁，请稍后再试");
            result.put("type", "PARAM_FLOW");
        } else if (exception instanceof AuthorityException) {
            // 授权规则异常
            result.put("code", 403);
            result.put("message", "无权限访问");
            result.put("type", "AUTHORITY");
        } else {
            // 其他限流异常
            result.put("code", 429);
            result.put("message", "系统限流");
            result.put("type", "BLOCK");
        }

        return result;
    }

    @Override
    public Object handleFallback(Object request, Throwable throwable) {
        log.error("[Sentinel] 服务异常降级", throwable);

        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("code", 500);
        result.put("message", "服务异常，已降级处理");
        result.put("type", "FALLBACK");
        result.put("timestamp", System.currentTimeMillis());

        return result;
    }
}

