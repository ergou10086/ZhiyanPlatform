package hbnu.project.zhiyanproject.client; // 建议放在config包中

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.util.StringUtils;

/**
 * Feign 请求拦截器
 * 用于在 Feign 请求中添加认证信息
 *
 * @author yxy
 */

@Component
public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        // 从当前请求上下文中获取认证信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String authorizationHeader = request.getHeader("Authorization");
            if (StringUtils.hasText(authorizationHeader)) {
                // 将认证头添加到 Feign 请求中
                template.header("Authorization", authorizationHeader);
                // 添加调试日志
                System.out.println("Feign Interceptor: Added Authorization header: " + authorizationHeader.substring(0, Math.min(20, authorizationHeader.length())) + "...");
            } else {
                System.out.println("Feign Interceptor: No Authorization header found in current request");
            }
        } else {
            System.out.println("Feign Interceptor: No request attributes found");
        }
    }
}