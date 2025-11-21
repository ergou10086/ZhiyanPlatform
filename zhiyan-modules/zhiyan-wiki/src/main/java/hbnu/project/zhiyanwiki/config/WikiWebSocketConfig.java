package hbnu.project.zhiyanwiki.config;

import hbnu.project.zhiyanwiki.interceptor.WikiWebSocketInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Wiki 用的 WebSocket 配置类
 * 启用 STOMP 消息代理，支持 Wiki 协同编辑
 *
 * @author ErgouTree
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WikiWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WikiWebSocketInterceptor wikiWebSocketInterceptor;

    /**
     * 配置消息代理
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单的内存消息代理，用于向客户端发送消息
        // 客户端订阅 /topic/wiki/{pageId} 接收广播消息
        config.enableSimpleBroker("/topic", "/user");
        // 客户端发送消息的目标前缀
        config.setApplicationDestinationPrefixes("/app");
        // 用户专属消息前缀
        config.setUserDestinationPrefix("/user");
    }


    /**
     * 注册 STOMP 端点
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/zhiyan/wiki/ws")
                .setAllowedOriginPatterns("*")  // 允许跨域，生产环境应配置具体域名
                .withSockJS();  // 启用 SockJS 支持，提供降级方案
    }


    /**
     * 配置客户端入站通道拦截器，用于用户认证
     */
    @Override
    public void configureClientInboundChannel(org.springframework.messaging.simp.config.ChannelRegistration registration) {
        registration.interceptors(wikiWebSocketInterceptor);
    }
}
