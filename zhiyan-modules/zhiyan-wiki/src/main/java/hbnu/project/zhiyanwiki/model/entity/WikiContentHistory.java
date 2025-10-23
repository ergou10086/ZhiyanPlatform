package hbnu.project.zhiyanwiki.model.entity;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Wiki内容历史记录实体类（MongoDB）
 * 当wiki_contents集合中的recentVersions超过10个时，
 * 旧的版本会被归档到这个集合中
 *
 * @author ErgouTree
 */
@Document(collection = "wiki_content_history")
@CompoundIndexes({
        @CompoundIndex(name = "idx_wiki_version", def = "{'wikiPageId': 1, 'version': -1}"),
        @CompoundIndex(name = "idx_project_created", def = "{'projectId': 1, 'createdAt': -1}")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WikiContentHistory {

    /**
     * MongoDB ObjectId
     */
    @Id
    private String id;

    /**
     * 关联 MySQL wiki_page 表的 ID
     */
    @Indexed
    private Long wikiPageId;

    /**
     * 冗余字段，方便查询和清理
     */
    @Indexed
    private Long projectId;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 相对于前一版本的差异补丁（Unified Diff 格式）
     */
    private String contentDiff;

    /**
     * 变更描述
     */
    private String changeDescription;

    /**
     * 编辑者用户ID
     */
    private Long editorId;

    /**
     * 版本创建时间
     */
    @Indexed
    private LocalDateTime createdAt;

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
     * 内容哈希值
     */
    private String contentHash;

    /**
     * 归档时间（当该版本从recent移入history时的时间）
     */
    private LocalDateTime archivedAt;
}

