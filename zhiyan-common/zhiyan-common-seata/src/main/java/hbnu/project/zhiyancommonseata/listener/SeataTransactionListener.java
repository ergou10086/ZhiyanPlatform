package hbnu.project.zhiyancommonseata.listener;

import io.seata.core.event.EventBus;
import io.seata.core.event.GlobalTransactionEvent;
import io.seata.core.event.GuavaEventBus;
import io.seata.core.model.GlobalStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;


/**
 * Seata 事务监听器
 * 监听全局事务的状态变化，用于日志记录和监控
 *
 * @author ErgouTree
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "seata", name = "enabled", havingValue = "true", matchIfMissing = false)
public class SeataTransactionListener {

    private final EventBus eventBus;

    public SeataTransactionListener() {
        this.eventBus = new GuavaEventBus("seata-transaction-listener");
    }

    @PostConstruct
    public void init() {
        // 订阅全局事务事件
        eventBus.register(new Object() {
            @com.google.common.eventbus.Subscribe
            public void onGlobalTransactionEvent(io.seata.core.event.GlobalTransactionEvent event) {
                handleTransactionEvent(event);
            }
        });
        log.info("Seata 事务监听器已初始化");
    }

    @PreDestroy
    public void destroy() {
        log.info("Seata 事务监听器已销毁");
    }

    /**
     * 事务事件处理
     */
    private void handleTransactionEvent(io.seata.core.event.GlobalTransactionEvent event) {
        String xid = String.valueOf(event.getId());
        GlobalStatus status = GlobalStatus.valueOf(event.getStatus());

        log.info("分布式事务状态变更: XID={}, Status={}, Role={}",
                xid, status, event.getRole());

        // 根据事务状态进行不同处理
        switch (status) {
            case Committed:
                log.info("分布式事务提交成功: XID={}", xid);
                // TODO: 记录成功指标
                break;
            case Rollbacked:
                log.warn("分布式事务回滚: XID={}", xid);
                // TODO: 发送告警通知、记录失败指标
                break;
            case TimeoutRollbacked:
                log.error("分布式事务超时回滚: XID={}", xid);
                // TODO: 发送紧急告警
                break;
            case CommitFailed:
                log.error("分布式事务提交失败: XID={}", xid);
                // TODO: 发送紧急告警
                break;
            case RollbackFailed:
                log.error("分布式事务回滚失败: XID={}", xid);
                // TODO: 发送紧急告警
                break;
            default:
                log.debug("分布式事务状态: XID={}, Status={}", xid, status);
        }
    }
}