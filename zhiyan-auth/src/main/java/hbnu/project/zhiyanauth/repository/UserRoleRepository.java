package hbnu.project.zhiyanauth.repository;

import hbnu.project.zhiyanauth.model.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户角色关联数据访问接口
 *
 * @author ErgouTree
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    /**
     * 根据用户ID和角色ID查找用户角色关联
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 用户角色关联对象（可能为空）
     */
    Optional<UserRole> findByUserIdAndRoleId(Long userId, Long roleId);

    /**
     * 根据用户ID查找用户角色关联列表
     *
     * @param userId 用户ID
     * @return 用户角色关联列表
     */
    List<UserRole> findByUserId(Long userId);

    /**
     * 根据角色ID查找用户角色关联列表
     *
     * @param roleId 角色ID
     * @return 用户角色关联列表
     */
    List<UserRole> findByRoleId(Long roleId);

    /**
     * 删除用户的所有角色关联
     *
     * @param userId 用户ID
     * @return 删除的记录数
     */
    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.user.id = :userId")
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 删除指定用户的指定角色关联
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 删除的记录数
     */
    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.user.id = :userId AND ur.role.id = :roleId")
    int deleteByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);
}