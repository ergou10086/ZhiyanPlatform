package hbnu.project.zhiyanmessage.repository;

import hbnu.project.zhiyanmessage.model.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long>{
}
