package hbnu.project.zhiyanauth.service;

import hbnu.project.zhiyanauth.model.dto.UserDTO;
import hbnu.project.zhiyanauth.model.form.UserProfileUpdateBody;
import hbnu.project.zhiyancommonbasic.domain.R;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 用户服务接口
 * 提供用户管理、认证、权限等核心功能
 *
 * @author ErgouTree
 */
public interface UserService {

    /**
     * 获取当前用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    R<UserDTO> getCurrentUser(Long userId);

    /**
     * 更新用户资料
     *
     * @param userId 用户ID
     * @param updateBody 更新表单
     * @return 更新结果
     */
    R<UserDTO> updateUserProfile(Long userId, UserProfileUpdateBody updateBody);

    /**
     * 分页查询用户列表（管理员功能）
     *
     * @param pageable 分页参数
     * @param keyword 搜索关键词
     * @return 用户列表
     */
    R<Page<UserDTO>> getUserList(Pageable pageable, String keyword);

    /**
     * 锁定/解锁用户
     *
     * @param userId 用户ID
     * @param isLocked 是否锁定
     * @return 操作结果
     */
    R<Void> lockUser(Long userId, boolean isLocked);

    /**
     * 软删除用户
     *
     * @param userId 用户ID
     * @return 删除结果
     */
    R<Void> deleteUser(Long userId);

    /**
     * 获取用户详细信息（包含角色和权限）
     *
     * @param userId 用户ID
     * @return 用户详细信息
     */
    R<UserDTO> getUserWithRolesAndPermissions(Long userId);
}
