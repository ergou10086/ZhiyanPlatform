package hbnu.project.zhiyanmessage.service;


import hbnu.project.zhiyanmessage.model.entity.Message;
import hbnu.project.zhiyanmessage.model.entity.MessageReception;
import hbnu.project.zhiyanmessage.model.enums.MessageScene;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;

/**
 * 站内消息服务
 * <p>
 * 说明：
 * - 各业务模块只负责在合适的业务事件中调用本服务；
 * - 消息模块负责消息体、收件人记录、未读数、已读状态等通用逻辑。
 *
 * @author ErgouTree
 */
public interface InboxMessageService {

    /**
     * 发送一条“个人消息”（向单个的收件人发送）
     *
     * @param scene       消息场景
     * @param senderId    发送人ID（系统消息可为 null）
     * @param receiverId  收件人ID
     * @param title       标题
     * @param content     正文
     * @param businessId  业务ID（任务ID、项目ID等，可为空）
     * @param businessType 业务类型（"TASK" / "PROJECT" / "ACHIEVEMENT" 等，可为空）
     * @param extendDataJson 扩展字段 JSON（跳转链接、额外参数等）
     * @return MessageBody（便于调用方根据需要做调试或链路跟踪）
     */
    Message sendPersonalMessage(MessageScene scene,
                                Long senderId,
                                Long receiverId,
                                String title,
                                String content,
                                Long businessId,
                                String businessType,
                                String extendDataJson);

    /**
     * 发送多收件人消息（多收件人，一条消息体，多条收件人记录）
     */
    Message sendBatchPersonalMessage(MessageScene scene,
                                         Long senderId,
                                         Collection<Long> receiverIds,
                                         String title,
                                         String content,
                                         Long businessId,
                                         String businessType,
                                         String extendDataJson);

    /**
     * 向全体用户发送消息
     * 向全体用户发送消息只能是管理员发送
     * 这种消息类型通常只有系统消息，不用管理业务模块
     */
    Message sendAllPersonalMessage(MessageScene scene,
                                       Long senderId,
                                       String title,
                                       String content,
                                       String extendDataJson);

    /**
     * 用户收件箱分页查询（全部）
     */
    Page<MessageReception> pageInbox(Long receiverId, Pageable pageable);

    /**
     * 用户未读消息分页查询
     */
    Page<MessageReception> pageUnread(Long receiverId, Pageable pageable);

    /**
     * 未读数量
     */
    long countUnread(Long receiverId);

    /**
     * 将某条消息标记为已读
     */
    void markAsRead(Long receiverId, Long recipientId);

    /**
     * 将当前用户全部未读消息标记为已读
     */
    void markAllAsRead(Long receiverId);

    /**
     * 删除某条消息（软删除）
     */
    void deleteMessage(Long receiverId, Long recipientId);

    /**
     * 清空当前用户的所有消息（软删除）
     */
    void clearAll(Long receiverId);
}
