package hbnu.project.zhiyancommonseata.listener;

import io.seata.core.event.EventBus;
import io.seata.core.event.GlobalTransactionEvent;
import io.seata.core.model.GlobalStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Seata 事务监听器
 * 监听全局事务的状态变化，用于日志记录和监控
 *
 * @author ErgouTree
 */
@Slf4j
@Component
public class SeataTransactionListener {


}