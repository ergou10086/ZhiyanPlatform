package hbnu.project.zhiyancommonsecurity.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 登录用户信息上下文对象
 * 用于在应用程序中传递当前登录用户的详细信息
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserBody implements UserDetails, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户邮箱（登录账号）
     */
    private String email;

    /**
     * 用户姓名
     */
    private String name;

    /**
     * 用户头像URL
     */
    private String avatarUrl;

    /**
     * 用户职称/职位
     */
    private String title;

    /**
     * 所属机构
     */
    private String institution;

    /**
     * 用户角色列表
     */
    private List<String> roles;

    /**
     * 用户权限列表
     */
    private Set<String> permissions;

    /**
     * 是否锁定
     */
    private Boolean isLocked;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

    /**
     * 登录IP
     */
    private String loginIp;

    /**
     * 浏览器信息
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * Token过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 密码哈希值（用于Spring Security认证）
     */
    private String passwordHash;

    /**
     * Spring Security权限集合
     */
    private Collection<? extends GrantedAuthority> authorities;

    /**
     * 判断用户是否拥有指定权限
     *
     * @param permission 权限标识
     * @return 是否拥有权限
     */
    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }

    /**
     * 判断用户是否拥有指定角色
     *
     * @param role 角色名称
     * @return 是否拥有角色
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    /**
     * 判断用户是否拥有任意一个指定权限
     *
     * @param permissions 权限列表
     * @return 是否拥有任意一个权限
     */
    public boolean hasAnyPermission(String... permissions) {
        if (this.permissions == null || permissions == null) {
            return false;
        }
        for (String permission : permissions) {
            if (this.permissions.contains(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断用户是否拥有任意一个指定角色
     *
     * @param roles 角色列表
     * @return 是否拥有任意一个角色
     */
    public boolean hasAnyRole(String... roles) {
        if (this.roles == null || roles == null) {
            return false;
        }
        for (String role : roles) {
            if (this.roles.contains(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断用户是否为管理员
     * 可以根据角色或其他业务逻辑判断
     *
     * @return 是否为管理员
     */
    public boolean isAdmin() {
        return hasRole("系统管理员") || hasRole("超级管理员") || hasRole("ADMIN");
    }

    /**
     * 判断账号是否可用（未锁定）
     *
     * @return 账号是否可用
     */
    public boolean isAccountNonLocked() {
        return isLocked == null || !isLocked;
    }

    // ==================== UserDetails接口实现 ====================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (authorities != null) {
            return authorities;
        }
        
        // 从角色和权限构建权限集合
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        
        // 添加角色权限（以ROLE_开头）
        if (roles != null) {
            grantedAuthorities.addAll(roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toSet()));
        }
        
        // 添加具体权限
        if (permissions != null) {
            grantedAuthorities.addAll(permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet()));
        }
        
        return grantedAuthorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }


    @Override
    public String getUsername() {
        // 使用邮箱作为用户名
        return email;
    }


    @Override
    public boolean isAccountNonExpired() {
        // 账户永不过期，可根据业务需求调整
        return true;
    }


    @Override
    public boolean isCredentialsNonExpired() {
        // 凭证永不过期，可根据业务需求调整
        return true;
    }


    @Override
    public boolean isEnabled() {
        // 账户未锁定则启用
        return !Boolean.TRUE.equals(isLocked);
    }
}

