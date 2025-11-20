package hbnu.project.zhiyancommonsse.utils;

import hbnu.project.zhiyancommonbasic.utils.SpringUtils;
import hbnu.project.zhiyancommonsse.core.SseEmitterManager;
import hbnu.project.zhiyancommonsse.dto.SseMessageDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
/**
 * SSE工具类
 *
 * @author Lion Li
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SseMessageUtils {

    private final static Boolean SSE_ENABLE = SpringUtils.getProperty("sse.enabled", Boolean.class, true);
    private static SseEmitterManager MANAGER;

    static {
        if (isEnable() && MANAGER == null) {
            try {
                MANAGER = SpringUtils.getBean(SseEmitterManager.class);
            } catch (NoSuchBeanDefinitionException e) {
                log.warn("SseEmitterManager bean not found, SSE functionality will be disabled. " +
                        "Please ensure 'sse.enabled=true' in your configuration.");
                MANAGER = null;
            } catch (Exception e) {
                log.warn("Failed to initialize SseEmitterManager: {}", e.getMessage());
                MANAGER = null;
            }
        }
    }

    /**
     * 向指定的SSE会话发送消息
     *
     * @param userId  要发送消息的用户id
     * @param message 要发送的消息内容
     */
    public static void sendMessage(Long userId, String message) {
        if (!isEnable() || MANAGER == null) {
            return;
        }
        MANAGER.sendMessage(userId, message);
    }

    /**
     * 本机全用户会话发送消息
     *
     * @param message 要发送的消息内容
     */
    public static void sendMessage(String message) {
        if (!isEnable() || MANAGER == null) {
            return;
        }
        MANAGER.sendMessage(message);
    }

    /**
     * 发布SSE订阅消息
     *
     * @param sseMessageDto 要发布的SSE消息对象
     */
    public static void publishMessage(SseMessageDto sseMessageDto) {
        if (!isEnable() || MANAGER == null) {
            return;
        }
        MANAGER.publishMessage(sseMessageDto);
    }

    /**
     * 向所有的用户发布订阅的消息(群发)
     *
     * @param message 要发布的消息内容
     */
    public static void publishAll(String message) {
        if (!isEnable() || MANAGER == null) {
            return;
        }
        MANAGER.publishAll(message);
    }

    /**
     * 是否开启
     */
    public static Boolean isEnable() {
        return SSE_ENABLE;
    }

}
