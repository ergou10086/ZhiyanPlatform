package hbnu.project.zhiyanproject.repository;

import hbnu.project.zhiyanproject.model.entity.ProjectRole;
import hbnu.project.zhiyanproject.model.enums.ProjectMemberRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 项目角色数据访问层
 *
 * @author Tokito
 */
@Repository
public interface ProjectRoleRepository extends JpaRepository<ProjectRole, Long> {

    /**
     * 根据项目ID查询项目角色列表
     *
     * @param projectId 项目ID
     * @param pageable 分页参数
     * @return 项目角色分页列表
     */
    Page<ProjectRole> findByProjectId(Long projectId, Pageable pageable);

    /**
     * 根据项目ID查询项目角色列表
     *
     * @param projectId 项目ID
     * @return 项目角色列表
     */
    List<ProjectRole> findByProjectId(Long projectId);

    /**
     * 根据项目ID和角色名称查询项目角色
     *
     * @param projectId 项目ID
     * @param name 角色名称
     * @return 项目角色
     */
    Optional<ProjectRole> findByProjectIdAndName(Long projectId, String name);

    /**
     * 根据项目ID和角色枚举查询项目角色
     *
     * @param projectId 项目ID
     * @param roleEnum 角色枚举
     * @return 项目角色
     */
    Optional<ProjectRole> findByProjectIdAndRoleEnum(Long projectId, ProjectMemberRole roleEnum);

    /**
     * 检查项目中是否存在指定名称的角色
     *
     * @param projectId 项目ID
     * @param name 角色名称
     * @return 是否存在
     */
    boolean existsByProjectIdAndName(Long projectId, String name);

    /**
     * 检查项目中是否存在指定名称的角色（排除指定ID）
     *
     * @param projectId 项目ID
     * @param name 角色名称
     * @param excludeId 排除的角色ID
     * @return 是否存在
     */
    boolean existsByProjectIdAndNameAndIdNot(Long projectId, String name, Long excludeId);

    /**
     * 根据项目ID和角色枚举检查是否存在
     *
     * @param projectId 项目ID
     * @param roleEnum 角色枚举
     * @return 是否存在
     */
    boolean existsByProjectIdAndRoleEnum(Long projectId, ProjectMemberRole roleEnum);

    /**
     * 根据角色类型查询角色列表
     *
     * @param roleType 角色类型
     * @param pageable 分页参数
     * @return 角色分页列表
     */
    Page<ProjectRole> findByRoleType(String roleType, Pageable pageable);

    /**
     * 查询系统默认角色
     *
     * @param isSystemDefault 是否为系统默认角色
     * @param pageable 分页参数
     * @return 角色分页列表
     */
    Page<ProjectRole> findByIsSystemDefault(Boolean isSystemDefault, Pageable pageable);

    /**
     * 根据项目ID删除所有角色
     *
     * @param projectId 项目ID
     * @return 删除的记录数
     */
    int deleteByProjectId(Long projectId);

    /**
     * 统计项目中的角色数量
     *
     * @param projectId 项目ID
     * @return 角色数量
     */
    long countByProjectId(Long projectId);

    /**
     * 查询项目中的默认角色
     *
     * @param projectId 项目ID
     * @param isSystemDefault 是否为系统默认角色
     * @return 默认角色列表
     */
    List<ProjectRole> findByProjectIdAndIsSystemDefault(Long projectId, Boolean isSystemDefault);

    /**
     * 根据多个项目ID查询角色
     *
     * @param projectIds 项目ID列表
     * @return 角色列表
     */
    @Query("SELECT pr FROM ProjectRole pr WHERE pr.projectId IN :projectIds")
    List<ProjectRole> findByProjectIdIn(@Param("projectIds") List<Long> projectIds);
}
