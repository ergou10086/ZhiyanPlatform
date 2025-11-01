package hbnu.project.common.sentinel.handler;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;

/**
 * 全局 Sentinel 限流异常处理器
 * <p>
 * 拦截所有 Sentinel 限流异常，统一处理并返回友好的响应
 * </p>
 *
 * @author ErgouTree
 */
@Slf4j
@RequiredArgsConstructor
public class GlobalBlockExceptionHandler implements BlockExceptionHandler {

    private final SentinelExceptionHandler sentinelExceptionHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, BlockException e) throws Exception {
        log.warn("[Sentinel] 全局拦截限流异常，请求路径：{}，异常类型：{}", 
            request.getRequestURI(), 
            e.getClass().getSimpleName());

        // 调用自定义处理器
        Object result = sentinelExceptionHandler.handle(request, e);

        // 设置响应
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        
        // 写入响应
        String jsonResult = objectMapper.writeValueAsString(result);
        response.getWriter().write(jsonResult);
        response.getWriter().flush();
    }
}

