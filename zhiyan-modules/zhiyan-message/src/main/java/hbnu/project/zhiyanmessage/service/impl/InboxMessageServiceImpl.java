package hbnu.project.zhiyanmessage.service.impl;


import hbnu.project.zhiyanmessage.model.entity.Message;
import hbnu.project.zhiyanmessage.model.entity.MessageReception;
import hbnu.project.zhiyanmessage.model.entity.MessageSendRecord;
import hbnu.project.zhiyanmessage.model.enums.MessagePriority;
import hbnu.project.zhiyanmessage.model.enums.MessageScene;
import hbnu.project.zhiyanmessage.model.enums.MessageType;
import hbnu.project.zhiyanmessage.repository.MessageRepository;
import hbnu.project.zhiyanmessage.repository.MessageSendRecordRepository;
import hbnu.project.zhiyanmessage.service.InboxMessageService;
import hbnu.project.zhiyanmessage.utils.SseMessagePushEdgeUtils;
import hbnu.project.zhiyanmessage.repository.MessageReceptionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 站内消息服务类实现
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InboxMessageServiceImpl implements InboxMessageService {

    private final MessageRepository messageBodyRepository;
    private final MessageReceptionRepository messageRecipientRepository;
    private final MessageSendRecordRepository messageSendRecordRepository;
    private final SseMessagePushEdgeUtils sseMessagePushEdgeUtils;

    /**
     * 发送一条“个人消息”（向单个的收件人发送）
     *
     * @param scene          消息场景
     * @param senderId       发送人ID（系统消息可为 null）
     * @param receiverId     收件人ID
     * @param title          标题
     * @param content        正文
     * @param businessId     业务ID（任务ID、项目ID等，可为空）
     * @param businessType   业务类型（"TASK" / "PROJECT" / "ACHIEVEMENT" 等，可为空）
     * @param extendDataJson 扩展字段 JSON（跳转链接、额外参数等）
     * @return MessageBody（便于调用方根据需要做调试或链路跟踪）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Message sendPersonalMessage(MessageScene scene, Long senderId, Long receiverId, String title, String content, Long businessId, String businessType, String extendDataJson) {
        // 构建消息体
        Message messageBody = Message.builder()
                .senderId(senderId)
                .messageType(MessageType.PERSONAL)
                .scene(scene)
                .priority(MessagePriority.ofScene(scene))
                .title(title)
                .content(content)
                .businessId(businessId)
                .businessType(businessType)
                .extendData(extendDataJson)
                .triggerTime(LocalDateTime.now())
                .build();

        // 构建收件人记录
        MessageReception recipient = MessageReception.builder()
                .receiverId(receiverId)
                .sceneCode(scene.name())
                .readFlag(false)
                .deleted(false)
                .triggerTime(messageBody.getTriggerTime())
                .build();

        messageBody.addRecipient(recipient);

        // 持久化消息
        messageBody = messageBodyRepository.save(messageBody);

        // 通过SSE推送消息
        try {
            sseMessagePushEdgeUtils.pushMessageViaSse(receiverId, messageBody);
        } catch (Exception e) {
            log.error("SSE推送消息失败,但消息已保存: receiverId={}, messageBodyId={}",
                    receiverId, messageBody.getId(), e);
        }

        log.info("发送个人消息成功: messageBodyId={}, receiverId={}, scene={}",
                messageBody.getId(), receiverId, scene);

        return messageBody;
    }


    /**
     * 发送多收件人消息(多收件人,一条消息体,多条收件人记录)
     *
     * @param scene          消息场景
     * @param senderId       发送人ID
     * @param receiverIds    收件人ID集合
     * @param title          标题
     * @param content        正文
     * @param businessId     业务ID
     * @param businessType   业务类型
     * @param extendDataJson 扩展数据
     * @return MessageBody
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Message sendBatchPersonalMessage(MessageScene scene, Long senderId,
                                                Collection<Long> receiverIds, String title,
                                                String content, Long businessId,
                                                String businessType, String extendDataJson) {
        if (receiverIds == null || receiverIds.isEmpty()) {
            log.warn("批量发送消息失败: 收件人列表为空");
            return null;
        }

        // 构建消息体
        Message messageBody = Message.builder()
                .senderId(senderId)
                .messageType(MessageType.PERSONAL)
                .scene(scene)
                .priority(MessagePriority.ofScene(scene))
                .title(title)
                .content(content)
                .businessId(businessId)
                .businessType(businessType)
                .extendData(extendDataJson)
                .triggerTime(LocalDateTime.now())
                .build();

        // 批量创建收件人记录
        Message finalMessageBody = messageBody;
        List<MessageReception> recipients = receiverIds.stream()
                .map(receiverId -> MessageReception.builder()
                        .receiverId(receiverId)
                        .sceneCode(scene.name())
                        .readFlag(false)
                        .deleted(false)
                        .triggerTime(finalMessageBody.getTriggerTime())
                        .build())
                .collect(Collectors.toList());

        messageBody.addRecipients(recipients);

        // 保存消息
        messageBody = messageBodyRepository.save(messageBody);

        // 创建发送记录
        MessageSendRecord sendRecord = MessageSendRecord.builder()
                .messageBodyId(messageBody.getId())
                .sendTime(LocalDateTime.now())
                .totalRecipients(receiverIds.size())
                .successCount(0)
                .failedCount(0)
                .status("SENDING")
                .build();
        messageSendRecordRepository.save(sendRecord);

        // 通过SSE批量推送消息
        int successCount = 0;
        int failedCount = 0;
        for (Long receiverId : receiverIds) {
            try {
                sseMessagePushEdgeUtils.pushMessageViaSse(receiverId, messageBody);
                successCount++;
            } catch (Exception e) {
                log.error("SSE推送消息失败: receiverId={}, messageBodyId={}",
                        receiverId, messageBody.getId(), e);
                failedCount++;
            }
        }

        // 更新发送记录
        sendRecord.setSuccessCount(successCount);
        sendRecord.setFailedCount(failedCount);
        sendRecord.setStatus(failedCount == 0 ? "SUCCESS" :
                (successCount > 0 ? "PARTIAL_FAILED" : "ALL_FAILED"));
        messageSendRecordRepository.save(sendRecord);

        log.info("批量发送消息完成: messageBodyId={}, total={}, success={}, failed={}",
                messageBody.getId(), receiverIds.size(), successCount, failedCount);

        return messageBody;
    }


    /**
     * 向全体用户发送消息
     * 向全体用户发送消息只能是管理员发送
     * 这种消息类型通常只有系统消息，不用管理业务模块
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Message sendAllPersonalMessage(MessageScene scene, Long senderId, String title, String content, String extendDataJson) {
        // 创建广播消息体
        Message messageBody = Message.builder()
                .senderId(senderId)
                .messageType(MessageType.BROADCAST)
                .scene(scene)
                .priority(MessagePriority.ofScene(scene))
                .title(title)
                .content(content)
                .extendData(extendDataJson)
                .triggerTime(LocalDateTime.now())
                .build();

        // 广播消息不创建收件人记录，通过SSE直接推送
        messageBody = messageBodyRepository.save(messageBody);

        // 通过SSE广播消息
        try {
            sseMessagePushEdgeUtils.pushBroadcastMessageViaSse(messageBody);
        } catch (Exception e) {
            log.error("SSE广播消息失败,但消息已保存: messageBodyId={}", messageBody.getId(), e);
        }

        log.info("发送广播消息成功: messageBodyId={}, scene={}",
                messageBody.getId(), scene);

        return messageBody;
    }


    /**
     * 将某条消息标记为已读
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long receiverId, Long recipientId) {
        Optional<MessageReception> messageRecipientOptional = messageRecipientRepository.findByIdAndReceiverIdAndDeletedFalse(recipientId, receiverId);

        if (messageRecipientOptional.isPresent()) {
            MessageReception messageRecipient = messageRecipientOptional.get();
            if(!messageRecipient.getReadFlag()){
                messageRecipient.markAsRead();
                messageRecipientRepository.save(messageRecipient);
                log.info("标记消息已读: recipientId={}, receiverId={}", recipientId, receiverId);
            }
        }else {
            log.warn("标记消息已读失败: 消息不存在或已删除, recipientId={}, receiverId={}",
                    recipientId, receiverId);
        }
    }


    /**
     * 将当前用户全部未读消息标记为已读
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long receiverId) {
        List<MessageReception> unreadList = messageRecipientRepository.findByReceiverIdAndReadFlagFalseAndDeletedFalse(receiverId);

        unreadList.forEach(MessageReception::markAsRead);
        messageRecipientRepository.saveAll(unreadList);

        log.info("全部标记已读: receiverId={}, count={}", receiverId, unreadList.size());
    }


    /**
     * 删除某条消息（软删除）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMessage(Long receiverId, Long recipientId) {
        Optional<MessageReception> optional = messageRecipientRepository.findByIdAndReceiverIdAndDeletedFalse(recipientId, receiverId);

        if (optional.isPresent()) {
            MessageReception messageRecipient = optional.get();
            messageRecipient.softDelete();
            messageRecipientRepository.save(messageRecipient);
            log.info("删除消息: recipientId={}, receiverId={}", recipientId, receiverId);
        } else {
            log.warn("删除消息失败: 消息不存在或已删除, recipientId={}, receiverId={}",
                    recipientId, receiverId);
        }
    }


    /**
     * 清空当前用户的所有消息（软删除）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearAll(Long receiverId) {
        List<MessageReception> allList = messageRecipientRepository
                .findByReceiverIdAndDeletedFalse(receiverId);

        allList.forEach(MessageReception::softDelete);
        messageRecipientRepository.saveAll(allList);

        log.info("清空所有消息: receiverId={}, count={}", receiverId, allList.size());
    }


    /**
     * 用户收件箱分页查询（全部）
     * 自己隔这二级构建一下这个方法
     */
    @Override
    public Page<MessageReception> pageInbox(Long receiverId, Pageable pageable) {
        return messageRecipientRepository.findByReceiverIdAndDeletedFalseOrderByTriggerTimeDesc(
                receiverId, pageable);
    }


    /**
     * 用户未读消息分页查询
     */
    @Override
    public Page<MessageReception> pageUnread(Long receiverId, Pageable pageable) {
        return messageRecipientRepository.findByReceiverIdAndReadFlagFalseAndDeletedFalseOrderByTriggerTimeDesc(
                receiverId, pageable);
    }


    /**
     * 未读数量
     */
    @Override
    public long countUnread(Long receiverId) {
        return messageRecipientRepository.countByReceiverIdAndReadFlagFalseAndDeletedFalse(receiverId);
    }
}
