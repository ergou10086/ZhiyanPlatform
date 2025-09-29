package hbnu.project.zhiyanauth.repository;

import hbnu.project.zhiyanauth.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 角色数据访问接口
 *
 * @author ErgouTree
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * 根据角色名称查找角色
     *
     * @param name 角色名称
     * @return 角色对象（可能为空）
     */
    Optional<Role> findByName(String name);

    /**
     * 检查角色名称是否已存在
     *
     * @param name 角色名称
     * @return 是否存在
     */
    boolean existsByName(String name);

    /**
     * 查询用户的所有角色
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    @Query("SELECT r FROM Role r " +
           "JOIN r.userRoles ur " +
           "WHERE ur.user.id = :userId")
    List<Role> findAllByUserId(@Param("userId") Long userId);

    /**
     * 查询角色及其权限
     *
     * @param roleId 角色ID
     * @return 角色对象（可能为空）
     */
    @Query("SELECT DISTINCT r FROM Role r " +
           "LEFT JOIN FETCH r.rolePermissions rp " +
           "LEFT JOIN FETCH rp.permission p " +
           "WHERE r.id = :roleId")
    Optional<Role> findByIdWithPermissions(@Param("roleId") Long roleId);
}