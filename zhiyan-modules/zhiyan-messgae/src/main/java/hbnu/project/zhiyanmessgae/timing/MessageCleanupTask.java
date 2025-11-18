package hbnu.project.zhiyanmessgae.timing;

import hbnu.project.zhiyanmessgae.repository.MessageRecipientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 消息清理定时任务
 * 清理180天前的消息（软删除）
 *
 * @author ErgouTree
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "message.cleanup.enabled", havingValue = "true", matchIfMissing = true)
public class MessageCleanupTask {

    private final MessageRecipientRepository messageRecipientRepository;

    /**
     * 每天凌晨2点执行清理任务
     * 清理180天前的消息
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldMessages() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(180);

        // 查询所有用户180天前的消息
        // 注意：这里需要查询所有用户，实际实现可能需要分批处理
        // 简化实现：假设通过业务逻辑调用清理

        log.info("开始清理180天前的消息，截止时间: {}", cutoffTime);

        // 实际清理逻辑应该在Service层实现，这里仅作示例
        // 可以通过Repository查询并批量软删除
    }
}
