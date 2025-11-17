package hbnu.project.zhiyanmessgae.repository;

import hbnu.project.zhiyanmessgae.model.entity.MessageRecipient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 消息收件人repository
 *
 * @author ErgouTree
 */
@Repository
public interface MessageRecipientRepository extends JpaRepository<MessageRecipient, Long> {

    /**
     * 用户收件箱分页查询（排除已删除）
     */
    Page<MessageRecipient> findByReceiverIdAndDeletedFalseOrderByTriggerTimeDesc(Long receiverId, Pageable pageable);

    /**
     * 用户未读消息分页查询
     */
    Page<MessageRecipient> findByReceiverIdAndReadFlagFalseAndDeletedFalseOrderByTriggerTimeDesc(Long receiverId, Pageable pageable);

    /**
     * 未读数量统计
     */
    long countByReceiverIdAndReadFlagFalseAndDeletedFalse(Long receiverId);

    /**
     * 根据 ID + 用户校验一条消息（防止越权）
     */
    Optional<MessageRecipient> findByIdAndReceiverIdAndDeletedFalse(Long id, Long receiverId);

    /**
     * 查询用户全部未读，用于全部已读
     */
    List<MessageRecipient> findByReceiverIdAndReadFlagFalseAndDeletedFalse(Long receiverId);

    /**
     * 查询用户全部未删除，用于“清空消息”
     */
    List<MessageRecipient> findByReceiverIdAndDeletedFalse(Long receiverId);

    /**
     * 按时间范围查询,用于多久清理任务
     */
    List<MessageRecipient> findByReceiverIdAndDeletedFalseAndTriggerTimeBefore(Long receiverId, LocalDateTime time);
}
