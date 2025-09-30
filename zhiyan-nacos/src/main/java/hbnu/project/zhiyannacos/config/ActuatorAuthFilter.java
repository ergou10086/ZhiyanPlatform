package hbnu.project.zhiyannacos.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Actuator 端点认证过滤器
 * 用于保护 Spring Boot Actuator 监控端点，通过 Basic 认证方式验证访问权限
 *
 * @author ErgouTree
 */
public class ActuatorAuthFilter implements Filter {

    /**
     * 预定义的认证用户名
     */
    private final String username;

    /**
     * 预定义的认证密码
     */
    private final String password;

    /**
     * 构造方法，初始化认证所需的用户名和密码
     *
     * @param username 认证用户名
     * @param password 认证密码
     */
    public ActuatorAuthFilter(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * 过滤器核心方法，实现对请求的认证逻辑
     *
     * @param servletRequest  请求对象
     * @param servletResponse 响应对象
     * @param filterChain     过滤器链，用于传递处理后的请求
     * @throws IOException      IO异常
     * @throws ServletException Servlet异常
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        // 将请求和响应对象转换为HTTP类型，以便处理HTTP特定功能
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 获取请求头中的Authorization信息，该字段用于存储认证凭证
        String authHeader = request.getHeader("Authorization");

        // 检查Authorization头是否存在且以"Basic "开头（Basic认证的标识）
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            // 若认证信息缺失或格式不正确，设置响应头要求客户端提供认证，并返回401未授权状态
            response.setHeader("WWW-Authenticate", "Basic realm=\"realm\"");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }

        // 提取Base64编码的凭证部分（去除"Basic "前缀）
        String base64Credentials = authHeader.substring("Basic ".length());
        // 解码Base64字符串为字节数组
        byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
        // 将字节数组转换为UTF-8编码的字符串（格式为"username:password"）
        String credentials = new String(credDecoded, StandardCharsets.UTF_8);
        // 分割用户名和密码，限制分割为两部分，防止密码中包含冒号导致解析错误
        final String[] split = credentials.split(":", 2);

        // 检查分割结果是否符合预期（必须包含用户名和密码两部分）
        if (split.length != 2) {
            response.setHeader("WWW-Authenticate", "Basic realm=\"realm\"");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }

        // 验证用户名和密码是否与预定义的值匹配
        if (username.equals(split[0]) && password.equals(split[1])) {
            // 认证成功，将请求传递给过滤器链中的下一个组件处理
            filterChain.doFilter(request, response);
        } else {
            // 认证失败，返回401未授权状态
            response.setHeader("WWW-Authenticate", "Basic realm=\"realm\"");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }
    }

    /**
     * 过滤器初始化方法
     * 可用于初始化过滤器所需的资源，此处无需实现
     *
     * @param filterConfig 过滤器配置对象
     * @throws ServletException Servlet异常
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 初始化方法，留空即可
    }

    /**
     * 过滤器销毁方法
     * 可用于释放过滤器占用的资源，此处无需实现
     */
    @Override
    public void destroy() {
        // 销毁方法，留空即可
    }

}