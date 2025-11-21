package hbnu.project.zhiyanwiki.listener;

import hbnu.project.zhiyanwiki.service.WikiCollaborationService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.security.Principal;

/**
 * WebSocket 事件监听器
 * 监听连接和断开事件，清理用户编辑状态
 *
 * @author ErgouTree
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WikiWebSocketEventListener {

    private final WikiCollaborationService wikiCollaborationService;

    /**
     * 监听websocket连接建立
     */
    @EventListener
    public void handleWebSocketConnect(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = headerAccessor.getUser();

        if (principal != null) {
            try {
                Long userId = Long.parseLong(principal.getName());
                log.info("WebSocket 连接建立: userId={}, sessionId={}",
                        userId, headerAccessor.getSessionId());
            } catch (Exception e) {
                log.error("处理连接事件失败", e);
            }
        } else {
            log.warn("WebSocket 连接建立但用户未认证: sessionId={}", headerAccessor.getSessionId());
        }
    }


    /**
     * 监听 WebSocket 连接断开
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = headerAccessor.getUser();

        if (principal != null) {
            try {
                Long userId = Long.parseLong(principal.getName());
                wikiCollaborationService.leaveEditing(userId);
                log.info("WebSocket 连接断开，清理用户[{}]编辑状态: sessionId={}",
                        userId, headerAccessor.getSessionId());
            } catch (Exception e) {
                log.error("处理断开连接事件失败", e);
            }
        }
    }


    /**
     * 监听订阅事件（用户订阅某个主题）
     */
    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = headerAccessor.getUser();
        String destination = headerAccessor.getDestination();

        if (principal != null && destination != null) {
            try {
                Long userId = Long.parseLong(principal.getName());
                log.debug("用户[{}]订阅主题: {}", userId, destination);

                // 如果订阅的是编辑者列表主题，可以在这里做一些处理
                if (destination != null && destination.contains("/editors")) {
                    // 可以在这里触发编辑者列表更新
                }
            } catch (Exception e) {
                log.error("处理订阅事件失败", e);
            }
        }
    }


    /**
     * 监听取消订阅事件
     */
    @EventListener
    public void handleUnsubscribeEvent(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = headerAccessor.getUser();

        if (principal != null) {
            try {
                Long userId = Long.parseLong(principal.getName());
                log.debug("用户[{}]取消订阅: sessionId={}", userId, headerAccessor.getSessionId());
            } catch (Exception e) {
                log.error("处理取消订阅事件失败", e);
            }
        }
    }
}
