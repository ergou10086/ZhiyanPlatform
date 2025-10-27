package hbnu.project.zhiyanauth.service.impl;

import hbnu.project.zhiyanauth.mapper.UserMapper;
import hbnu.project.zhiyanauth.model.dto.UserDTO;
import hbnu.project.zhiyanauth.model.entity.Permission;
import hbnu.project.zhiyanauth.model.entity.User;
import hbnu.project.zhiyanauth.model.entity.UserRole;
import hbnu.project.zhiyanauth.model.form.UserProfileUpdateBody;
import hbnu.project.zhiyanauth.repository.PermissionRepository;
import hbnu.project.zhiyanauth.repository.UserRepository;
import hbnu.project.zhiyanauth.service.UserService;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


/**
 * 用户服务实现类
 * 提供用户管理、权限查询等核心功能
 * 
 * 实现说明：
 * 1. 所有涉及角色和权限的查询，优先从缓存获取（未来可集成Redis）
 * 2. 用户信息更新操作需要事务保护
 * 3. 权限校验结果可缓存，避免频繁查询数据库
 * 4. 服务间调用接口需要考虑性能优化
 *
 * @author ErgouTree
 * @version 2.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final UserMapper userMapper;

    /**
     * 获取当前用户信息（不含角色和权限）
     * 用于用户基本信息展示，不涉及权限相关逻辑
     */
    @Override
    @Transactional(readOnly = true)
    public R<UserDTO> getCurrentUser(Long userId) {
        try {
            log.debug("获取当前用户信息 - userId: {}", userId);
            
            Optional<User> optionalUser = userRepository.findByIdAndIsDeletedFalse(userId);
            if (optionalUser.isEmpty()) {
                log.warn("用户不存在或已被删除 - userId: {}", userId);
                return R.fail("用户不存在");
            }

            User user = optionalUser.get();
            UserDTO userDTO = userMapper.toDTO(user);
            
            log.debug("成功获取用户信息 - userId: {}, email: {}", userId, user.getEmail());
            return R.ok(userDTO);

        } catch (Exception e) {
            log.error("获取用户信息异常 - userId: {}, 错误: {}", userId, e.getMessage(), e);
            return R.fail("获取用户信息失败");
        }
    }

    /**
     * 获取用户详细信息（包含角色和权限）
     * 用于权限校验、用户详情页展示等场景
     */
    @Override
    @Transactional(readOnly = true)
    public R<UserDTO> getUserWithRolesAndPermissions(Long userId) {
        try {
            log.debug("获取用户详细信息（含角色权限） - userId: {}", userId);
            
            // 查询用户及其角色信息（避免 MultipleBagFetchException）
            Optional<User> optionalUser = userRepository.findByIdWithRolesAndPermissions(userId);
            if (optionalUser.isEmpty()) {
                log.warn("用户不存在或已被删除 - userId: {}", userId);
                return R.fail("用户不存在");
            }

            User user = optionalUser.get();
            
            // 转换为DTO，包含角色
            UserDTO userDTO = userMapper.toDTOWithRoles(user);
            
            // 单独查询权限（避免N+1问题和懒加载问题）
            List<Permission> permissions = permissionRepository.findAllByUserId(userId);
            List<String> permissionNames = permissions.stream()
                    .map(Permission::getName)
                    .distinct()
                    .collect(Collectors.toList());
            userDTO.setPermissions(permissionNames);

            log.debug("成功获取用户详细信息 - userId: {}, 角色数: {}, 权限数: {}", 
                    userId, userDTO.getRoles().size(), permissionNames.size());
            return R.ok(userDTO);

        } catch (Exception e) {
            log.error("获取用户详细信息异常 - userId: {}, 错误: {}", userId, e.getMessage(), e);
            return R.fail("获取用户信息失败");
        }
    }

    /**
     * 根据邮箱查询用户信息（服务间调用接口）
     * 供项目服务、团队服务等通过Feign调用
     */
    @Override
    @Transactional(readOnly = true)
    public R<UserDTO> getUserByEmail(String email) {
        try {
            log.debug("根据邮箱查询用户 - email: {}", email);
            
            if (StringUtils.isBlank(email)) {
                return R.fail("邮箱不能为空");
            }

            Optional<User> optionalUser = userRepository.findByEmailAndIsDeletedFalse(email);
            if (optionalUser.isEmpty()) {
                log.warn("用户不存在 - email: {}", email);
                return R.fail("用户不存在");
            }

            User user = optionalUser.get();
            UserDTO userDTO = userMapper.toDTO(user);
            
            log.debug("成功查询到用户 - email: {}, userId: {}", email, user.getId());
            return R.ok(userDTO);

        } catch (Exception e) {
            log.error("根据邮箱查询用户异常 - email: {}, 错误: {}", email, e.getMessage(), e);
            return R.fail("查询用户失败");
        }
    }

    /**
     * 根据姓名查询用户信息（服务间调用接口）
     * 供其他微服务通过Feign调用
     */
    @Override
    @Transactional(readOnly = true)
    public R<UserDTO> getUserByName(String name) {
        try {
            log.debug("根据姓名查询用户 - name: {}", name);
            
            if (StringUtils.isBlank(name)) {
                return R.fail("姓名不能为空");
            }

            Optional<User> optionalUser = userRepository.findByNameAndIsDeletedFalse(name);
            if (optionalUser.isEmpty()) {
                log.warn("用户不存在 - name: {}", name);
                return R.fail("用户不存在");
            }

            User user = optionalUser.get();
            UserDTO userDTO = userMapper.toDTO(user);
            
            log.debug("成功查询到用户 - name: {}, userId: {}", name, user.getId());
            return R.ok(userDTO);

        } catch (Exception e) {
            log.error("根据姓名查询用户异常 - name: {}, 错误: {}", name, e.getMessage(), e);
            return R.fail("查询用户失败");
        }
    }

    /**
     * 批量根据用户ID查询用户信息（服务间调用接口）
     * 用于项目服务批量查询成员信息等场景
     */
    @Override
    @Transactional(readOnly = true)
    public R<List<UserDTO>> getUsersByIds(List<Long> userIds) {
        try {
            log.debug("批量查询用户信息 - 数量: {}", userIds.size());
            
            if (userIds == null || userIds.isEmpty()) {
                return R.ok(Collections.emptyList());
            }

            List<User> users = userRepository.findAllById(userIds);
            
            // 过滤掉已删除的用户
            List<User> activeUsers = users.stream()
                    .filter(user -> !user.getIsDeleted())
                    .collect(Collectors.toList());
            
            List<UserDTO> userDTOs = userMapper.toDTOList(activeUsers);
            
            log.debug("成功批量查询用户 - 请求数: {}, 查询到: {}", userIds.size(), userDTOs.size());
            return R.ok(userDTOs);

        } catch (Exception e) {
            log.error("批量查询用户异常 - 错误: {}", e.getMessage(), e);
            return R.fail("批量查询用户失败");
        }
    }

    /**
     * 更新用户个人资料
     * 普通用户只能更新自己的资料
     */
    @Override
    @Transactional
    public R<UserDTO> updateUserProfile(Long userId, UserProfileUpdateBody updateBody) {
        try {
            log.info("更新用户资料 - userId: {}", userId);
            
            Optional<User> optionalUser = userRepository.findByIdAndIsDeletedFalse(userId);
            if (optionalUser.isEmpty()) {
                return R.fail("用户不存在");
            }

            User user = optionalUser.get();

            // 使用MapStruct更新用户信息
            userMapper.updateUserProfile(user, updateBody);

            user = userRepository.save(user);
            UserDTO userDTO = userMapper.toDTO(user);

            log.info("用户资料更新成功 - userId: {}", userId);
            return R.ok(userDTO, "资料更新成功");

        } catch (Exception e) {
            log.error("用户资料更新异常 - userId: {}, 错误: {}", userId, e.getMessage(), e);
            return R.fail("资料更新失败，请稍后重试");
        }
    }

    /**
     * 分页查询用户列表（管理员功能）
     * 支持关键词搜索（邮箱、姓名）
     */
    @Override
    @Transactional(readOnly = true)
    public R<Page<UserDTO>> getUserList(Pageable pageable, String keyword) {
        try {
            log.debug("查询用户列表 - 页码: {}, 每页数量: {}, 关键词: {}", 
                    pageable.getPageNumber(), pageable.getPageSize(), keyword);
            
            Page<User> userPage;

            if (StringUtils.isNotBlank(keyword)) {
                userPage = userRepository.findByNameContainingOrEmailContainingAndIsDeletedFalse(
                        keyword, keyword, pageable);
            } else {
                userPage = userRepository.findByIsDeletedFalse(pageable);
            }

            List<UserDTO> userDTOs = userMapper.toDTOList(userPage.getContent());
            Page<UserDTO> userDTOPage = new PageImpl<>(userDTOs, pageable, userPage.getTotalElements());
            
            log.debug("查询用户列表成功 - 总数: {}, 当前页数量: {}", 
                    userPage.getTotalElements(), userDTOs.size());
            return R.ok(userDTOPage);

        } catch (Exception e) {
            log.error("查询用户列表异常 - 错误: {}", e.getMessage(), e);
            return R.fail("查询用户列表失败");
        }
    }

    /**
     * 锁定/解锁用户（管理员功能）
     * 锁定后用户无法登录系统
     */
    @Override
    @Transactional
    public R<Void> lockUser(Long userId, boolean isLocked) {
        try {
            log.info("{}用户 - userId: {}", isLocked ? "锁定" : "解锁", userId);
            
            Optional<User> optionalUser = userRepository.findByIdAndIsDeletedFalse(userId);
            if (optionalUser.isEmpty()) {
                return R.fail("用户不存在");
            }

            User user = optionalUser.get();
            user.setIsLocked(isLocked);
            userRepository.save(user);

            String action = isLocked ? "锁定" : "解锁";
            log.info("用户{}成功 - userId: {}", action, userId);
            return R.ok(null, "用户" + action + "成功");

        } catch (Exception e) {
            log.error("用户锁定/解锁异常 - userId: {}, 错误: {}", userId, e.getMessage(), e);
            return R.fail("操作失败，请稍后重试");
        }
    }

    /**
     * 软删除用户（管理员功能）
     * 标记用户为已删除，不会从数据库中物理删除
     */
    @Override
    @Transactional
    public R<Void> deleteUser(Long userId) {
        try {
            log.info("删除用户 - userId: {}", userId);
            
            Optional<User> optionalUser = userRepository.findByIdAndIsDeletedFalse(userId);
            if (optionalUser.isEmpty()) {
                return R.fail("用户不存在");
            }

            User user = optionalUser.get();
            user.setIsDeleted(true);
            userRepository.save(user);

            log.info("用户删除成功 - userId: {}", userId);
            return R.ok(null, "用户删除成功");

        } catch (Exception e) {
            log.error("用户删除异常 - userId: {}, 错误: {}", userId, e.getMessage(), e);
            return R.fail("用户删除失败，请稍后重试");
        }
    }

    /**
     * 获取用户的所有角色
     * 用于权限服务调用
     */
    @Override
    @Transactional(readOnly = true)
    public R<List<String>> getUserRoles(Long userId) {
        try {
            log.debug("获取用户角色 - userId: {}", userId);
            
            Optional<User> optionalUser = userRepository.findByIdWithRolesAndPermissions(userId);
            if (optionalUser.isEmpty()) {
                log.warn("用户不存在 - userId: {}", userId);
                return R.fail("用户不存在");
            }

            User user = optionalUser.get();
            List<String> roles = user.getUserRoles().stream()
                    .map(ur -> ur.getRole().getName())
                    .distinct()
                    .collect(Collectors.toList());

            log.debug("成功获取用户角色 - userId: {}, 角色数: {}", userId, roles.size());
            return R.ok(roles);

        } catch (Exception e) {
            log.error("获取用户角色异常 - userId: {}, 错误: {}", userId, e.getMessage(), e);
            return R.fail("获取用户角色失败");
        }
    }

    /**
     * 获取用户的所有权限
     * 根据用户的所有角色计算出权限集合
     */
    @Override
    @Transactional(readOnly = true)
    public R<List<String>> getUserPermissions(Long userId) {
        try {
            log.debug("获取用户权限 - userId: {}", userId);
            
            // 使用专门的Repository方法查询权限
            List<Permission> permissions = permissionRepository.findAllByUserId(userId);
            List<String> permissionNames = permissions.stream()
                    .map(Permission::getName)
                    .distinct()
                    .collect(Collectors.toList());

            log.debug("成功获取用户权限 - userId: {}, 权限数: {}", userId, permissionNames.size());
            return R.ok(permissionNames);

        } catch (Exception e) {
            log.error("获取用户权限异常 - userId: {}, 错误: {}", userId, e.getMessage(), e);
            return R.fail("获取用户权限失败");
        }
    }

    /**
     * 检查用户是否拥有指定权限
     * 用于API网关的权限校验
     */
    @Override
    @Transactional(readOnly = true)
    public R<Boolean> hasPermission(Long userId, String permission) {
        try {
            log.debug("检查用户权限 - userId: {}, permission: {}", userId, permission);
            
            if (StringUtils.isBlank(permission)) {
                return R.fail("权限标识符不能为空");
            }

            // 查询用户的所有权限
            List<Permission> permissions = permissionRepository.findAllByUserId(userId);
            boolean hasPermission = permissions.stream()
                    .anyMatch(p -> p.getName().equals(permission));

            log.debug("权限检查结果 - userId: {}, permission: {}, result: {}", 
                    userId, permission, hasPermission);
            return R.ok(hasPermission);

        } catch (Exception e) {
            log.error("检查用户权限异常 - userId: {}, permission: {}, 错误: {}", 
                    userId, permission, e.getMessage(), e);
            return R.fail("权限检查失败");
        }
    }

    /**
     * 批量检查用户是否拥有多个权限
     * 用于一次性校验多个权限，减少数据库查询次数
     */
    @Override
    @Transactional(readOnly = true)
    public R<Map<String, Boolean>> hasPermissions(Long userId, List<String> permissions) {
        try {
            log.debug("批量检查用户权限 - userId: {}, permissions数量: {}", userId, permissions.size());
            
            if (permissions == null || permissions.isEmpty()) {
                return R.ok(Collections.emptyMap());
            }

            // 查询用户的所有权限
            List<Permission> userPermissions = permissionRepository.findAllByUserId(userId);
            Set<String> userPermissionSet = userPermissions.stream()
                    .map(Permission::getName)
                    .collect(Collectors.toSet());

            // 批量检查
            Map<String, Boolean> resultMap = permissions.stream()
                    .distinct()
                    .collect(Collectors.toMap(
                            p -> p,
                            userPermissionSet::contains
                    ));

            log.debug("批量权限检查完成 - userId: {}, 检查数量: {}", userId, resultMap.size());
            return R.ok(resultMap);

        } catch (Exception e) {
            log.error("批量检查用户权限异常 - userId: {}, 错误: {}", userId, e.getMessage(), e);
            return R.fail("批量权限检查失败");
        }
    }

    /**
     * 检查用户是否拥有指定角色
     */
    @Override
    @Transactional(readOnly = true)
    public R<Boolean> hasRole(Long userId, String roleName) {
        try {
            log.debug("检查用户角色 - userId: {}, roleName: {}", userId, roleName);
            
            if (StringUtils.isBlank(roleName)) {
                return R.fail("角色名称不能为空");
            }

            Optional<User> optionalUser = userRepository.findByIdWithRolesAndPermissions(userId);
            if (optionalUser.isEmpty()) {
                return R.ok(false);
            }

            User user = optionalUser.get();
            boolean hasRole = user.getUserRoles().stream()
                    .anyMatch(ur -> ur.getRole().getName().equals(roleName));

            log.debug("角色检查结果 - userId: {}, roleName: {}, result: {}", 
                    userId, roleName, hasRole);
            return R.ok(hasRole);

        } catch (Exception e) {
            log.error("检查用户角色异常 - userId: {}, roleName: {}, 错误: {}", 
                    userId, roleName, e.getMessage(), e);
            return R.fail("角色检查失败");
        }
    }

    /**
     * 搜索用户（用于项目成员邀请等场景）
     * 根据用户ID、邮箱或姓名搜索
     */
    @Override
    @Transactional(readOnly = true)
    public R<Page<UserDTO>> searchUsers(String keyword, Pageable pageable) {
        try {
            log.debug("搜索用户 - 关键词: {}, 页码: {}, 每页数量: {}", 
                    keyword, pageable.getPageNumber(), pageable.getPageSize());
            
            if (StringUtils.isBlank(keyword)) {
                return R.fail("搜索关键词不能为空");
            }

            Page<User> userPage;
            
            // 尝试将关键词解析为用户ID（纯数字）
            try {
                Long userId = Long.parseLong(keyword.trim());
                // 如果是数字，先按ID精确查找
                Optional<User> userById = userRepository.findByIdAndIsDeletedFalse(userId);
                if (userById.isPresent()) {
                    // 找到了，返回单个用户的分页结果
                    List<User> users = List.of(userById.get());
                    userPage = new PageImpl<>(users, pageable, 1);
                    log.debug("按用户ID搜索成功 - ID: {}", userId);
                } else {
                    // ID没找到，按邮箱和姓名模糊搜索（可能是邮箱中的数字）
                    userPage = userRepository.findByNameContainingOrEmailContainingAndIsDeletedFalse(
                            keyword, keyword, pageable);
                }
            } catch (NumberFormatException e) {
                // 不是纯数字，按邮箱和姓名模糊搜索
                userPage = userRepository.findByNameContainingOrEmailContainingAndIsDeletedFalse(
                        keyword, keyword, pageable);
            }
            
            List<UserDTO> userDTOs = userMapper.toDTOList(userPage.getContent());
            Page<UserDTO> userDTOPage = new PageImpl<>(userDTOs, pageable, userPage.getTotalElements());

            log.debug("搜索用户成功 - 关键词: {}, 找到: {}个", keyword, userPage.getTotalElements());
            return R.ok(userDTOPage);

        } catch (Exception e) {
            log.error("搜索用户异常 - 关键词: {}, 错误: {}", keyword, e.getMessage(), e);
            return R.fail("搜索用户失败");
        }
    }
}
