package hbnu.project.zhiyanwiki.model.entity;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Wiki内容实体类（MongoDB）
 * 存储Markdown内容、版本历史和差异补丁
 * 支持最近10个版本的快速访问
 *
 * @author ErgouTree
 */
@Document(collection = "wiki_contents")
@CompoundIndexes({
        @CompoundIndex(name = "idx_project_updated", def = "{'projectId': 1, 'updatedAt': -1}"),
        @CompoundIndex(name = "idx_wiki_version", def = "{'wikiPageId': 1, 'currentVersion': -1}")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WikiContent {

    /**
     * MongoDB ObjectId
     */
    @Id
    private String id;

    /**
     * 关联 MySQL wiki_page 表的 ID
     */
    @Indexed(unique = true)
    private Long wikiPageId;

    /**
     * 冗余字段，方便查询和分片
     */
    @Indexed
    private Long projectId;

    /**
     * Markdown 内容（当前最新版本）
     */
    @TextIndexed
    private String content;

    /**
     * 内容哈希值（用于检测内容是否真正变化）
     */
    private String contentHash;

    /**
     * 当前版本号
     */
    private Integer currentVersion;

    /**
     * 最近版本历史（保留最近 10 个版本的差异补丁）
     */
    private List<RecentVersion> recentVersions;

    // ==================== 元数据 ====================

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Indexed
    private LocalDateTime updatedAt;

    /**
     * 最后编辑者ID
     */
    private Long lastEditorId;

    // ==================== 协同编辑预留字段 ====================

    /**
     * 当前在线编辑者列表（用户ID集合）
     */
    private Set<Long> activeEditors;

    /**
     * 操作序列号（用于协同编辑的操作变换 OT）
     */
    private Long operationSequence;

    /**
     * 最后一次协同编辑同步时间
     */
    private LocalDateTime lastSyncAt;

    /**
     * 最近版本记录（嵌套文档）
     * 保存差异补丁而非完整内容，节省存储空间
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentVersion {
        /**
         * 版本号
         */
        private Integer version;

        /**
         * 相对于前一版本的差异补丁（Unified Diff 格式）
         */
        private String contentDiff;

        /**
         * 变更描述（用户提交的说明）
         */
        private String changeDescription;

        /**
         * 编辑者用户ID
         */
        private Long editorId;

        /**
         * 版本创建时间
         */
        private LocalDateTime createdAt;

        // ==================== 统计信息（来自 DiffService） ====================

        /**
         * 新增行数
         */
        private Integer addedLines;

        /**
         * 删除行数
         */
        private Integer deletedLines;

        /**
         * 变更字符数
         */
        private Integer changedChars;

        /**
         * 内容哈希值（该版本的完整内容哈希）
         */
        private String contentHash;
    }
}
