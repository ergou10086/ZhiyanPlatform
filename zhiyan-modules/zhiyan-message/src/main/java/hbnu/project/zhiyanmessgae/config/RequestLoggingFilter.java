package hbnu.project.zhiyanmessgae.config;



import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 请求日志过滤器
 *
 * @author yxy
 */
@Slf4j
@Component
@Order(1)
public class RequestLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        log.info("=== Incoming Request ===");
        log.info("Method: {}", httpRequest.getMethod());
        log.info("URL: {}", httpRequest.getRequestURL());
        log.info("Authorization: {}", httpRequest.getHeader("Authorization"));
        log.info("Content-Type: {}", httpRequest.getHeader("Content-Type"));
        log.info("User-Agent: {}", httpRequest.getHeader("User-Agent"));
        log.info("Remote Addr: {}", httpRequest.getRemoteAddr());
        log.info("=========================");

        chain.doFilter(request, response);
    }
}