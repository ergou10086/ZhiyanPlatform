package hbnu.project.zhiyancommonseata.compensation;

import hbnu.project.zhiyancommonoss.enums.BucketType;
import hbnu.project.zhiyancommonoss.service.MinioService;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * MinIO 分布式事务补偿服务
 * 使用 TCC 模式处理 MinIO 文件操作的事务一致性
 *
 * @author ErgouTree
 */
@Slf4j
@Component
@LocalTCC
@RequiredArgsConstructor
public class MinioCompensationService {

    private final MinioService minioService;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String MINIO_TX_KEY_PREFIX = "seata:minio:tx:";
    private static final int TX_EXPIRE_SECONDS = 300; // 5分钟过期


    /**
     * Try 阶段：上传文件到临时区域
     * 文件先上传到 temp 目录，等待事务确认
     */
    @TwoPhaseBusinessAction(
            name = "minioUpload",
            commitMethod = "commitUpload",
            rollbackMethod = "rollbackUpload"
    )
    public boolean prepareUpload(
            @BusinessActionContextParameter(paramName = "bucketType") BucketType bucketType,
            @BusinessActionContextParameter(paramName = "tempObjectKey") String tempObjectKey,
            @BusinessActionContextParameter(paramName = "finalObjectKey") String finalObjectKey,
            @BusinessActionContextParameter(paramName = "txId") String txId) {

        try {
            // 记录事务信息到 Redis（用于后续的 confirm/cancel 操作）
            String txKey = MINIO_TX_KEY_PREFIX + txId;
            String txInfo = String.format("%s|%s|%s",
                    bucketType.name(), tempObjectKey, finalObjectKey);
            redisTemplate.opsForValue().set(txKey, txInfo, TX_EXPIRE_SECONDS, TimeUnit.SECONDS);

            log.info("MinIO Try 阶段成功: tempKey={}, finalKey={}, txId={}",
                    tempObjectKey, finalObjectKey, txId);
            return true;
        } catch (Exception e) {
            log.error("MinIO Try 阶段失败", e);
            return false;
        }
    }


    /**
     * Confirm 阶段：将临时文件移动到最终位置
     */
    public boolean commitUpload(BusinessActionContext context) {
        String txId = (String) context.getActionContext("txId");
        String txKey = MINIO_TX_KEY_PREFIX + txId;

        try {
            String txInfo = redisTemplate.opsForValue().get(txKey);
            if (txInfo == null) {
                log.warn("MinIO Confirm: 事务信息不存在，可能已过期: txId={}", txId);
                return true; // 幂等处理
            }

            String[] parts = txInfo.split("\\|");
            BucketType bucketType = BucketType.valueOf(parts[0]);
            String tempObjectKey = parts[1];
            String finalObjectKey = parts[2];

            // 检查临时文件是否存在
            if (!minioService.fileExists(bucketType, tempObjectKey)) {
                log.warn("MinIO Confirm: 临时文件不存在，可能已处理: tempKey={}, txId={}", tempObjectKey, txId);
                redisTemplate.delete(txKey);
                return true; // 幂等处理
            }

            // 将临时文件复制到最终位置（使用 copyFile 方法）
            minioService.copyFile(bucketType, tempObjectKey, bucketType, finalObjectKey);

            // 删除临时文件（使用 deleteFile 方法）
            minioService.deleteFile(bucketType, tempObjectKey);

            // 清理 Redis 事务记录
            redisTemplate.delete(txKey);

            log.info("MinIO Confirm 阶段成功: finalKey={}, txId={}", finalObjectKey, txId);
            return true;
        } catch (Exception e) {
            log.error("MinIO Confirm 阶段失败: txId={}", txId, e);
            return false;
        }
    }



    /**
     * Cancel 阶段：删除临时文件
     */
    public boolean rollbackUpload(BusinessActionContext context) {
        String txId = (String) context.getActionContext("txId");
        String txKey = MINIO_TX_KEY_PREFIX + txId;

        try {
            String txInfo = redisTemplate.opsForValue().get(txKey);
            if (txInfo == null) {
                log.warn("MinIO Cancel: 事务信息不存在: txId={}", txId);
                return true; // 幂等处理
            }

            String[] parts = txInfo.split("\\|");
            BucketType bucketType = BucketType.valueOf(parts[0]);
            String tempObjectKey = parts[1];

            // 检查临时文件是否存在
            if (minioService.fileExists(bucketType, tempObjectKey)) {
                // 删除临时文件（使用 deleteFile 方法）
                minioService.deleteFile(bucketType, tempObjectKey);
                log.info("MinIO Cancel: 临时文件已删除: tempKey={}, txId={}", tempObjectKey, txId);
            } else {
                log.info("MinIO Cancel: 临时文件不存在，跳过删除: tempKey={}, txId={}", tempObjectKey, txId);
            }

            // 清理 Redis 事务记录
            redisTemplate.delete(txKey);

            log.info("MinIO Cancel 阶段成功: tempKey={}, txId={}", tempObjectKey, txId);
            return true;
        } catch (Exception e) {
            log.error("MinIO Cancel 阶段失败: txId={}", txId, e);
            return false;
        }
    }
}