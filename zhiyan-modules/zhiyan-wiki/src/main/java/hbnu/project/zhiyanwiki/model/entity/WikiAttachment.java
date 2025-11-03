package hbnu.project.zhiyanwiki.model.entity;

import hbnu.project.zhiyancommonbasic.annotation.LongToString;
import hbnu.project.zhiyancommonbasic.utils.id.SnowflakeIdUtil;
import hbnu.project.zhiyanwiki.model.enums.AttachmentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

/**
 * Wiki附件实体类（MySQL）
 * 存储Wiki页面的附件元数据，包括图片和普通文件
 *
 * @author Tokito
 */
@Data
@Entity
@Table(name = "wiki_attachment", indexes = {
        @Index(name = "idx_wiki_page", columnList = "wiki_page_id"),
        @Index(name = "idx_project", columnList = "project_id"),
        @Index(name = "idx_type", columnList = "attachment_type"),
        @Index(name = "idx_upload_by", columnList = "upload_by"),
        @Index(name = "idx_upload_at", columnList = "upload_at")
})
@DynamicInsert
@DynamicUpdate
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class WikiAttachment {

    /**
     * 附件唯一标识（雪花ID）
     */
    @Id
    @LongToString
    @Column(name = "id", nullable = false, columnDefinition = "BIGINT COMMENT '附件唯一标识（雪花ID）'")
    private Long id;

    /**
     * 所属Wiki页面ID
     */
    @LongToString
    @Column(name = "wiki_page_id", nullable = false, columnDefinition = "BIGINT COMMENT '所属Wiki页面ID'")
    private Long wikiPageId;

    /**
     * 所属项目ID（冗余字段，便于查询）
     */
    @LongToString
    @Column(name = "project_id", nullable = false, columnDefinition = "BIGINT COMMENT '所属项目ID'")
    private Long projectId;

    /**
     * 附件类型（IMAGE=图片, FILE=普通文件）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "attachment_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20) COMMENT '附件类型'")
    private AttachmentType attachmentType;

    /**
     * 原始文件名
     */
    @Column(name = "file_name", nullable = false, length = 255, columnDefinition = "VARCHAR(255) COMMENT '原始文件名'")
    private String fileName;

    /**
     * 文件大小（字节）
     */
    @Column(name = "file_size", nullable = false, columnDefinition = "BIGINT COMMENT '文件大小（字节）'")
    private Long fileSize;

    /**
     * 文件类型/扩展名（jpg/png/pdf/zip等）
     */
    @Column(name = "file_type", length = 50, columnDefinition = "VARCHAR(50) COMMENT '文件类型'")
    private String fileType;

    /**
     * MinIO桶名
     */
    @Column(name = "bucket_name", nullable = false, length = 100, columnDefinition = "VARCHAR(100) COMMENT 'MinIO桶名'")
    private String bucketName;

    /**
     * MinIO对象键（存储路径）
     */
    @Column(name = "object_key", nullable = false, length = 500, columnDefinition = "VARCHAR(500) COMMENT 'MinIO对象键'")
    private String objectKey;

    /**
     * 完整访问URL
     */
    @Column(name = "file_url", nullable = false, length = 1000, columnDefinition = "VARCHAR(1000) COMMENT '完整访问URL'")
    private String fileUrl;

    /**
     * 文件描述/备注
     */
    @Column(name = "description", length = 500, columnDefinition = "VARCHAR(500) COMMENT '文件描述'")
    private String description;

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
     * 是否已删除（软删除标记）
     */
    @Column(name = "is_deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE COMMENT '是否已删除'")
    private Boolean isDeleted = false;

    /**
     * 删除时间
     */
    @Column(name = "deleted_at", columnDefinition = "TIMESTAMP COMMENT '删除时间'")
    private LocalDateTime deletedAt;

    /**
     * 在持久化之前生成雪花ID和上传时间
     */
    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = SnowflakeIdUtil.nextId();
        }
        if (this.uploadAt == null) {
            this.uploadAt = LocalDateTime.now();
        }
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
    }
}


