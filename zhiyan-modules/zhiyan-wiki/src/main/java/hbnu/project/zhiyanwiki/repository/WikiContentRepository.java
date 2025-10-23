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
     */
    @Query("{ 'projectId': ?0, '$text': { '$search': ?1 } }")
    List<WikiContent> searchByContent(Long projectId, String keyword);
}
