package hbnu.project.zhiyanmessgae.repository;

import hbnu.project.zhiyanmessgae.model.entity.MessageBody;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 站内消息repository层
 *
 * @author ErgouTree
 */
public interface InboxMessageRepository extends JpaRepository<MessageBody, Long>, JpaSpecificationExecutor<MessageBody> {

}
