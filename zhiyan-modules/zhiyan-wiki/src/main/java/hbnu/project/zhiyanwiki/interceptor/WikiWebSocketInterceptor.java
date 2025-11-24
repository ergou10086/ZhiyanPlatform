package hbnu.project.zhiyanwiki.interceptor;

import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.security.Principal;

/**
 * WebSocket 消息拦截器
 * 用于在连接建立时进行用户认证
 *
 * @author ErgouTree
 */
@Slf4j
@Component
public class WikiWebSocketInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel){
        // Message 对象中提取出专门用于处理 STOMP 协议的头信息访问器
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // 只处理客户端发起的连接请求（CONNECT 命令），而不是其他类型的 STOMP 消息
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 在连接时进行用户认证
            try {
                // 从 Spring Security 上下文获取用户信息
                Long userId = SecurityUtils.getUserId();
                if (userId != null) {
                    // 设置用户 Principal,将用户身份与 WebSocket 会话绑定
                    Principal principal = new UsernamePasswordAuthenticationToken(
                            userId.toString(), null, null);

                    accessor.setUser(principal);
                    log.info("WebSocket 用户连接: userId={}, sessionId={}",
                            userId, accessor.getSessionId());
                } else {
                    log.warn("WebSocket 连接失败: 用户未认证, sessionId={}",
                            accessor.getSessionId());
                    throw new SecurityException("用户未认证");
                }
            } catch (SecurityException e) {
                log.error("WebSocket 认证失败: sessionId={}", accessor.getSessionId(), e);
                throw e;
            } catch (Exception e) {
                log.error("WebSocket 认证异常: sessionId={}", accessor.getSessionId(), e);
                throw new SecurityException("认证失败: " + e.getMessage());
            }
        }

        return message;
    }


    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        // 可以在这里记录消息发送日志，懒得记录了
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        if (ex != null) {
            StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
            log.error("WebSocket 消息发送失败: sessionId={}, command={}",
                    accessor.getSessionId(), accessor.getCommand(), ex);
        }
    }
}
