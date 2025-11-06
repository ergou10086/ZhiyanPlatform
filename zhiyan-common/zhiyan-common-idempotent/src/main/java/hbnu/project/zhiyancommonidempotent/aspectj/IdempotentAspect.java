package hbnu.project.zhiyancommonidempotent.aspectj;

import cn.dev33.satoken.SaManager;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.SecureUtil;
import hbnu.project.zhiyancommonbasic.constants.GlobalConstants;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonbasic.exception.ServiceException;
import hbnu.project.zhiyancommonbasic.utils.JsonUtils;
import hbnu.project.zhiyancommonbasic.utils.MessageUtils;
import hbnu.project.zhiyancommonbasic.utils.ServletUtils;
import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import hbnu.project.zhiyancommonbasic.utils.ip.IpUtils;
import hbnu.project.zhiyancommonidempotent.annotation.Idempotent;
import hbnu.project.zhiyancommonidempotent.enums.IdempotentType;
import hbnu.project.zhiyancommonidempotent.service.IdempotentTokenService;
import hbnu.project.zhiyancommonredis.utils.RedisUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.StringJoiner;

/**
 * 幂等切面处理器
 * 支持多种幂等策略：参数防重、Token机制、SpEL自定义key
 *
 * @author yui
 */
@Slf4j
@Aspect
@RequiredArgsConstructor
public class IdempotentAspect {

    private final IdempotentTokenService idempotentTokenService;

    /**
     * SpEL表达式解析器
     */
    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 参数名发现器
     */
    private final StandardReflectionParameterNameDiscoverer discoverer = new StandardReflectionParameterNameDiscoverer();

    /**
     * 线程本地变量，存储当前请求的Redis key
     */
    private static final ThreadLocal<String> KEY_CACHE = new ThreadLocal<>();

    @Before("@annotation(idempotent)")
    public void doBefore(JoinPoint point, Idempotent idempotent) {
        // 获取幂等类型
        IdempotentType type = idempotent.type();

        // 根据不同类型处理
        switch (type) {
            case PARAM -> handleParamIdempotent(point, idempotent);
            case TOKEN -> handleTokenIdempotent(point, idempotent);
            case SPEL -> handleSpelIdempotent(point, idempotent);
            default -> throw new ServiceException("不支持的幂等类型: " + type);
        }
    }

    /**
     * 处理基于参数的幂等
     */
    private void handleParamIdempotent(JoinPoint point, Idempotent idempotent) {
        HttpServletRequest request = ServletUtils.getRequest();
        if (request == null) {
            throw new ServiceException("无法获取当前请求对象");
        }

        // 获取请求参数
        String params = argsArrayToString(point.getArgs());

        // 请求地址
        String url = request.getRequestURI();

        // 获取token或使用IP作为唯一标识
        String identifier = StringUtils.trimToEmpty(request.getHeader(SaManager.getConfig().getTokenName()));
        if (StringUtils.isBlank(identifier)) {
            identifier = IpUtils.getIpAddr(request);
        }

        // 生成唯一key
        String uniqueKey = SecureUtil.md5(identifier + ":" + params);
        String cacheKey = GlobalConstants.GLOBAL_REDIS_KEY + "idempotent:param:" + url + ":" + uniqueKey;

        // 尝试设置Redis key
        long timeout = idempotent.timeUnit().toMillis(idempotent.timeout());
        if (RedisUtils.setObjectIfAbsent(cacheKey, System.currentTimeMillis(), Duration.ofMillis(timeout))) {
            KEY_CACHE.set(cacheKey);
            log.debug("幂等检查通过[PARAM]: {}", cacheKey);
        } else {
            String message = getMessage(idempotent.message(), "请勿重复操作");
            log.warn("幂等检查失败[PARAM]: {}", cacheKey);
            throw new ServiceException(message);
        }
    }

    /**
     * 处理基于Token的幂等
     */
    private void handleTokenIdempotent(JoinPoint point, Idempotent idempotent) {
        HttpServletRequest request = ServletUtils.getRequest();
        if (request == null) {
            throw new ServiceException("无法获取当前请求对象");
        }

        // 从请求头中获取Token
        String token = request.getHeader("Idempotent-Token");
        if (StringUtils.isBlank(token)) {
            // 尝试从参数中获取
            token = request.getParameter("idempotentToken");
        }

        if (StringUtils.isBlank(token)) {
            String message = getMessage(idempotent.message(), "请提供幂等Token");
            log.warn("幂等检查失败[TOKEN]: Token为空");
            throw new ServiceException(message);
        }

        // 验证并消费Token
        if (!idempotentTokenService.validateAndConsumeToken(token)) {
            String message = getMessage(idempotent.message(), "操作已提交，请勿重复操作");
            log.warn("幂等检查失败[TOKEN]: Token无效或已被消费 - {}", token);
            throw new ServiceException(message);
        }

        // Token方式不需要在finally中删除，因为已经被消费了
        log.debug("幂等检查通过[TOKEN]: {}", token);
    }

