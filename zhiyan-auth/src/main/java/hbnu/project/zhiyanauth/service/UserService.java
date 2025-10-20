package hbnu.project.zhiyanauth.service;

import hbnu.project.zhiyanauth.model.dto.UserDTO;
import hbnu.project.zhiyanauth.model.form.UserProfileUpdateBody;
import hbnu.project.zhiyancommonbasic.domain.R;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 用户服务接口
 * 提供用户信息管理、权限查询等核心功能
 * 
 * 职责说明：
 * - 本服务专注于用户信息管理（查询、更新、删除等）
 * - 认证相关功能（注册、登录、token管理）由 AuthService 负责
 * - 本服务为其他微服务提供用户信息查询接口
 *
 * @author ErgouTree
 * @version 3.0
 * @rewrite Tokito
 */
public interface UserService {

    /**
     * 获取当前用户信息（不含角色和权限）
     * 
     * @param userId 用户ID
     * @return 用户基本信息
     */
    R<UserDTO> getCurrentUser(Long userId);

    /**
     * 获取用户详细信息（包含角色和权限）
     * 用于权限校验、用户详情页展示等场景
     * 
     * @param userId 用户ID
     * @return 用户详细信息（包含角色列表和权限列表）
     */
    R<UserDTO> getUserWithRolesAndPermissions(Long userId);

    /**
     * 根据邮箱查询用户信息（服务间调用接口）
     * 供其他微服务通过Feign调用
     * 
     * @param email 用户邮箱
     * @return 用户信息
     */
    R<UserDTO> getUserByEmail(String email);

    /**
     * 根据姓名查询用户信息（服务间调用接口）
     * 供其他微服务通过Feign调用
     * 
     * @param name 用户姓名
     * @return 用户信息
     */
    R<UserDTO> getUserByName(String name);

    /**
     * 批量根据用户ID查询用户信息（服务间调用接口）
     * 用于项目服务批量查询成员信息等场景
     * 
     * @param userIds 用户ID列表
     * @return 用户信息列表
     */
    R<List<UserDTO>> getUsersByIds(List<Long> userIds);

    /**
     * 更新用户个人资料
     * 普通用户只能更新自己的资料（姓名、头像、职称、机构等）
     * 
     * @param userId 用户ID
     * @param updateBody 更新内容
     * @return 更新后的用户信息
     */
    R<UserDTO> updateUserProfile(Long userId, UserProfileUpdateBody updateBody);

    /**
     * 分页查询用户列表（管理员功能）
     * 支持关键词搜索（邮箱、姓名、机构）
     * 
     * @param pageable 分页参数
     * @param keyword 搜索关键词（可选）
     * @return 用户列表（分页）
     */
    R<Page<UserDTO>> getUserList(Pageable pageable, String keyword);

    /**
     * 锁定用户（管理员功能）
     * 锁定后用户无法登录系统
     * 
     * @param userId 用户ID
     * @param isLocked 是否锁定（true=锁定，false=解锁）
     * @return 操作结果
     */
    R<Void> lockUser(Long userId, boolean isLocked);

    /**
     * 软删除用户（管理员功能）
     * 标记用户为已删除，不会从数据库中物理删除
     * 
     * @param userId 用户ID
     * @return 操作结果
     */
    R<Void> deleteUser(Long userId);

    /**
     * 获取用户的所有角色（内部接口/权限服务调用）
     * 
     * @param userId 用户ID
     * @return 角色名称列表
     */
    R<List<String>> getUserRoles(Long userId);

    /**
     * 获取用户的所有权限（内部接口/权限服务调用）
     * 根据用户的所有角色计算出权限集合
     * 
     * @param userId 用户ID
     * @return 权限名称列表
     */
    R<List<String>> getUserPermissions(Long userId);

    /**
     * 检查用户是否拥有指定权限（内部接口/API网关调用）
     * 用于权限校验流程
     * 
     * @param userId 用户ID
     * @param permission 权限标识符（如 "project:create"）
     * @return 是否拥有该权限
     */
    R<Boolean> hasPermission(Long userId, String permission);

    /**
     * 批量检查用户是否拥有多个权限（内部接口）
     * 用于一次性校验多个权限
     * 
     * @param userId 用户ID
     * @param permissions 权限标识符列表
     * @return 权限校验结果Map（权限 -> 是否拥有）
     */
    R<java.util.Map<String, Boolean>> hasPermissions(Long userId, List<String> permissions);

    /**
     * 检查用户是否拥有指定角色
     * 
     * @param userId 用户ID
     * @param roleName 角色名称（如 "ADMIN", "TEACHER"）
     * @return 是否拥有该角色
     */
    R<Boolean> hasRole(Long userId, String roleName);

    /**
     * 搜索用户（用于项目成员邀请等场景）
     * 根据邮箱或姓名模糊搜索用户
     * 
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @return 用户列表（只返回基本信息）
     */
    R<Page<UserDTO>> searchUsers(String keyword, Pageable pageable);
}
