package hbnu.project.zhiyancommonsecurity.filter;

import cn.hutool.core.text.AntPathMatcher;
import hbnu.project.zhiyancommonbasic.utils.SpringUtils;
import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import hbnu.project.zhiyancommonsecurity.xss.XssProperties;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.util.List;

/**
 * 防止XSS攻击的过滤器
 *
 * @author akoiv
 */
@RequiredArgsConstructor
public class XssFilter implements Filter {

    private final XssProperties xssProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        if (handleExcludeURL(req, resp)) {
            chain.doFilter(request, response);
            return;
        }
        XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper((HttpServletRequest) request);
        chain.doFilter(xssRequest, response);
    }


    private boolean handleExcludeURL(HttpServletRequest request, HttpServletResponse response) {
        // 未启用XSS防护
        if (!xssProperties.isEnabled()) {
            return true;
        }

        String url = request.getServletPath();
        String method = request.getMethod();
        // GET DELETE 不过滤
        if (method == null || HttpMethod.GET.matches(method) || HttpMethod.DELETE.matches(method)) {
            return true;
        }
        // 每次都获取处理 支持nacos热更配置
        XssProperties properties = SpringUtils.getBean(XssProperties.class);
        String prefix = StringUtils.blankToDefault(request.getHeader("X-Forwarded-Prefix"), "");
        // 从请求头获取gateway转发的服务前缀
        List<String> excludeUrls = properties.getExcludeUrls().stream()
            .filter(x -> StringUtils.startsWith(x, prefix))
            .map(x -> x.replaceFirst(prefix, StringUtils.EMPTY))
            .toList();
        return StringUtils.matches(url, excludeUrls);
    }

    @Override
    public void destroy() {
        // 清理逻辑
    }
}
