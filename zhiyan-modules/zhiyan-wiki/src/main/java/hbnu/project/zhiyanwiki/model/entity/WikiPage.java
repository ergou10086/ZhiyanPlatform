package hbnu.project.zhiyanwiki.model.entity;

import hbnu.project.zhiyancommonbasic.annotation.LongToString;
import hbnu.project.zhiyancommonbasic.domain.BaseAuditEntity;
import hbnu.project.zhiyancommonbasic.utils.id.SnowflakeIdUtil;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.List;

/**
 * Wiki页面实体类
 * 支持树状结构组织和版本控制
 *
 * @author ErgouTree
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "wiki_page", indexes = {
        @Index(name = "idx_project", columnList = "project_id"),
        @Index(name = "idx_parent", columnList = "parent_id"),
        @Index(name = "idx_project_parent", columnList = "project_id, parent_id")
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
     * Markdown内容
     */
    @Lob
    @Column(name = "content", columnDefinition = "LONGTEXT COMMENT 'Markdown内容'")
    private String content;

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
     * 子页面列表（一对多，用于构建树）
     */
    @OneToMany(mappedBy = "parentId", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<WikiPage> children;

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

