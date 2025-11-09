package hbnu.project.zhiyanproject.config;

import hbnu.project.zhiyancommonoss.enums.BucketType;
import hbnu.project.zhiyancommonoss.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * MinIO桶初始化配置
 * 应用启动时自动创建所需的桶
 *
 * @author ErgouTree
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@Order(1) // 确保优先执行
public class MinioInitConfig implements CommandLineRunner {

    private final MinioService minioService;

    @Override
    public void run(String... args) {
        log.info("=".repeat(80));
        log.info("========== 开始初始化MinIO桶 ==========");
        log.info("=".repeat(80));
        
        // 检查依赖注入
        log.info("MinioService 注入状态: {}", minioService != null ? "✅ 成功" : "❌ 失败");
        if (minioService == null) {
            log.error("❌ MinioService 未注入，无法创建桶");
            return;
        }

        try {
            // 创建任务提交文件桶
            String bucketName = BucketType.TASK_SUBMISSION.getBucketName();
            log.info("准备创建的桶名: [{}]", bucketName);
            
            // 检查桶是否存在
            log.info("正在检查桶是否已存在...");
            boolean exists = minioService.bucketExists(bucketName);
            log.info("桶 [{}] 存在状态: {}", bucketName, exists ? "✅ 已存在" : "❌ 不存在");
            
            if (!exists) {
                log.info("开始创建桶: [{}]", bucketName);
                minioService.createBucket(bucketName);
                log.info("✅ MinIO桶创建成功: [{}]", bucketName);
                
                // 再次验证
                boolean created = minioService.bucketExists(bucketName);
                log.info("创建后验证: {}", created ? "✅ 桶创建成功并已验证" : "⚠️ 桶可能创建失败");
            } else {
                log.info("✅ MinIO桶已存在，无需创建: [{}]", bucketName);
            }

            log.info("=".repeat(80));
            log.info("========== MinIO桶初始化完成 ==========");
            log.info("=".repeat(80));

        } catch (Exception e) {
            log.error("=".repeat(80));
            log.error("❌❌❌ MinIO桶初始化失败 ❌❌❌");
            log.error("=".repeat(80));
            log.error("异常类型: {}", e.getClass().getName());
            log.error("异常消息: {}", e.getMessage());
            log.error("详细堆栈:", e);
            
            if (e.getCause() != null) {
                log.error("根本原因: {}", e.getCause().getMessage());
                log.error("根本原因堆栈:", e.getCause());
            }
            
            log.error("=".repeat(80));
            log.error("请检查:");
            log.error("1. MinIO 服务是否正在运行？");
            log.error("2. MinIO 配置是否正确？(endpoint, accessKey, secretKey)");
            log.error("3. 网络连接是否正常？");
            log.error("=".repeat(80));
            
            // 不抛出异常，避免影响应用启动
        }
    }
}

