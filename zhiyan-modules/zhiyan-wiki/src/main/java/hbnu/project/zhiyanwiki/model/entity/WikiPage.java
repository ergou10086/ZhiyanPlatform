package hbnu.project.zhiyanwiki.model.entity;

import hbnu.project.zhiyancommonbasic.annotation.LongToString;
import hbnu.project.zhiyancommonbasic.domain.BaseAuditEntity;
import hbnu.project.zhiyancommonbasic.utils.id.SnowflakeIdUtil;
import hbnu.project.zhiyanwiki.model.enums.PageType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Wiki页面实体类（MySQL）
 * 仅存储元数据和关系，内容存储在MongoDB
 * 支持树状目录结构：可以是目录(DIRECTORY)或文档(DOCUMENT)
 *
 * @author ErgouTree
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "wiki_page", indexes = {
        @Index(name = "idx_project", columnList = "project_id"),
        @Index(name = "idx_parent", columnList = "parent_id"),
        @Index(name = "idx_project_parent", columnList = "project_id, parent_id"),
        @Index(name = "idx_project_type", columnList = "project_id, page_type"),
        @Index(name = "idx_mongo_content", columnList = "mongo_content_id"),
        @Index(name = "idx_path", columnList = "path(255)")
})
@DynamicInsert
@DynamicUpdate
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class WikiPage extends BaseAuditEntity {

    /**
     * Wiki页面唯一标识（雪花ID）
     */
    @Id
    @LongToString
    @Column(name = "id", nullable = false, columnDefinition = "BIGINT COMMENT 'Wiki页面唯一标识（雪花ID）'")
    private Long id;

    /**
     * 所属项目ID
     */
    @LongToString
    @Column(name = "project_id", nullable = false, columnDefinition = "BIGINT COMMENT '所属项目ID'")
    private Long projectId;

    /**
     * 页面标题
     */
    @Column(name = "title", nullable = false, length = 255, columnDefinition = "VARCHAR(255) COMMENT '页面标题'")
    private String title;

    /**
     * 页面类型（目录 DIRECTORY 或文档 DOCUMENT）
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "page_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'DOCUMENT' COMMENT '页面类型'")
    private PageType pageType = PageType.DOCUMENT;

    /**
     * MongoDB 内容文档ID（关联 wiki_contents 集合）
     * 注意：仅当 pageType = DOCUMENT 时才有值
     */
    @Column(name = "mongo_content_id", length = 24, columnDefinition = "VARCHAR(24) COMMENT 'MongoDB文档ID'")
    private String mongoContentId;

    /**
     * 父页面ID（用于构建树状结构，根页面为null）
     */
    @LongToString
    @Column(name = "parent_id", columnDefinition = "BIGINT COMMENT '父页面ID'")
    private Long parentId;

    /**
     * 页面路径（用于快速查找，如 "/root/parent/current"）
     */
    @Column(name = "path", length = 1000, columnDefinition = "VARCHAR(1000) COMMENT '页面路径'")
    private String path;

    /**
     * 排序序号（用于同级页面排序）
     */
    @Builder.Default
    @Column(name = "sort_order", columnDefinition = "INT DEFAULT 0 COMMENT '排序序号'")
    private Integer sortOrder = 0;

    /**
     * 是否公开（默认只对项目成员可见）
     */
    @Builder.Default
    @Column(name = "is_public", columnDefinition = "BOOLEAN DEFAULT FALSE COMMENT '是否公开'")
    private Boolean isPublic = false;

    /**
     * 创建者ID
     */
    @LongToString
    @Column(name = "creator_id", nullable = false, columnDefinition = "BIGINT COMMENT '创建者ID'")
    private Long creatorId;

    /**
     * 最后编辑者ID
     */
    @LongToString
    @Column(name = "last_editor_id", columnDefinition = "BIGINT COMMENT '最后编辑者ID'")
    private Long lastEditorId;

    /**
     * 内容大小（字符数，冗余字段用于快速统计）
     */
    @Column(name = "content_size", columnDefinition = "INT DEFAULT 0 COMMENT '内容大小'")
    private Integer contentSize;

    /**
     * 当前版本号（冗余字段，与MongoDB同步）
     */
    @Builder.Default
    @Column(name = "current_version", columnDefinition = "INT DEFAULT 1 COMMENT '当前版本号'")
    private Integer currentVersion = 1;

    /**
     * 内容摘要（前200字符，用于列表展示）
     */
    @Column(name = "content_summary", length = 200, columnDefinition = "VARCHAR(200) COMMENT '内容摘要'")
    private String contentSummary;

    // ==================== 协同编辑相关字段（预留） ====================
    // TODO: 协同编辑功能暂不实现，预留字段供未来扩展
    // TODO: 实现时需要引入WebSocket + OT算法（Operational Transformation）
    // TODO: 参考方案：使用ShareDB、Yjs或自研OT引擎

    /**
     * 是否被锁定（协同编辑时，锁定状态防止并发冲突）
     * TODO: 暂未使用，待协同编辑功能实现
     */
    @Builder.Default
    @Column(name = "is_locked", columnDefinition = "BOOLEAN DEFAULT FALSE COMMENT '是否被锁定'")
    private Boolean isLocked = false;

    /**
     * 锁定者用户ID（正在编辑的用户）
     * TODO: 暂未使用，待协同编辑功能实现
     */
    @LongToString
    @Column(name = "locked_by", columnDefinition = "BIGINT COMMENT '锁定者用户ID'")
    private Long lockedBy;

    /**
     * 锁定时间
     * TODO: 暂未使用，待协同编辑功能实现
     */
    @Column(name = "locked_at", columnDefinition = "DATETIME COMMENT '锁定时间'")
    private LocalDateTime lockedAt;

    /**
     * 是否启用协同编辑模式
     * TODO: 暂未使用，待协同编辑功能实现
     */
    @Builder.Default
    @Column(name = "collaborative_mode", columnDefinition = "BOOLEAN DEFAULT FALSE COMMENT '是否启用协同编辑'")
    private Boolean collaborativeMode = false;

    /**
     * 在持久化之前生成雪花ID
     */
    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = SnowflakeIdUtil.nextId();
        }
    }
}

