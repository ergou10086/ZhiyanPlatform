package hbnu.project.zhiyanproject.config;

import hbnu.project.zhiyancommonoss.enums.BucketType;
import hbnu.project.zhiyancommonoss.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO桶初始化配置
 * 应用启动时自动创建所需的桶
 *
 * @author ErgouTree
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MinioInitConfig implements CommandLineRunner {

    private final MinioService minioService;

    @Override
    public void run(String... args) {
        log.info("开始初始化MinIO桶...");

        try {
            // 创建任务提交文件桶
            String bucketName = BucketType.TASK_SUBMISSION.getBucketName();
            
            if (!minioService.bucketExists(bucketName)) {
                minioService.createBucket(bucketName);
                log.info("✅ MinIO桶创建成功: {}", bucketName);
            } else {
                log.info("✅ MinIO桶已存在: {}", bucketName);
            }

            log.info("MinIO桶初始化完成");

        } catch (Exception e) {
            log.error("❌ MinIO桶初始化失败", e);
            // 不抛出异常，避免影响应用启动
        }
    }
}

