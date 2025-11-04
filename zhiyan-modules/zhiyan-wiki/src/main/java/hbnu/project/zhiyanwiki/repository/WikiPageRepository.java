package hbnu.project.zhiyanwiki.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import hbnu.project.zhiyanwiki.model.entity.WikiPage;

import java.util.List;
import java.util.Optional;

/**
 * Wiki页面数据访问层
 * 提供Wiki页面的CRUD和树状结构查询
 *
 * @author ErgouTree
 */
@Repository
public interface WikiPageRepository extends JpaRepository<WikiPage, Long> {

    /**
     * 根据项目ID查询所有Wiki页面
     *
     * @param projectId 项目ID
     * @return Wiki页面列表
     */
    List<WikiPage> findByProjectId(Long projectId);

    /**
     * 根据项目ID查询所有Wiki页面（分页）
     *
     * @param projectId 项目ID
     * @param pageable  分页参数
     * @return Wiki页面分页列表
     */
    Page<WikiPage> findByProjectId(Long projectId, Pageable pageable);

    /**
     * 根据项目ID和父页面ID查询子页面列表
     *
     * @param projectId 项目ID
     * @param parentId  父页面ID
     * @return 子页面列表（按排序序号升序）
     */
    @Query("SELECT w FROM WikiPage w WHERE w.projectId = :projectId AND w.parentId = :parentId " +
           "ORDER BY w.sortOrder ASC, w.createdAt ASC")
    List<WikiPage> findChildPages(@Param("projectId") Long projectId, @Param("parentId") Long parentId);

    /**
     * 查询项目下的根页面（无父页面）
     *
     * @param projectId 项目ID
     * @return 根页面列表（按排序序号升序）
     */
    @Query("SELECT w FROM WikiPage w WHERE w.projectId = :projectId AND w.parentId IS NULL " +
           "ORDER BY w.sortOrder ASC, w.createdAt ASC")
    List<WikiPage> findRootPages(@Param("projectId") Long projectId);

    /**
     * 根据项目ID和标题查询Wiki页面
     *
     * @param projectId 项目ID
     * @param title     页面标题
     * @return Wiki页面
     */
    Optional<WikiPage> findByProjectIdAndTitle(Long projectId, String title);

    /**
     * 根据项目ID和标题模糊查询Wiki页面
     *
     * @param projectId 项目ID
     * @param title     标题关键字
     * @param pageable  分页参数
     * @return Wiki页面分页列表
     */
    Page<WikiPage> findByProjectIdAndTitleContaining(Long projectId, String title, Pageable pageable);

    /**
     * 根据创建者ID查询Wiki页面
     *
     * @param creatorId 创建者ID
     * @param pageable  分页参数
     * @return Wiki页面分页列表
     */
    Page<WikiPage> findByCreatorId(Long creatorId, Pageable pageable);

    /**
     * 根据项目ID和创建者ID查询Wiki页面
     *
     * @param projectId 项目ID
     * @param creatorId 创建者ID
     * @return Wiki页面列表
     */
    List<WikiPage> findByProjectIdAndCreatorId(Long projectId, Long creatorId);

    /**
     * 统计项目下的Wiki页面数量
     *
     * @param projectId 项目ID
     * @return 页面数量
     */
    long countByProjectId(Long projectId);

    /**
     * 统计指定页面下的子页面数量
     *
     * @param parentId 父页面ID
     * @return 子页面数量
     */
    long countByParentId(Long parentId);

    /**
     * 检查项目下是否存在指定标题的页面
     *
     * @param projectId 项目ID
     * @param title     页面标题
     * @return 是否存在
     */
    boolean existsByProjectIdAndTitle(Long projectId, String title);

    /**
     * 批量删除项目下的所有Wiki页面
     *
     * @param projectId 项目ID
     */
    void deleteByProjectId(Long projectId);

    /**
     * 查询公开的Wiki页面（分页）
     *
     * @param pageable 分页参数
     * @return Wiki页面分页列表
     */
    @Query("SELECT w FROM WikiPage w WHERE w.isPublic = true ORDER BY w.updatedAt DESC")
    Page<WikiPage> findPublicPages(Pageable pageable);

    /**
     * 根据项目ID查询公开的Wiki页面
     *
     * @param projectId 项目ID
     * @return Wiki页面列表
     */
    List<WikiPage> findByProjectIdAndIsPublicTrue(Long projectId);

    /**
     * 根据路径查询Wiki页面
     *
     * @param projectId 项目ID
     * @param path      页面路径
     * @return Wiki页面
     */
    Optional<WikiPage> findByProjectIdAndPath(Long projectId, String path);

    /**
     * 查询最近更新的Wiki页面
     *
     * @param projectId 项目ID
     * @param pageable  分页参数
     * @return Wiki页面列表
     */
    @Query("SELECT w FROM WikiPage w WHERE w.projectId = :projectId ORDER BY w.updatedAt DESC")
    List<WikiPage> findRecentlyUpdated(@Param("projectId") Long projectId, Pageable pageable);

    /**
     * 根据ID和项目ID查询Wiki页面（用于权限校验）
     *
     * @param id        页面ID
     * @param projectId 项目ID
     * @return Wiki页面
     */
    Optional<WikiPage> findByIdAndProjectId(Long id, Long projectId);

    /**
     * 查询指定父页面下的最大排序序号
     *
     * @param projectId 项目ID
     * @param parentId  父页面ID
     * @return 最大排序序号
     */
    @Query("SELECT COALESCE(MAX(w.sortOrder), 0) FROM WikiPage w " +
           "WHERE w.projectId = :projectId AND w.parentId = :parentId")
    Integer findMaxSortOrder(@Param("projectId") Long projectId, @Param("parentId") Long parentId);

    /**
     * 三项查询
     * @param projectId
     * @param title
     * @param projectId1
     * @return
     */
    List<WikiPage> findByProjectIdAndTitleAndParentId(Long projectId, String title, Long projectId1);
}

