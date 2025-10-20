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
 * Wiki页面历史版本实体类
 * 记录Wiki页面的每次修改历史，支持版本回溯和差异对比
 *
 * @author ErgouTree
 */
@Data
@Entity
@Table(name = "wiki_page_history", indexes = {
        @Index(name = "idx_wiki_page", columnList = "wiki_page_id"),
        @Index(name = "idx_version", columnList = "wiki_page_id, version")
})
@DynamicInsert
@DynamicUpdate
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class WikiPageHistory {

    /**
     * 历史记录唯一标识
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @LongToString
    @Column(name = "id", nullable = false, columnDefinition = "BIGINT COMMENT '历史记录唯一标识'")
    private Long id;

    /**
     * 关联的Wiki页面ID
     */
    @LongToString
    @Column(name = "wiki_page_id", nullable = false, columnDefinition = "BIGINT COMMENT '关联的Wiki页面ID'")
    private Long wikiPageId;

    /**
     * 版本号（从1开始递增）
     */
    @Column(name = "version", nullable = false, columnDefinition = "INT COMMENT '版本号'")
    private Integer version;

    /**
     * 页面标题（历史版本）
     */
    @Column(name = "title", nullable = false, length = 255, columnDefinition = "VARCHAR(255) COMMENT '页面标题'")
    private String title;

    /**
     * Markdown内容（历史版本）
     */
    @Lob
    @Column(name = "content", columnDefinition = "LONGTEXT COMMENT 'Markdown内容'")
    private String content;

    /**
     * 修改说明/备注
     */
    @Column(name = "change_description", length = 500, columnDefinition = "VARCHAR(500) COMMENT '修改说明'")
    private String changeDescription;

    /**
     * 编辑者ID
     */
    @LongToString
    @Column(name = "editor_id", nullable = false, columnDefinition = "BIGINT COMMENT '编辑者ID'")
    private Long editorId;

    /**
     * 创建时间（版本创建时间）
     */
    @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'")
    private LocalDateTime createdAt;

    /**
     * 内容大小（字节数）
     */
    @Column(name = "content_size", columnDefinition = "INT COMMENT '内容大小'")
    private Integer contentSize;

    /**
     * 关联的Wiki页面实体
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wiki_page_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "wiki_page_history_ibfk_1"))
    private WikiPage wikiPage;

    /**
     * 在持久化之前设置创建时间和内容大小
     */
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.content != null) {
            this.contentSize = this.content.length();
        }
    }
}

