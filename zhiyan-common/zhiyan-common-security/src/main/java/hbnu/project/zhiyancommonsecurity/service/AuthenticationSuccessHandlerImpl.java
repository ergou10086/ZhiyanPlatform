package hbnu.project.zhiyancommonsecurity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonbasic.utils.IpUtils;
import hbnu.project.zhiyancommonsecurity.context.LoginUserBody;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 认证成功处理器
 * 处理登录成功后的操作
 * @author ErgouTree
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {

    private final LoginFailureService loginFailureService;
    private final ObjectMapper objectMapper = new ObjectMapper();


    /**
     * 新版本带过滤器链
     * 认证成功处理方法
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        // 获取登录用户信息
        LoginUserBody loginUser = (LoginUserBody) authentication.getPrincipal();
        String email = loginUser.getEmail();
        String clientIp = IpUtils.getIpAddr(request);

        log.info("用户登录成功 - 邮箱: {}, 用户ID: {}, IP: {}", email, loginUser.getUserId(), clientIp);

        // 清除登录失败记录
        loginFailureService.clearLoginFailure(email, request);

        // 构建成功响应
        R<Object> result = R.ok("登录成功");

        // 写入响应
        writeResponse(response, result);
    }


    /**
     * 旧版本的认证成功处理方法
     * 没有FilterChain参数，无法继续执行过滤器链
     * 通常用于直接返回响应给客户端的场景
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 获取登录用户信息
        LoginUserBody loginUser = (LoginUserBody) authentication.getPrincipal();
        String email = loginUser.getEmail();
        String clientIp = IpUtils.getIpAddr(request);

        log.info("用户登录成功(旧方法) - 邮箱: {}, 用户ID: {}, IP: {}", email, loginUser.getUserId(), clientIp);

        // 清除登录失败记录
        loginFailureService.clearLoginFailure(email, request);

        // 构建成功响应
        R<Object> result = R.ok("登录成功");

        // 写入响应
        writeResponse(response, result);

        // 注意：旧方法没有FilterChain，不需要也无法调用chain.doFilter()
        // 响应在这里已经完成，不会继续执行后续过滤器
    }


    /**
     * 写入响应
     */
    private void writeResponse(HttpServletResponse response, R<Object> result) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        String jsonResponse = objectMapper.writeValueAsString(result);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}
