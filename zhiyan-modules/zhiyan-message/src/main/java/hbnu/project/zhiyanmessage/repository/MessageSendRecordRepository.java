package hbnu.project.zhiyanmessage.repository;

import hbnu.project.zhiyanmessage.model.entity.MessageSendRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
