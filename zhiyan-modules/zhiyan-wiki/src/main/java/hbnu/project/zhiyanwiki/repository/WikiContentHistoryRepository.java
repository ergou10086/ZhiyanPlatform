package hbnu.project.zhiyanwiki.repository;

import hbnu.project.zhiyanwiki.model.entity.WikiContentHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Wiki内容历史记录数据访问层
 * 用于查询和管理归档的历史版本
 *
 * @author ErgouTree
 */
@Repository
public interface WikiContentHistoryRepository extends MongoRepository<WikiContentHistory, String> {

    /**
     * 根据Wiki页面ID查询所有历史版本（按版本号降序）
     *
     * @param wikiPageId Wiki页面ID
     * @return 历史版本列表
     */
    List<WikiContentHistory> findByWikiPageIdOrderByVersionDesc(Long wikiPageId);

    /**
     * 根据Wiki页面ID分页查询历史版本
     *
     * @param wikiPageId Wiki页面ID
     * @param pageable   分页参数
     * @return 历史版本分页列表
     */
    Page<WikiContentHistory> findByWikiPageIdOrderByVersionDesc(Long wikiPageId, Pageable pageable);

    /**
     * 根据Wiki页面ID和版本号查询特定历史版本
     *
     * @param wikiPageId Wiki页面ID
     * @param version    版本号
     * @return 历史版本
     */
    Optional<WikiContentHistory> findByWikiPageIdAndVersion(Long wikiPageId, Integer version);

    /**
     * 根据项目ID查询所有历史版本（按创建时间降序）
     *
     * @param projectId 项目ID
     * @param pageable  分页参数
     * @return 历史版本分页列表
     */
    Page<WikiContentHistory> findByProjectIdOrderByCreatedAtDesc(Long projectId, Pageable pageable);

    /**
     * 根据编辑者ID查询历史版本
     *
     * @param editorId 编辑者ID
     * @param pageable 分页参数
     * @return 历史版本分页列表
     */
    Page<WikiContentHistory> findByEditorIdOrderByCreatedAtDesc(Long editorId, Pageable pageable);

    /**
     * 统计Wiki页面的历史版本数量
     *
     * @param wikiPageId Wiki页面ID
     * @return 历史版本数量
     */
    long countByWikiPageId(Long wikiPageId);

    /**
     * 删除Wiki页面的所有历史版本
     *
     * @param wikiPageId Wiki页面ID
     */
    void deleteByWikiPageId(Long wikiPageId);

    /**
     * 删除项目下的所有历史版本
     *
     * @param projectId 项目ID
     */
    void deleteByProjectId(Long projectId);

    /**
     * 查询指定时间之前创建的历史版本（用于清理旧数据）
     *
     * @param createdAt 截止时间
     * @return 历史版本列表
     */
    List<WikiContentHistory> findByCreatedAtBefore(LocalDateTime createdAt);

    /**
     * 根据Wiki页面ID查询版本号范围内的历史版本
     *
     * @param wikiPageId   Wiki页面ID
     * @param minVersion   最小版本号
     * @param maxVersion   最大版本号
     * @return 历史版本列表（按版本号升序）
     */
    @Query("{ 'wikiPageId': ?0, 'version': { $gte: ?1, $lte: ?2 } }")
    List<WikiContentHistory> findByWikiPageIdAndVersionBetween(Long wikiPageId, Integer minVersion, Integer maxVersion);
}

