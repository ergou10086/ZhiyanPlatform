package hbnu.project.common.sentinel.feign;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Sentinel Feign 降级工厂
 * <p>
 * 通用的 Feign 降级处理，自动返回友好的错误信息
 * </p>
 *
 * @author ErgouTree
 */
@Slf4j
public class SentinelFeignFallbackFactory<T> implements FallbackFactory<T> {

    private final Class<T> targetType;
    private final String serviceName;

    public SentinelFeignFallbackFactory(Class<T> targetType, String serviceName) {
        this.targetType = targetType;
        this.serviceName = serviceName;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T create(Throwable cause) {
        log.error("[Sentinel Feign] 服务调用失败，服务: {}, 异常: {}", 
            serviceName, cause.getMessage(), cause);

        return (T) Proxy.newProxyInstance(
            targetType.getClassLoader(),
            new Class<?>[]{targetType},
            new FallbackInvocationHandler(serviceName, cause)
        );
    }

    /**
     * 降级处理器
     */
    static class FallbackInvocationHandler implements InvocationHandler {
        private final String serviceName;
        private final Throwable cause;

        public FallbackInvocationHandler(String serviceName, Throwable cause) {
            this.serviceName = serviceName;
            this.cause = cause;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            log.warn("[Sentinel Feign] 调用降级方法: {}.{}", serviceName, method.getName());

            // 构造降级响应
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("code", 503);
            result.put("message", String.format("服务 %s 暂时不可用", serviceName));
            result.put("service", serviceName);
            result.put("method", method.getName());
            result.put("timestamp", System.currentTimeMillis());

            // 如果返回类型是 void，返回 null
            if (method.getReturnType() == Void.TYPE) {
                return null;
            }

            // 返回降级结果
            return result;
        }
    }
}

