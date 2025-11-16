package hbnu.project.zhiyanknowledge.scheduled;

import hbnu.project.zhiyanknowledge.repository.FileUploadSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 上传会话清理定时任务
 * 每天凌晨2点清理过期的上传会话
 *
 * @author ErgouTree
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UploadSessionCleanupTask {

    @Autowired
    private final FileUploadSessionRepository sessionRepository;

    /**
     * 清理过期的上传会话
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredSessions() {
        log.info("开始清理过期的上传会话");

        try {
            int deletedCount = sessionRepository.deleteExpiredSessions(LocalDateTime.now());
            log.info("清理过期上传会话完成: 清理数量={}", deletedCount);
        } catch (Exception e) {
            log.error("清理过期上传会话失败", e);
        }
    }
}
