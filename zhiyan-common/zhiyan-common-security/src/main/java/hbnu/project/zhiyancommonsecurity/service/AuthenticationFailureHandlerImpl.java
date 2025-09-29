package hbnu.project.zhiyancommonsecurity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonbasic.utils.ip.IpUtils;
import hbnu.project.zhiyancommonbasic.utils.StringUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 认证失败处理器
 * 处理各种用户认证失败情况，返回相应的错误信息和状态码
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFailureHandlerImpl implements AuthenticationFailureHandler {

    private final LoginFailureService loginFailureService;
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String email = request.getParameter("email");
        String clientIp = IpUtils.getIpAddr(request);

        log.warn("认证失败 - 邮箱: {}, IP: {}, 异常类型: {}, 异常信息: {}",
                email, clientIp, exception.getClass().getSimpleName(), exception.getMessage());

        // 记录登录失败（如果不是账户锁定异常）
        boolean shouldLock = false;
        if (!(exception instanceof LockedException) && StringUtils.isNotBlank(email)) {
            shouldLock = loginFailureService.recordLoginFailure(email, request);
        }

        // 构建响应信息
        R<Object> result = buildFailureResponse(exception, email, shouldLock);

        // 设置响应
        writeResponse(response, result);
    }


    /**
     * 构建失败响应
     */
    private R<Object> buildFailureResponse(AuthenticationException exception, String email, boolean shouldLock) {
        String message;
        int code = 401;

        if(exception instanceof BadCredentialsException){
            // 密码错误
            if (shouldLock) {
                message = "邮箱或密码错误，账户已被临时锁定";
                // 423 Locked
                code = 423;
            } else{
                int remainingAttempts = loginFailureService.getRemainingAttempts(email);
                if (remainingAttempts > 0) {
                    message = String.format("邮箱或密码错误，还有 %d 次尝试机会", remainingAttempts);
                } else {
                    message = "邮箱或密码错误";
                }
            }
        }else if(exception instanceof LockedException){
            // 账户被锁定
            long remainingTime = loginFailureService.getLockRemainingTime(email);
            if (remainingTime > 0) {
                message = String.format("账户已被锁定，请 %d 分钟后重试", remainingTime);
            } else {
                message = "账户已被锁定，请联系管理员";
            }
            code = 423;
        } else if(exception instanceof DisabledException){
            // 账户被禁用
            message = "账户已被禁用，请联系管理员";
            // 403 Forbidden
            code = 403;
        } else {
            // 其他认证失败
            message = "登录失败，请稍后重试";
        }

        return R.fail(code, message);
    }


    /**
     * 写入响应
     */
    private void writeResponse(HttpServletResponse response, R<Object> result) throws IOException {
        // 统一返回200，错误信息在响应体中
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        String jsonResponse = objectMapper.writeValueAsString(result);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }


    /**
     * 获取失败消息（已整合到buildFailureResponse中）这个保留
     */
    private String getFailureMessage(AuthenticationException exception, String email) {
        if (exception instanceof BadCredentialsException) {
            return "邮箱或密码错误";
        } else if (exception instanceof LockedException) {
            return "账户已被锁定，请联系管理员";
        } else {
            return "登录失败，请稍后重试";
        }
    }
}
