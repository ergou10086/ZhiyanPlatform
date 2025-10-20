package hbnu.project.zhiyanknowledge.repository;

import hbnu.project.zhiyanknowledge.model.entity.WikiPageHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Wiki页面历史数据访问层
 * 管理Wiki页面的版本历史
 *
 * @author ErgouTree
 */
@Repository
public interface WikiPageHistoryRepository extends JpaRepository<WikiPageHistory, Long> {

    /**
     * 根据Wiki页面ID查询所有历史版本（按版本号降序）
     *
     * @param wikiPageId Wiki页面ID
     * @param pageable   分页参数
     * @return 历史版本分页列表
     */
    @Query("SELECT h FROM WikiPageHistory h WHERE h.wikiPageId = :wikiPageId ORDER BY h.version DESC")
    Page<WikiPageHistory> findByWikiPageId(@Param("wikiPageId") Long wikiPageId, Pageable pageable);

    /**
     * 根据Wiki页面ID查询所有历史版本
     *
     * @param wikiPageId Wiki页面ID
     * @return 历史版本列表（按版本号降序）
     */
    @Query("SELECT h FROM WikiPageHistory h WHERE h.wikiPageId = :wikiPageId ORDER BY h.version DESC")
    List<WikiPageHistory> findByWikiPageId(@Param("wikiPageId") Long wikiPageId);

    /**
     * 根据Wiki页面ID和版本号查询历史版本
     *
     * @param wikiPageId Wiki页面ID
     * @param version    版本号
     * @return 历史版本
     */
    Optional<WikiPageHistory> findByWikiPageIdAndVersion(Long wikiPageId, Integer version);

    /**
     * 查询Wiki页面的最新版本号
     *
     * @param wikiPageId Wiki页面ID
     * @return 最新版本号
     */
    @Query("SELECT COALESCE(MAX(h.version), 0) FROM WikiPageHistory h WHERE h.wikiPageId = :wikiPageId")
    Integer findMaxVersionByWikiPageId(@Param("wikiPageId") Long wikiPageId);

    /**
     * 根据编辑者ID查询历史记录
     *
     * @param editorId 编辑者ID
     * @param pageable 分页参数
     * @return 历史记录分页列表
     */
    Page<WikiPageHistory> findByEditorId(Long editorId, Pageable pageable);

    /**
     * 统计Wiki页面的历史版本数量
     *
     * @param wikiPageId Wiki页面ID
     * @return 版本数量
     */
    long countByWikiPageId(Long wikiPageId);

    /**
     * 批量删除Wiki页面的所有历史版本
     *
     * @param wikiPageId Wiki页面ID
     */
    void deleteByWikiPageId(Long wikiPageId);

    /**
     * 查询Wiki页面的最近N个版本
     *
     * @param wikiPageId Wiki页面ID
     * @param pageable   分页参数（limit）
     * @return 历史版本列表
     */
    @Query("SELECT h FROM WikiPageHistory h WHERE h.wikiPageId = :wikiPageId ORDER BY h.version DESC")
    List<WikiPageHistory> findRecentVersions(@Param("wikiPageId") Long wikiPageId, Pageable pageable);

    /**
     * 批量查询多个Wiki页面的历史版本
     *
     * @param wikiPageIds Wiki页面ID列表
     * @return 历史版本列表
     */
    @Query("SELECT h FROM WikiPageHistory h WHERE h.wikiPageId IN :wikiPageIds ORDER BY h.version DESC")
    List<WikiPageHistory> findByWikiPageIdIn(@Param("wikiPageIds") List<Long> wikiPageIds);
}

