package hbnu.project.zhiyanactivelog.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import hbnu.project.zhiyanactivelog.annotation.BizOperationLog;
import hbnu.project.zhiyanactivelog.model.entity.*;
import hbnu.project.zhiyanactivelog.model.enums.*;
import hbnu.project.zhiyanactivelog.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 操作日志切面
 * 使用AOP自动记录操作日志，采用异步处理避免影响业务性能
 *
 * @author ErgouTree
 */
@Slf4j
@Aspect
@Component
@Order(2)
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogService operationLogService;

    private final ObjectMapper objectMapper;

    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 定义切点：所有带有@BizOperationLog注解的方法
     */
    @Pointcut("@annotation(hbnu.project.zhiyanactivelog.annotation.BizOperationLog)")
    public void operationLogPointcut() {
    }


    /**
     * 环绕通知：拦截操作并记录日志
     */
    @Around("operationLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 先拿到方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 获取注解
        BizOperationLog annotation = method.getAnnotation(BizOperationLog.class);
        if (annotation == null) {
            return joinPoint.proceed();
        }

        return null;
    }
}
