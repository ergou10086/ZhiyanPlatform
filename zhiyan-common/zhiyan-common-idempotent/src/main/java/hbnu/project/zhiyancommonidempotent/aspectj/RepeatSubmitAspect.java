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
import hbnu.project.zhiyancommonidempotent.annotation.RepeatSubmit;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import hbnu.project.zhiyancommonredis.utils.RedisUtils;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.StringJoiner;

/**
 * 防止重复提交(参考美团GTIS防重系统)
 * 注意：推荐使用 @Idempotent 注解替代此注解，功能更加强大
 *
 * @author yui
 * 推荐使用 @Idempotent 注解，支持更多幂等策略   -- yui
 */
@Slf4j
@Aspect
public class RepeatSubmitAspect {

    private static final ThreadLocal<String> KEY_CACHE = new ThreadLocal<>();

    @Before("@annotation(repeatSubmit)")
    public void doBefore(JoinPoint point, RepeatSubmit repeatSubmit) {
        // 如果注解不为0 则使用注解数值
        long interval = repeatSubmit.timeUnit().toMillis(repeatSubmit.interval());

        if (interval < 1000) {
            throw new ServiceException("重复提交间隔时间不能小于'1'秒");
        }
        
        HttpServletRequest request = ServletUtils.getRequest();
        if (request == null) {
            throw new ServiceException("无法获取当前请求对象");
        }
        
        String nowParams = argsArrayToString(point.getArgs());

        // 请求地址（作为存放cache的key值）
        String url = request.getRequestURI();

        // 唯一值（优先使用token，没有则使用IP地址）
        String submitKey = StringUtils.trimToEmpty(request.getHeader(SaManager.getConfig().getTokenName()));
        if (StringUtils.isBlank(submitKey)) {
            submitKey = IpUtils.getIpAddr(request);
        }

        submitKey = SecureUtil.md5(submitKey + ":" + nowParams);
        // 唯一标识（指定key + url + 消息头）
        String cacheRepeatKey = GlobalConstants.REPEAT_SUBMIT_KEY + url + ":" + submitKey;
        
        if (RedisUtils.setObjectIfAbsent(cacheRepeatKey, System.currentTimeMillis(), Duration.ofMillis(interval))) {
            KEY_CACHE.set(cacheRepeatKey);
            log.debug("防重复提交检查通过: {}", cacheRepeatKey);
        } else {
            String message = repeatSubmit.message();
            if (StringUtils.startsWith(message, "{") && StringUtils.endsWith(message, "}")) {
                message = MessageUtils.message(StringUtils.substring(message, 1, message.length() - 1));
            }
            log.warn("检测到重复提交: {}", cacheRepeatKey);
            throw new ServiceException(message);
        }
    }

    /**
     * 处理完请求后执行
     *
     * @param joinPoint 切点
     */
    @AfterReturning(pointcut = "@annotation(repeatSubmit)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, RepeatSubmit repeatSubmit, Object jsonResult) {
        String cacheKey = KEY_CACHE.get();
        if (StringUtils.isBlank(cacheKey)) {
            return;
        }
        
        try {
            if (jsonResult instanceof R<?> r) {
                // 成功则不删除redis数据 保证在有效时间内无法重复提交
                if (r.getCode() == R.SUCCESS) {
                    log.debug("请求成功，保留防重key: {}", cacheKey);
                    return;
                }
                // 失败则删除key，允许重试
                RedisUtils.deleteObject(cacheKey);
                log.debug("请求失败，删除防重key允许重试: {}", cacheKey);
            }
        } finally {
            KEY_CACHE.remove();
        }
    }

    /**
     * 拦截异常操作
     *
     * @param joinPoint 切点
     * @param e         异常
     */
    @AfterThrowing(value = "@annotation(repeatSubmit)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, RepeatSubmit repeatSubmit, Exception e) {
        String cacheKey = KEY_CACHE.get();
        if (StringUtils.isNotBlank(cacheKey)) {
            RedisUtils.deleteObject(cacheKey);
            log.debug("请求异常，删除防重key允许重试: {}", cacheKey);
        }
        KEY_CACHE.remove();
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
     * 判断是否需要过滤的对象。
     *
     * @param o 对象信息。
     * @return 如果是需要过滤的对象，则返回true；否则返回false。
     */
    @SuppressWarnings("rawtypes")
    public boolean isFilterObject(final Object o) {
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
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse
            || o instanceof BindingResult;
    }

}
