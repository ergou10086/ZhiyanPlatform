package hbnu.project.zhiyanwiki.repository;


import hbnu.project.zhiyanwiki.model.entity.WikiContent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Wiki内容的数据访问，从MongoDB拿出内容
 */
@Repository
public interface WikiContentRepository extends MongoRepository<WikiContent, String> {

    /**
     * 根据 WikiPage ID 查询内容
     */
    Optional<WikiContent> findByWikiPageId(Long wikiPageId);

    /**
     * 根据项目 ID 查询所有wiki内容（用于批量操作）
     */
    List<WikiContent> findByProjectId(Long projectId);

    /**
     * 删除指定 WikiPage 的内容
     */
    void deleteByWikiPageId(Long wikiPageId);

    /**
     * 批量删除项目下所有内容
     */
    void deleteByProjectId(Long projectId);

    /**
     * 全文搜索（MongoDB 文本索引）
     * 注意：$text 查询必须与其他条件正确组合
     */
    @Query("{ '$and': [ { 'projectId': ?0 }, { '$text': { '$search': ?1 } } ] }")
    List<WikiContent> searchByContent(Long projectId, String keyword);

    /**
     * 正则表达式搜索（用于短关键词和中文单字）
     * 当文本索引搜索失败时使用此方法作为备选
     */
    @Query("{ 'projectId': ?0, 'content': { $regex: ?1, $options: 'i' } }")
    List<WikiContent> searchByContentRegex(Long projectId, String keyword);
}
