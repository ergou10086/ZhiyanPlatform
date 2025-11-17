package hbnu.project.zhiyanmessgae.service.impl;

import hbnu.project.zhiyanmessgae.model.entity.MessageBody;
import hbnu.project.zhiyanmessgae.model.entity.MessageRecipient;
import hbnu.project.zhiyanmessgae.model.enums.MessageScene;
import hbnu.project.zhiyanmessgae.service.InboxMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;

import hbnu.project.zhiyanmessgae.model.entity.MessageBody;
import hbnu.project.zhiyanmessgae.model.entity.MessageRecipient;
import hbnu.project.zhiyanmessgae.model.entity.MessageSendRecord;
import hbnu.project.zhiyanmessgae.model.enums.MessagePriority;
import hbnu.project.zhiyanmessgae.model.enums.MessageScene;
import hbnu.project.zhiyanmessgae.model.enums.MessageType;
import hbnu.project.zhiyanmessgae.repository.MessageRecipientRepository;
import hbnu.project.zhiyanmessgae.repository.MessageSendRecordRepository;
import hbnu.project.zhiyanmessgae.service.InboxMessageService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
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
    public MessageBody sendPersonalMessage(MessageScene scene, Long senderId, Long receiverId, String title, String content, Long businessId, String businessType, String extendDataJson) {
        return null;
    }


    /**
     * 发送多收件人消息（多收件人，一条消息体，多条收件人记录）
     *
     * @param scene
     * @param senderId
     * @param receiverIds
     * @param title
     * @param content
     * @param businessId
     * @param businessType
     * @param extendDataJson
     */
    @Override
    public MessageBody sendBatchPersonalMessage(MessageScene scene, Long senderId, Collection<Long> receiverIds, String title, String content, Long businessId, String businessType, String extendDataJson) {
        return null;
    }


    /**
     * 向全体用户发送消息
     * 向全体用户发送消息只能是管理员发送
     * 这种消息类型通常只有系统消息，不用管理业务模块
     *
     * @param scene
     * @param senderId
     * @param title
     * @param content
     * @param extendDataJson
     */
    @Override
    public MessageBody sendAllPersonalMessage(MessageScene scene, Long senderId, String title, String content, String extendDataJson) {
        return null;
    }


    /**
     * 用户收件箱分页查询（全部）
     *
     * @param receiverId
     * @param pageable
     */
    @Override
    public Page<MessageRecipient> pageInbox(Long receiverId, Pageable pageable) {
        return null;
    }


    /**
     * 用户未读消息分页查询
     *
     * @param receiverId
     * @param pageable
     */
    @Override
    public Page<MessageRecipient> pageUnread(Long receiverId, Pageable pageable) {
        return null;
    }


    /**
     * 未读数量
     *
     * @param receiverId
     */
    @Override
    public long countUnread(Long receiverId) {
        return 0;
    }


    /**
     * 将某条消息标记为已读
     *
     * @param receiverId
     * @param recipientId
     */
    @Override
    public void markAsRead(Long receiverId, Long recipientId) {

    }


    /**
     * 将当前用户全部未读消息标记为已读
     *
     * @param receiverId
     */
    @Override
    public void markAllAsRead(Long receiverId) {

    }


    /**
     * 删除某条消息（软删除）
     *
     * @param receiverId
     * @param recipientId
     */
    @Override
    public void deleteMessage(Long receiverId, Long recipientId) {

    }


    /**
     * 清空当前用户的所有消息（软删除）
     *
     * @param receiverId
     */
    @Override
    public void clearAll(Long receiverId) {

    }
}
