package hbnu.project.zhiyanauth.service.impl;

import hbnu.project.zhiyancommonsecurity.service.UserDetailsService;
import hbnu.project.zhiyanauth.model.entity.User;
import hbnu.project.zhiyanauth.model.enums.PermissionModule;
import hbnu.project.zhiyanauth.model.enums.SysRole;
import hbnu.project.zhiyanauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 认证模块用户详情服务实现
 *
 * @author yxy
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthUserDetailsService extends UserDetailsService {

    private final UserRepository userRepository;

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

        // 2. 检查用户状态
        if (Boolean.TRUE.equals(user.getIsLocked())) {
            log.warn("用户账户已被锁定: {}", email);
            // 注意：这里不抛出异常，让Spring Security处理锁定状态
        }

        // 3. 加载用户权限和角色
        Set<String> permissions = loadUserPermissions(user);
        List<String> roles = loadUserRoles(user);

        // 4. 使用父类方法构建LoginUserBody对象
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
        Set<String> permissions = loadUserPermissions(user);
        List<String> roles = loadUserRoles(user);

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
     * 加载用户权限
     */
    private Set<String> loadUserPermissions(User user) {
        Set<String> permissions = new HashSet<>();

        // 1. 根据用户注册状态分配基础权限
        permissions.addAll(PermissionModule.BASIC_USER.getPermissionStrings());

        // 2. 检查是否为开发者
        if (isDeveloper(user)) {
            permissions.addAll(PermissionModule.SYSTEM_ADMIN.getPermissionStrings());
            log.debug("用户[{}]具有开发者权限", user.getEmail());
        }

        // 3. 项目相关权限需要在用户加入项目时动态加载
        // 这里可以预留接口，后续在项目服务中实现
        permissions.addAll(loadProjectPermissions(user.getId()));

        log.debug("用户[{}]权限加载完成，共{}个权限", user.getEmail(), permissions.size());
        return permissions;
    }

    /**
     * 加载用户角色
     */
    private List<String> loadUserRoles(User user) {
        List<String> roles = new ArrayList<>();

        // 1. 检查是否为开发者
        if (isDeveloper(user)) {
            roles.add(SysRole.DEVELOPER.getCode());
        } else {
            // 2. 所有注册用户默认为普通用户
            roles.add(SysRole.USER.getCode());
        }

        log.debug("用户[{}]角色加载完成: {}", user.getEmail(), roles);
        return roles;
    }

    /**
     * 判断用户是否为开发者
     */
    private boolean isDeveloper(User user) {
        // 临时实现：假设有特定邮箱的用户为开发者
        String[] developerEmails = {"admin@zhiyan.com", "developer@zhiyan.com"};
        return Arrays.asList(developerEmails).contains(user.getEmail());

        // 未来可以改为从数据库或配置中读取
        // return user.getIsDeveloper() != null && user.getIsDeveloper();
    }

    /**
     * 加载项目相关权限（预留接口）
     */
    private Set<String> loadProjectPermissions(Long userId) {
        Set<String> projectPermissions = new HashSet<>();

        // TODO: 实现项目权限加载逻辑
        // 1. 查询用户参与的所有项目
        // 2. 根据用户在各项目中的角色添加相应权限

        return projectPermissions;
    }

    /**
     * 获取用户权限（业务特定方法）
     */
    public Set<String> getUserPermissions(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        return user.map(this::loadUserPermissions).orElse(Collections.emptySet());
    }

    /**
     * 获取用户角色（业务特定方法）
     */
    public List<String> getUserRoles(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        return user.map(this::loadUserRoles).orElse(Collections.emptyList());
    }
}