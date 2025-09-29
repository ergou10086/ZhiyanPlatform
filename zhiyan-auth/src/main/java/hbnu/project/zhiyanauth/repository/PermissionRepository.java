package hbnu.project.zhiyanauth.repository;

import hbnu.project.zhiyanauth.model.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 权限数据访问接口
 *
 * @author ErgouTree
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * 根据权限名称查找权限
     *
     * @param name 权限名称
     * @return 权限对象（可能为空）
     */
    Optional<Permission> findByName(String name);

    /**
     * 根据权限名称列表批量查找权限
     *
     * @param names 权限名称列表
     * @return 权限列表
     */
    List<Permission> findByNameIn(List<String> names);

    /**
     * 检查权限名称是否已存在
     *
     * @param name 权限名称
     * @return 是否存在
     */
    boolean existsByName(String name);

    /**
     * 查询角色的所有权限
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Query("SELECT p FROM Permission p " +
           "JOIN p.rolePermissions rp " +
           "WHERE rp.role.id = :roleId")
    List<Permission> findAllByRoleId(@Param("roleId") Long roleId);

    /**
     * 查询用户的所有权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @Query("SELECT DISTINCT p FROM Permission p " +
           "JOIN p.rolePermissions rp " +
           "JOIN rp.role r " +
           "JOIN r.userRoles ur " +
           "WHERE ur.user.id = :userId")
    List<Permission> findAllByUserId(@Param("userId") Long userId);
}