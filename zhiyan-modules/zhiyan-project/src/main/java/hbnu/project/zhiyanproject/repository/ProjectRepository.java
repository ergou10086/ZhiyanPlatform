package hbnu.project.zhiyanproject.repository;

import hbnu.project.zhiyanproject.model.entity.Project;
import hbnu.project.zhiyanproject.model.enums.ProjectStatus;
import hbnu.project.zhiyanproject.model.enums.ProjectVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 项目数据访问层
 *
 * @author Tokito
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * 根据项目名称查询项目
     *
     * @param name 项目名称
     * @return 项目
     */
    Optional<Project> findByName(String name);

    /**
     * 根据创建者ID查询项目列表（不包含已删除的项目）
     *
     * @param creatorId 创建者ID
     * @param isDeleted 是否删除标记
     * @param pageable 分页参数
     * @return 项目分页列表
     */
    Page<Project> findByCreatorIdAndIsDeleted(Long creatorId, Boolean isDeleted, Pageable pageable);

    /**
     * 根据创建者ID查询项目列表（不包含已删除的项目）
     *
     * @param creatorId 创建者ID
     * @param isDeleted 是否删除标记
     * @return 项目列表
     */
    List<Project> findByCreatorIdAndIsDeleted(Long creatorId, Boolean isDeleted);

    /**
     * 根据项目状态查询项目列表（不包含已删除的项目）
     *
     * @param status 项目状态
     * @param isDeleted 是否删除标记
     * @param pageable 分页参数
     * @return 项目分页列表
     */
    Page<Project> findByStatusAndIsDeleted(ProjectStatus status, Boolean isDeleted, Pageable pageable);

    /**
     * 根据可见性查询项目列表（不包含已删除的项目）
     *
     * @param visibility 可见性
     * @param isDeleted 是否删除标记
     * @param pageable 分页参数
     * @return 项目分页列表
     */
    Page<Project> findByVisibilityAndIsDeleted(ProjectVisibility visibility, Boolean isDeleted, Pageable pageable);

    /**
     * 根据项目名称模糊查询
     *
     * @param name 项目名称关键字
     * @param pageable 分页参数
     * @return 项目分页列表
     */
    Page<Project> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * 根据描述模糊查询
     *
     * @param description 描述关键字
     * @param pageable 分页参数
     * @return 项目分页列表
     */
    Page<Project> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);

    /**
     * 检查项目名称是否存在
     *
     * @param name 项目名称
     * @return 是否存在
     */
    boolean existsByName(String name);

    /**
     * 检查未删除的项目名称是否存在
     *
     * @param name 项目名称
     * @return 是否存在
     */
    boolean existsByNameAndIsDeletedFalse(String name);

    /**
     * 检查项目名称是否存在（排除指定ID）
     *
     * @param name 项目名称
     * @param excludeId 排除的项目ID
     * @return 是否存在
     */
    boolean existsByNameAndIdNot(String name, Long excludeId);

    /**
     * 检查未删除的项目名称是否存在（排除指定ID）
     *
     * @param name 项目名称
     * @param excludeId 排除的项目ID
     * @param isDeleted 是否删除标记
     * @return 是否存在
     */
    boolean existsByNameAndIdNotAndIsDeleted(String name, Long excludeId, Boolean isDeleted);

    /**
     * 统计创建者的项目数量
     *
     * @param creatorId 创建者ID
     * @return 项目数量
     */
    long countByCreatorId(Long creatorId);

    /**
     * 统计指定状态的项目数量
     *
     * @param status 项目状态
     * @return 项目数量
     */
    long countByStatus(ProjectStatus status);

    /**
     * 根据创建时间范围查询项目
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 项目分页列表
     */
    @Query("SELECT p FROM Project p WHERE p.createdAt BETWEEN :startTime AND :endTime")
    Page<Project> findByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, 
                                        @Param("endTime") LocalDateTime endTime, 
                                        Pageable pageable);

    /**
     * 查询公开项目（排除已删除和已归档）
     *
     * @param pageable 分页参数
     * @return 项目分页列表
     */
    @Query("SELECT p FROM Project p WHERE p.visibility = 'PUBLIC' AND p.status != 'ARCHIVED' AND p.isDeleted = false")
    Page<Project> findPublicActiveProjects(Pageable pageable);

    /**
     * 查询未删除的项目
     *
     * @param pageable 分页参数
     * @return 项目分页列表
     */
    @Query("SELECT p FROM Project p WHERE p.isDeleted = false")
    Page<Project> findAllActive(Pageable pageable);

    /**
     * 根据关键字搜索项目（名称或描述，不包含已删除的项目）
     *
     * @param keyword 关键字
     * @param pageable 分页参数
     * @return 项目分页列表
     */
    @Query("SELECT p FROM Project p WHERE p.isDeleted = false AND (p.name LIKE %:keyword% OR p.description LIKE %:keyword%)")
    Page<Project> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 查询用户参与的项目（包括创建的和加入的，不包含已删除的项目）
     *
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 项目分页列表
     */
    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN ProjectMember pm ON p.id = pm.projectId " +
           "WHERE (p.creatorId = :userId OR pm.userId = :userId) AND p.isDeleted = false")
    Page<Project> findUserProjects(@Param("userId") Long userId, Pageable pageable);

    /**
     * 查询最近更新的项目
     *
     * @param limit 限制数量
     * @return 项目列表
     */
    @Query("SELECT p FROM Project p ORDER BY p.updatedAt DESC")
    List<Project> findRecentlyUpdatedProjects(Pageable pageable);

    /**
     * 查询热门项目（根据成员数量排序）
     *
     * @param pageable 分页参数
     * @return 项目分页列表
     */
    @Query("SELECT p, COUNT(pm) as memberCount FROM Project p LEFT JOIN ProjectMember pm ON p.id = pm.projectId " +
           "WHERE p.visibility = 'PUBLIC' AND p.status = 'ACTIVE' " +
           "GROUP BY p ORDER BY memberCount DESC")
    Page<Object[]> findPopularProjects(Pageable pageable);
}
