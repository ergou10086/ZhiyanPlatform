package hbnu.project.zhiyanmessgae.utils;

import hbnu.project.zhiyancommonbasic.utils.JsonUtils;
import hbnu.project.zhiyancommonsse.dto.SseMessageDto;
import hbnu.project.zhiyancommonsse.utils.SseMessageUtils;
import hbnu.project.zhiyanmessgae.model.converter.MessageConverter;
import hbnu.project.zhiyanmessgae.model.entity.MessageBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 使用sse实现的消息推送逻辑
 *
 * @author ErgouTree
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SseMessagePushEdgeUtils {

    private final MessageConverter messageConverter;

    /**
     * 通过SSE广播消息
     */
    public void pushBroadcastMessageViaSse(MessageBody messageBody) {
        try {
            String messageJson = JsonUtils.toJsonString(
                    messageConverter.toNotificationDTO(messageBody)
            );
            SseMessageUtils.publishAll(messageJson);
        } catch (Exception e) {
            log.error("SSE广播消息序列化失败: messageBodyId={}", messageBody.getId(), e);
            throw new RuntimeException("消息序列化失败", e);
        }
    }

    /**
     * 通过SSE推送消息给单个用户
     */
    public void pushMessageViaSse(Long receiverId, MessageBody messageBody) {
        try {
            String messageJson = JsonUtils.toJsonString(
                    messageConverter.toNotificationDTO(messageBody)
            );
            SseMessageDto sseMessageDto = new SseMessageDto();
            sseMessageDto.setUserIds(List.of(receiverId));
            sseMessageDto.setMessage(messageJson);
            SseMessageUtils.publishMessage(sseMessageDto);
        } catch (Exception e) {
            log.error("SSE推送消息序列化失败: receiverId={}, messageBodyId={}",
                    receiverId, messageBody.getId(), e);
            throw new RuntimeException("消息序列化失败", e);
        }
    }
}