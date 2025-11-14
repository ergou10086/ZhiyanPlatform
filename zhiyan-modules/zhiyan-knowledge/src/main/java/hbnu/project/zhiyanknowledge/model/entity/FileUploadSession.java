package hbnu.project.zhiyanknowledge.model.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hbnu.project.zhiyancommonbasic.annotation.LongToString;
import hbnu.project.zhiyancommonbasic.utils.id.SnowflakeIdUtil;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件分片上传会话实体
 *
 * @author ErgouTree
 */
@Slf4j
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "achievement_file_upload_session")
public class FileUploadSession {

    /**
     * 雪花id
     */
    @Id
    @LongToString
    @Column(name = "id", nullable = false, columnDefinition = "BIGINT COMMENT '分片唯一标识（雪花ID）'")
    private Long id;

    /**
     * MinIO的uploadId
     */
    @Column(name = "upload_id", nullable = false, unique = true)
    private String uploadId;

    /**
     * 成果ID
     */
    @Column(name = "achievement_id", nullable = false)
    private Long achievementId;

    /**
     * 文件名
     */
    @Column(name = "file_name", nullable = false)
    private String fileName;

    /**
     * 文件总大小（字节）
     */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    /**
     * 分片大小(字节)
     */
    @Column(name = "chunk_size", nullable = false)
    private Integer chunkSize;

    /**
     * 总分片数
     */
    @Column(name = "total_chunks", nullable = false)
    private Integer totalChunks;

    /**
     * 已上传分片号列表(JSON格式)
     */
    @Column(name = "uploaded_chunks", columnDefinition = "TEXT")
    private String uploadedChunksJson;

    /**
     * MinIO对象键
     */
    @Column(name = "object_key", nullable = false, length = 500)
    private String objectKey;

    /**
     * 桶名称
     */
    @Column(name = "bucket_name", nullable = false)
    private String bucketName;

    /**
     * 上传用户ID
     */
    @Column(name = "upload_by", nullable = false)
    private Long uploadBy;

    /**
     * 状态
     */
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private UploadStatus status;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 过期时间
     */
    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    /**
     * 上传状态枚举
     */
    public enum UploadStatus {
        IN_PROGRESS,  // 上传中
        COMPLETED,    // 已完成
        FAILED,       // 失败
        CANCELLED     // 已取消
    }

    // 辅助方法：JSON转换
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取已上传分片列表
     */
    @Transient
    public List<Integer> getUploadedChunks() {
        if (uploadedChunksJson == null || uploadedChunksJson.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(uploadedChunksJson, new TypeReference<List<Integer>>() {});
        } catch (Exception e) {
            log.error("解析已上传分片列表失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 设置已上传分片列表
     */
    public void setUploadedChunks(List<Integer> chunks) {
        try {
            this.uploadedChunksJson = objectMapper.writeValueAsString(chunks);
        } catch (Exception e) {
            log.error("序列化已上传分片列表失败", e);
        }
    }

    /**
     * 添加已上传分片
     */
    public void addUploadedChunk(int chunkNumber) {
        List<Integer> chunks = getUploadedChunks();
        if (!chunks.contains(chunkNumber)) {
            chunks.add(chunkNumber);
            setUploadedChunks(chunks);
        }
    }

    /**
     * 计算上传进度百分比
     */
    @Transient
    public double getProgress() {
        if (totalChunks == 0) {
            return 0.0;
        }
        return (double) getUploadedChunks().size() / totalChunks * 100;
    }

    /**
     * 持久化前初始化方法：生成ID、设置时间
     */
    @PrePersist
    protected void onCreate() {
        // 生成雪花ID（如果未设置）
        if (this.id == null) {
            this.id = SnowflakeIdUtil.nextId();
        }
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        // 设置7天后过期
        this.expiredAt = now.plusDays(7);
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}