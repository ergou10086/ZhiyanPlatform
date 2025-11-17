package hbnu.project.zhiyanactivelog.aspect;

import hbnu.project.zhiyanactivelog.annotation.BizOperationLog;
import hbnu.project.zhiyanactivelog.model.entity.*;
import hbnu.project.zhiyanactivelog.model.enums.*;
import hbnu.project.zhiyanactivelog.service.OperationLogSaveService;
import hbnu.project.zhiyancommonbasic.utils.JsonUtils;
import hbnu.project.zhiyancommonbasic.utils.ServletUtils;
import hbnu.project.zhiyancommonbasic.utils.ip.IpUtils;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志切面
 * 使用AOP自动记录操作日志，采用异步处理避免影响业务性能
 *
 * @author ErgouTree
 */
@Slf4j
@Aspect
@Component("activelogOperationLogAspect")
@Order(2)
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogSaveService operationLogSaveService;

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

        // 获取请求信息
        HttpServletRequest request = ServletUtils.getRequest();

        // 获取用户信息
        Long userId = SecurityUtils.getUserId();
        String username = getUsernameFromAnnotationOrContext(annotation, joinPoint);

        // 记录开始时间
        LocalDateTime operationTime = LocalDateTime.now();

        // 执行目标方法
        Object result = null;
        OperationResult operationResult = OperationResult.SUCCESS;
        String errorMessage = null;

        try{
            result = joinPoint.proceed();
            return result;
        }catch (Exception e) {
            operationResult = OperationResult.FAILED;
            errorMessage = e.getMessage();
            log.error("操作执行失败: {}", e.getMessage(), e);
            throw e;
        } finally {
            // 异步记录日志
            try{
                recordOperationLogAsync(
                        annotation,
                        joinPoint,
                        request,
                        userId,
                        username,
                        operationTime,
                        operationResult,
                        errorMessage,
                        result
                );
            } catch (Exception e) {
                log.error("记录操作日志失败: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * 异步记录操作日志
     */
    @Async("operationLogTaskExecutor")
    public void recordOperationLogAsync(
            BizOperationLog annotation,
            ProceedingJoinPoint joinPoint,
            HttpServletRequest request,
            Long userId,
            String username,
            LocalDateTime operationTime,
            OperationResult operationResult,
            String errorMessage,
            Object result) {

        try{
            // 解析 SpEL
            EvaluationContext context = createEvaluationContext(joinPoint, result);

            Long projectId = parseSpelExpression(annotation.projectId(), context, Long.class);
            Long bizId = parseSpelExpression(annotation.bizId(), context, Long.class);
            String bizTitle = parseSpelExpression(annotation.bizTitle(), context, String.class);

            // 使用工具类获取IP和User-Agent
            String ipAddress = getIpAddress(request);
            String userAgent = ServletUtils.getHeader(request, "User-Agent");

            // 构建请求参数JSON
            String requestParams = null;
            if (annotation.recordParams()) {
                requestParams = buildRequestParams(joinPoint);
            }

            // 使用日志保存服务统一保存日志
            operationLogSaveService.saveLogByModule(
                    annotation, projectId, bizId, bizTitle, userId, username,
                    ipAddress, userAgent, operationTime, operationResult,
                    errorMessage, requestParams
            );

            log.debug("操作日志记录成功，pxl: module={}, type={}, user={}",
                    annotation.module(), annotation.type(), username);

        } catch (Exception e) {
            log.error("异步记录操作日志失败: {}", e.getMessage(), e);
        }
    }



    /**
     * 构建一个包含方法调用上下文信息的 SpEL 评估环境
     * 让 SpEL 表达式能访问到方法的参数和返回值
     */
    private EvaluationContext createEvaluationContext(ProceedingJoinPoint joinPoint, Object result) {
        // Spring 提供的标准评估上下文
        StandardEvaluationContext context = new StandardEvaluationContext();

        // 把上面的参数拿下来设置
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 提取参数名数组parameterNames
        String[] parameterNames = signature.getParameterNames();
        // 实参数组
        Object[] arguments = joinPoint.getArgs();

        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], arguments[i]);
        }

        // 设置返回结果
        if (result != null) {
            context.setVariable("result", result);
        }

        return context;
    }

    /**
     * 解析SpEL表达式
     * 根据创建的上下文解析 SpEL 表达式，并返回指定类型的结果。
     */
    private <T> T parseSpelExpression(String expression, EvaluationContext context, Class<T> clazz) {
        if(expression == null || expression.trim().isEmpty()){
            return null;
        }

        try {
            // ExpressionParser SpEL 解析器，并且在在指定的上下文中执行表达式，并将结果转换为目标类型clazz。
            return parser.parseExpression(expression).getValue(context, clazz);
        } catch (Exception e) {
            log.warn("解析SpEL表达式失败: expression={}, error={}", expression, e.getMessage());
            return null;
        }
    }

    /**
     * 从注解或安全上下文获取用户名
     */
    private String getUsernameFromAnnotationOrContext(BizOperationLog annotation, ProceedingJoinPoint joinPoint) {
        // 如果注解中指定了username的SpEL表达式
        if(annotation.username() != null && !annotation.username().trim().isEmpty()){
            EvaluationContext context = createEvaluationContext(joinPoint, null);
            String username = parseSpelExpression(annotation.username(), context, String.class);
            if (username != null) {
                return username;
            }
        // 若没有则从Spring Security上下文获取
        }else{
            try{
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if(authentication != null && authentication.getName() != null){
                    return authentication.getName();
                }
            }catch (Exception e){
                log.warn("获取当前用户名失败: {}", e.getMessage());
            }
            return "SYSTEM";
        }
        return "SYSTEM";
    }


    /**
     * 构建请求参数JSON - 使用工具类优化
     */
    private String buildRequestParams(ProceedingJoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] parameterNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();

            Map<String, Object> params = new HashMap<>();
            for (int i = 0; i < parameterNames.length; i++) {
                Object arg = args[i];
                // 过滤掉HttpServletRequest等不需要序列化的参数
                if (arg instanceof HttpServletRequest || arg instanceof jakarta.servlet.http.HttpServletResponse) {
                    continue;
                }
                params.put(parameterNames[i], arg);
            }

            // 使用JsonUtils工具类进行序列化
            return JsonUtils.toJsonString(params);
        } catch (Exception e) {
            log.warn("构建请求参数JSON失败: {}", e.getMessage());
            return null;
        }
    }


    /**
     * 获取客户端IP地址 - 使用工具类优化
     */
    private String getIpAddress(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        // 使用工具类获取IP地址
        String ip = IpUtils.getIpAddr(request);

        // 处理IPv6本地地址
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            return "127.0.0.1";
        }

        return ip;
    }

}
