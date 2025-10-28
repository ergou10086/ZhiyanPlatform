package hbnu.project.zhiyancommonsse.core;

import hbnu.project.zhiyancommonbasic.exception.SseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dify 流式响应发射器管理器
 * 专门用于管理 AI 对话的流式响应
 *
 * @author ErgouTree
 */
@Slf4j
public class DifyStreamEmitter {

    /**
     * 存储每个会话的 SSE 发射器
     * Key: conversationId, Value: SseEmitter
     */
    private static final Map<String, SseEmitter> CONVERSATION_EMITTERS = new ConcurrentHashMap<>();

    /**
     * SSE 超时时间：10分钟（AI 对话可能较长）
     */
    private static final Long SSE_TIMEOUT = 600000L;


    /**
     * 创建新的对话流式发射器
     *
     * @param conversationId 对话 ID
     * @param userId 用户 ID
     * @return SseEmitter 实例
     */
    public static SseEmitter createEmitter(String conversationId, Long userId) {
        SseEmitter sseEmitter = new SseEmitter(SSE_TIMEOUT);

        // 注册回调
        sseEmitter.onCompletion(() -> {
            log.info("[Dify SSE] 对话完成: conversationId={}, userId={}", conversationId, userId);
            CONVERSATION_EMITTERS.remove(conversationId);
        });

        sseEmitter.onTimeout(() -> {
            log.warn("[Dify SSE] 对话超时: conversationId={}, userId={}", conversationId, userId);
            CONVERSATION_EMITTERS.remove(conversationId);
        });

        sseEmitter.onError((e) -> {
            log.error("[Dify SSE] 对话错误: conversationId={}, userId={}", conversationId, userId, e);
            CONVERSATION_EMITTERS.remove(conversationId);
        });

        // 存储发射器
        CONVERSATION_EMITTERS.put(conversationId, sseEmitter);

        // 发送连接成功消息
        try {
            sseEmitter.send(SseEmitter.event()
                    .name("connected")
                    .data("连接成功"));
        }catch (SseException | IOException e){
            log.error("[Dify SSE] 发送连接消息失败", e);
            CONVERSATION_EMITTERS.remove(conversationId);
        }

        log.info("[Dify SSE] 创建流式发射器: conversationId={}, userId={}", conversationId, userId);

        return sseEmitter;
    }


    /**
     * 发送流式消息
     *
     * @param conversationId 对话 ID
     * @param message 消息内容
     * @param eventType 事件类型 (message/answer/error/done)
     */
    public static void sendMessage(String conversationId, String message, String eventType) {
        SseEmitter emitter = CONVERSATION_EMITTERS.get(conversationId);

        if(emitter == null){
            log.warn("[Dify SSE] 发射器不存在: conversationId={}", conversationId);
            return;
        }

        try {
            hbnu.project.zhiyancommonsse.dto.DifyStreamMessage streamMessage = hbnu.project.zhiyancommonsse.dto.DifyStreamMessage.builder()
                    .conversationId(conversationId)
                    .event(eventType)
                    .data(message)
                    .timestamp(System.currentTimeMillis())
                    .build();

            emitter.send(SseEmitter.event()
                    .name(eventType)
                    .data(streamMessage));

        } catch (IOException e) {
            log.error("[Dify SSE] 发送消息失败: conversationId={}", conversationId, e);
            completeEmitter(conversationId);
        }
    }


    /**
     * 发送文本块（用于流式输出）
     *
     * @param conversationId 对话 ID
     * @param textChunk 文本块
     */
    public static void sendChunk(String conversationId, String textChunk) {
        sendMessage(conversationId, textChunk, "chunk");
    }


    /**
     * 发送完整回答
     *
     * @param conversationId 对话 ID
     * @param fullAnswer 完整回答
     */
    public static void sendAnswer(String conversationId, String fullAnswer) {
        sendMessage(conversationId, fullAnswer, "answer");
    }


    /**
     * 发送错误消息
     *
     * @param conversationId 对话 ID
     * @param errorMessage 错误消息
     */
    public static void sendError(String conversationId, String errorMessage) {
        sendMessage(conversationId, errorMessage, "error");
        completeEmitter(conversationId);
    }


    /**
     * 完成并关闭发射器
     *
     * @param conversationId 对话 ID
     */
    public static void completeEmitter(String conversationId) {
        SseEmitter emitter = CONVERSATION_EMITTERS.remove(conversationId);

        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("done")
                        .data("对话完成"));
                emitter.complete();
                log.info("[Dify SSE] 关闭发射器: conversationId={}", conversationId);
            } catch (Exception e) {
                log.error("[Dify SSE] 关闭发射器失败: conversationId={}", conversationId, e);
            }
        }
    }


    /**
     * 检查发射器是否存在
     *
     * @param conversationId 对话 ID
     * @return 是否存在
     */
    public static boolean exists(String conversationId) {
        return CONVERSATION_EMITTERS.containsKey(conversationId);
    }


    /**
     * 获取当前活跃的对话数量
     *
     * @return 活跃对话数
     */
    public static int getActiveCount() {
        return CONVERSATION_EMITTERS.size();
    }
}
