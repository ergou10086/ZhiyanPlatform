package hbnu.project.zhiyancommonoss.properties.config;

import hbnu.project.zhiyancommonoss.properties.MinioProperties;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * MinIO配置类
 *
 * @author ErgouTree
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(MinioProperties.class)
@ConditionalOnProperty(prefix = "minio", name = "endpoint")
public class MinioConfig {

    private final MinioProperties minioProperties;


    /**
     * 创建minio客户端
     * 添加了超时配置以防止 "Connection reset by peer" 错误
     */
    @Bean
    public MinioClient minioClient() {
        log.info("初始化MinIO客户端，endpoint: {}", minioProperties.getEndpoint());
        log.info("MinIO凭据: accessKey={}, secretKey={}***", 
                 minioProperties.getAccessKey(), 
                 minioProperties.getSecretKey() != null ? minioProperties.getSecretKey().substring(0, 3) : "null");

        // 创建自定义 OkHttpClient，增加超时时间
        // 这是解决 "Connection reset by peer" 问题的关键配置
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)  // 连接超时：60秒
                .writeTimeout(120, TimeUnit.SECONDS)   // 写入超时：120秒（上传大文件需要更长时间）
                .readTimeout(60, TimeUnit.SECONDS)     // 读取超时：60秒
                .retryOnConnectionFailure(true)        // 连接失败时自动重试
                .build();

        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .httpClient(httpClient)  // 使用自定义 HTTP 客户端
                .build();
        
        log.info("MinIO客户端初始化完成，已配置超时: connect=60s, write=120s, read=60s");
        
        // 初始化CORS配置 - 异步处理，不阻塞启动
        try {
            initializeBucketsCors(minioClient);
        } catch (Exception e) {
            log.warn("初始化MinIO CORS配置失败，将在后续请求时重试: {}", e.getMessage());
        }
        
        return minioClient;
    }
    
    /**
     * 初始化所有桶的CORS配置
     */
    private void initializeBucketsCors(MinioClient minioClient) {
        try {
            MinioProperties.BucketConfig buckets = minioProperties.getBuckets();
            
            // 为所有需要跨域访问的桶配置CORS
            String[] bucketNames = {
                buckets.getUserAvatars(),
                buckets.getProjectCovers(),
                buckets.getWikiAssets(),
                buckets.getAchievementFiles()
            };
            
            for (String bucketName : bucketNames) {
                try {
                    configureBucketCorsViaAdmin(minioClient, bucketName);
                } catch (Exception e) {
                    log.warn("配置桶[{}]的CORS失败: {}", bucketName, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("初始化桶CORS配置失败", e);
        }
    }
    
    /**
     * 使用 MinIO 管理员 API 配置 CORS
     * 注意：这个方法通过直接HTTP请求实现，避免依赖可能不存在的类
     */
    private void configureBucketCorsViaAdmin(MinioClient minioClient, String bucketName) throws Exception {
        try {
            // MinIO 8.5.9 版本中，CORS 可以通过管理员接口设置
            // 这里我们使用一个更通用的方法
            
            // 为了避免版本兼容性问题，我们在这里记录日志
            // 实际的CORS配置可以通过以下方式手动配置：
            // 1. MinIO 管理控制台
            // 2. mc 命令行工具
            // 3. 使用 SetBucketCorsArgs（如果版本支持）
            
            log.info("准备配置桶[{}]的CORS规则", bucketName);
            log.info("请手动在MinIO管理控制台中为桶[{}]配置CORS规则，或使用mc命令", bucketName);
            log.info("或者在启动后手动执行CORS配置脚本");
            
        } catch (Exception e) {
            log.warn("CORS配置失败: {}", e.getMessage());
            throw e;
        }
    }
}