    /**
     * 处理基于SpEL表达式的幂等
     */
    private void handleSpelIdempotent(JoinPoint point, Idempotent idempotent) {
        String spelKey = idempotent.key();
        if (StringUtils.isBlank(spelKey)) {
            throw new ServiceException("SpEL类型的幂等必须指定key表达式");
        }

        // 解析SpEL表达式
        String uniqueKey = parseSpel(point, spelKey);
        if (StringUtils.isBlank(uniqueKey)) {
            throw new ServiceException("SpEL表达式解析结果为空");
        }

        HttpServletRequest request = ServletUtils.getRequest();
        String url = request != null ? request.getRequestURI() : "unknown";

        // 生成Redis key
        String cacheKey = GlobalConstants.GLOBAL_REDIS_KEY + "idempotent:spel:" + url + ":" + uniqueKey;

        // 尝试设置Redis key
        long timeout = idempotent.timeUnit().toMillis(idempotent.timeout());
        if (RedisUtils.setObjectIfAbsent(cacheKey, System.currentTimeMillis(), Duration.ofMillis(timeout))) {
            KEY_CACHE.set(cacheKey);
            log.debug("幂等检查通过[SPEL]: {} -> {}", spelKey, cacheKey);
        } else {
            String message = getMessage(idempotent.message(), "请勿重复操作");
            log.warn("幂等检查失败[SPEL]: {} -> {}", spelKey, cacheKey);
            throw new ServiceException(message);
        }
    }

    /**
     * 处理完请求后执行
     */
    @AfterReturning(pointcut = "@annotation(idempotent)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Idempotent idempotent, Object jsonResult) {
        // Token类型不需要处理，因为已经在验证时消费了
        if (idempotent.type() == IdempotentType.TOKEN) {
            return;
        }

        String cacheKey = KEY_CACHE.get();
        if (StringUtils.isBlank(cacheKey)) {
            return;
        }

        try {
            if (jsonResult instanceof R<?> r) {
                // 如果配置了失败时删除，且返回失败，则删除key允许重试
                if (idempotent.deleteOnError() && r.getCode() != R.SUCCESS) {
                    RedisUtils.deleteObject(cacheKey);
                    log.debug("业务执行失败，删除幂等key: {}", cacheKey);
                }
            }
        } finally {
            KEY_CACHE.remove();
        }
    }

    /**
     * 拦截异常操作
     */
    @AfterThrowing(value = "@annotation(idempotent)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Idempotent idempotent, Exception e) {
        // Token类型不需要处理
        if (idempotent.type() == IdempotentType.TOKEN) {
            return;
        }

        String cacheKey = KEY_CACHE.get();
        if (StringUtils.isBlank(cacheKey)) {
            return;
        }

        try {
            // 如果配置了失败时删除，则删除key允许重试
            if (idempotent.deleteOnError()) {
                RedisUtils.deleteObject(cacheKey);
                log.debug("业务执行异常，删除幂等key: {}", cacheKey);
            }
        } finally {
            KEY_CACHE.remove();
        }
    }

    /**
     * 解析SpEL表达式
     */
    private String parseSpel(JoinPoint point, String spelExpression) {
        try {
            // 获取方法
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();

            // 获取参数名
            String[] parameterNames = discoverer.getParameterNames(method);
            if (parameterNames == null || parameterNames.length == 0) {
                return null;
            }

            // 获取参数值
            Object[] args = point.getArgs();

            // 创建SpEL上下文
            EvaluationContext context = new StandardEvaluationContext();
            for (int i = 0; i < parameterNames.length; i++) {
                context.setVariable(parameterNames[i], args[i]);
            }

            // 解析表达式
            Expression expression = parser.parseExpression(spelExpression);
            Object value = expression.getValue(context);

            return value != null ? value.toString() : null;
        } catch (Exception e) {
            log.error("SpEL表达式解析失败: {}", spelExpression, e);
            throw new ServiceException("幂等key解析失败: " + e.getMessage());
        }
    }

    /**
     * 参数拼装
     */
    private String argsArrayToString(Object[] paramsArray) {
        StringJoiner params = new StringJoiner(" ");
        if (ArrayUtil.isEmpty(paramsArray)) {
            return params.toString();
        }
        for (Object o : paramsArray) {
            if (ObjectUtil.isNotNull(o) && !isFilterObject(o)) {
                params.add(JsonUtils.toJsonString(o));
            }
        }
        return params.toString();
    }

    /**
     * 判断是否需要过滤的对象
     */
    @SuppressWarnings("rawtypes")
    private boolean isFilterObject(final Object o) {
        Class<?> clazz = o.getClass();
        if (clazz.isArray()) {
            return MultipartFile.class.isAssignableFrom(clazz.getComponentType());
        } else if (Collection.class.isAssignableFrom(clazz)) {
            Collection collection = (Collection) o;
            for (Object value : collection) {
                return value instanceof MultipartFile;
            }
        } else if (Map.class.isAssignableFrom(clazz)) {
            Map map = (Map) o;
            for (Object value : map.values()) {
                return value instanceof MultipartFile;
            }
        }
        return o instanceof MultipartFile 
            || o instanceof HttpServletRequest 
            || o instanceof HttpServletResponse
            || o instanceof BindingResult;
    }

    /**
     * 获取提示消息
     */
    private String getMessage(String messageKey, String defaultMessage) {
        if (StringUtils.startsWith(messageKey, "{") && StringUtils.endsWith(messageKey, "}")) {
            String code = StringUtils.substring(messageKey, 1, messageKey.length() - 1);
            return MessageUtils.message(code);
        }
        return StringUtils.isNotBlank(messageKey) ? messageKey : defaultMessage;
    }
}

