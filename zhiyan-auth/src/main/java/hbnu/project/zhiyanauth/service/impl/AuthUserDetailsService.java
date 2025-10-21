package hbnu.project.zhiyanauth.service.impl;

import hbnu.project.zhiyancommonsecurity.service.UserDetailsService;
import hbnu.project.zhiyanauth.model.entity.Permission;
import hbnu.project.zhiyanauth.model.entity.User;
import hbnu.project.zhiyanauth.repository.PermissionRepository;
import hbnu.project.zhiyanauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 认证模块用户详情服务实现
 *
 * @author yxy
 */
@Slf4j
@Service
@RequiredArgsConstructor
//@ConditionalOnBean({UserRepository.class, PermissionRepository.class})
public class AuthUserDetailsService extends UserDetailsService {

    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("开始加载用户信息: {}", email);

        // 1. 查找用户基本信息
        Optional<User> optionalUser = userRepository.findByEmailAndIsDeletedFalse(email);
        if (optionalUser.isEmpty()) {
            log.warn("用户不存在: {}", email);
            throw new UsernameNotFoundException("用户不存在: " + email);
        }

        User user = optionalUser.get();

        // 2. 从数据库加载用户角色和权限
        Set<String> permissions = loadUserPermissionsFromDatabase(user.getId());
        List<String> roles = loadUserRolesFromDatabase(user.getId());

        log.debug("用户[{}]加载完成 - 角色数: {}, 权限数: {}", 
                email, roles.size(), permissions.size());

        // 3. 使用父类方法构建LoginUserBody对象
        return buildLoginUserBody(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getAvatarUrl(),
                user.getTitle(),
                user.getInstitution(),
                roles,
                permissions,
                user.getIsLocked(),
                user.getPasswordHash()
        );
    }

    /**
     * 根据用户ID加载用户详情（业务特定方法）
     */
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            log.warn("用户不存在: {}", userId);
            throw new UsernameNotFoundException("用户不存在: " + userId);
        }

        User user = optionalUser.get();
        
        // 从数据库加载角色和权限
        Set<String> permissions = loadUserPermissionsFromDatabase(userId);
        List<String> roles = loadUserRolesFromDatabase(userId);

        return buildLoginUserBody(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getAvatarUrl(),
                user.getTitle(),
                user.getInstitution(),
                roles,
                permissions,
                user.getIsLocked(),
                user.getPasswordHash()
        );
    }

    /**
     * 从数据库加载用户权限
     * 通过用户的角色关联查询所有权限
     */
    private Set<String> loadUserPermissionsFromDatabase(Long userId) {
        try {
            List<Permission> permissions = permissionRepository.findAllByUserId(userId);
            Set<String> permissionNames = permissions.stream()
                    .map(Permission::getName)
                    .collect(Collectors.toSet());
            
            log.debug("用户[ID: {}]权限加载完成，共{}个权限", userId, permissionNames.size());
            return permissionNames;
            
        } catch (Exception e) {
            log.error("加载用户权限异常 - userId: {}, 错误: {}", userId, e.getMessage(), e);
            return Collections.emptySet();
        }
    }

    /**
     * 从数据库加载用户角色
     * 查询用户的所有角色关联
     */
    private List<String> loadUserRolesFromDatabase(Long userId) {
        try {
            Optional<User> optionalUser = userRepository.findByIdWithRolesAndPermissions(userId);
            if (optionalUser.isEmpty()) {
                log.warn("用户不存在 - userId: {}", userId);
                return Collections.emptyList();
            }

            User user = optionalUser.get();
            List<String> roles = user.getUserRoles().stream()
                    .map(ur -> ur.getRole().getName())
                    .distinct()
                    .collect(Collectors.toList());

            log.debug("用户[ID: {}]角色加载完成: {}", userId, roles);
            return roles;
            
        } catch (Exception e) {
            log.error("加载用户角色异常 - userId: {}, 错误: {}", userId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取用户权限（业务特定方法）
     */
    public Set<String> getUserPermissions(Long userId) {
        return loadUserPermissionsFromDatabase(userId);
    }

    /**
     * 获取用户角色（业务特定方法）
     */
    public List<String> getUserRoles(Long userId) {
        return loadUserRolesFromDatabase(userId);
    }
}