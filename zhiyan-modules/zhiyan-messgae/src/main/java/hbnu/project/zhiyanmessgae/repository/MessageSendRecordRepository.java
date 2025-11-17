package hbnu.project.zhiyanmessgae.repository;

import hbnu.project.zhiyanmessgae.model.entity.MessageSendRecord;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 发送记录的Repository
 */
@Repository
public interface MessageSendRecordRepository extends JpaRepository<MessageSendRecord, Long> {

    /**
     * 根据id查询
     * @param messageBodyId 消息体id
     * @return 消息发送记录列表
     */
    List<MessageSendRecord> findByMessageBodyId(Long messageBodyId);
}
