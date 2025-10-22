package hbnu.project.zhiyanproject.repository;

import hbnu.project.zhiyanproject.model.entity.ProjectMember;
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
 * 项目成员数据访问层
 *
 * @author Tokito
 */
@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    /**
     * 根据项目ID和用户ID查询项目成员
     *
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return 项目成员
     */
    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);

    /**
     * 根据项目ID查询项目成员列表
     *
     * @param projectId 项目ID
     * @param pageable 分页参数
     * @return 项目成员分页列表
     */
    Page<ProjectMember> findByProjectId(Long projectId, Pageable pageable);

    /**
     * 根据项目ID查询项目成员列表
     *
     * @param projectId 项目ID
     * @return 项目成员列表
     */
    List<ProjectMember> findByProjectId(Long projectId);

    /**
     * 根据用户ID查询项目成员列表
     *
     * @param userId 用户ID
     * @return 项目成员列表
     */
    List<ProjectMember> findByUserId(Long userId);

    /**
     * 根据用户ID查询项目成员列表（分页）
     *
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 项目成员分页列表
     */
    Page<ProjectMember> findByUserId(Long userId, Pageable pageable);

    /**
     * 根据项目ID和角色查询项目成员列表
     *
     * @param projectId 项目ID
     * @param projectRole 项目角色
     * @return 项目成员列表
     */
    List<ProjectMember> findByProjectIdAndProjectRole(Long projectId, ProjectMemberRole projectRole);

    /**
     * 根据项目角色查询项目成员列表
     *
     * @param projectRole 项目角色
     * @return 项目成员列表
     */
    List<ProjectMember> findByProjectRole(ProjectMemberRole projectRole);

    /**
     * 检查用户是否为项目成员
     *
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return 是否为项目成员
     */
    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    /**
     * 统计项目成员数量
     *
     * @param projectId 项目ID
     * @return 成员数量
     */
    long countByProjectId(Long projectId);

    /**
     * 统计用户参与的项目数量
     *
     * @param userId 用户ID
     * @return 项目数量
     */
    long countByUserId(Long userId);

    /**
     * 统计使用指定角色的成员数量
     *
     * @param projectRole 项目角色
     * @return 成员数量
     */
    long countByProjectRole(ProjectMemberRole projectRole);

    /**
     * 根据项目ID和角色统计成员数量
     *
     * @param projectId 项目ID
     * @param projectRole 项目角色
     * @return 成员数量
     */
    long countByProjectIdAndProjectRole(Long projectId, ProjectMemberRole projectRole);

    /**
     * 根据项目ID删除所有成员
     *
     * @param projectId 项目ID
     * @return 删除的记录数
     */
    int deleteByProjectId(Long projectId);

    /**
     * 根据用户ID删除所有项目成员记录
     *
     * @param userId 用户ID
     * @return 删除的记录数
     */
    int deleteByUserId(Long userId);

    /**
     * 根据项目ID和用户ID删除项目成员
     *
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return 删除的记录数
     */
    int deleteByProjectIdAndUserId(Long projectId, Long userId);

    /**
     * 查询用户在指定项目中的角色
     *
     * @param userId 用户ID
     * @param projectId 项目ID
     * @return 用户角色
     */
    @Query("SELECT pm.projectRole FROM ProjectMember pm WHERE pm.userId = :userId AND pm.projectId = :projectId")
    Optional<ProjectMemberRole> findUserRoleInProject(@Param("userId") Long userId, @Param("projectId") Long projectId);

    /**
     * 查询用户参与的所有项目ID
     *
     * @param userId 用户ID
     * @return 项目ID列表
     */
    @Query("SELECT pm.projectId FROM ProjectMember pm WHERE pm.userId = :userId")
    List<Long> findProjectIdsByUserId(@Param("userId") Long userId);

    /**
     * 查询项目的所有成员用户ID
     *
     * @param projectId 项目ID
     * @return 用户ID列表
     */
    @Query("SELECT pm.userId FROM ProjectMember pm WHERE pm.projectId = :projectId")
    List<Long> findUserIdsByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据多个项目ID查询成员
     *
     * @param projectIds 项目ID列表
     * @return 项目成员列表
     */
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.projectId IN :projectIds")
    List<ProjectMember> findByProjectIdIn(@Param("projectIds") List<Long> projectIds);
}
