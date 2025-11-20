package hbnu.project.zhiyanmessgae.repository;

import hbnu.project.zhiyanmessgae.model.entity.MessageBody;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageBodyRepository extends JpaRepository<MessageBody, Long>{
}
