package hbnu.project.zhiyanknowledge.model.entity;

import hbnu.project.zhiyancommonbasic.annotation.LongToString;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;


/**
 * 文件管理表实体类
 *
 * @author ErgouTree
 */
@Data
@Entity
@Table(name = "achievement_file", indexes = {
        @Index(name = "idx_achievement", columnList = "achievement_id"),
        @Index(name = "idx_latest", columnList = "achievement_id, is_latest")
})
@DynamicInsert
@DynamicUpdate
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementFile{

    /**
     * 文件唯一标识
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @LongToString
    @Column(name = "id", nullable = false, columnDefinition = "BIGINT COMMENT '文件唯一标识'")
    private Long id;


    /**
     * 所属成果ID
     */
    @LongToString
    @Column(name = "achievement_id", nullable = false, columnDefinition = "BIGINT COMMENT '所属成果ID'")
    private Long achievementId;


    /**
     * 原始文件名
     */
    @Column(name = "file_name", nullable = false, length = 255, columnDefinition = "VARCHAR(255) COMMENT '原始文件名'")
    private String fileName;


    /**
     * 文件大小（字节）
     */
    @Column(name = "file_size", columnDefinition = "BIGINT COMMENT '文件大小（字节）'")
    private Long fileSize;


    /**
     * 文件类型（pdf/zip/csv等）
     * 通过读取文件后缀名自动添加
     */
    @Column(name = "file_type", length = 50, columnDefinition = "VARCHAR(50) COMMENT '文件类型（pdf/zip/csv等）'")
    private String fileType;


    /**
     * MinIO桶名
     */
    @Column(name = "bucket_name", nullable = false, length = 100, columnDefinition = "VARCHAR(100) COMMENT 'MinIO桶名'")
    private String bucketName;


    /**
     * MinIO对象键
     */
    @Column(name = "object_key", nullable = false, length = 500, columnDefinition = "VARCHAR(500) COMMENT 'MinIO对象键'")
    private String objectKey;


    /**
     * 完整访问URL
     */
    @Column(name = "minio_url", nullable = false, length = 1000, columnDefinition = "VARCHAR(1000) COMMENT '完整访问URL'")
    private String minioUrl;


    /**
     * 文件版本号
     */
    @Column(name = "version", columnDefinition = "INT DEFAULT 1 COMMENT '文件版本号'")
    private Integer version = 1;


    /**
     * 是否最新版本
     */
    @Column(name = "is_latest", columnDefinition = "BOOLEAN DEFAULT TRUE COMMENT '是否最新版本'")
    private Boolean isLatest = true;


    /**
     * 上传者ID
     */
    @LongToString
    @Column(name = "upload_by", nullable = false, columnDefinition = "BIGINT COMMENT '上传者ID'")
    private Long uploadBy;


    /**
     * 上传时间
     */
    @Column(name = "upload_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间'")
    private LocalDateTime uploadAt;


    /**
     * 关联的成果实体（外键关联）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "achievement_file_ibfk_1"))
    private Achievement achievement;


    /**
     * 在持久化之前设置上传时间
     */
    @PrePersist
    public void setUploadAt() {
        if (this.uploadAt == null) {
            this.uploadAt = LocalDateTime.now();
        }
    }
}
