package hbnu.project.zhiyancommonsecurity.service;


import hbnu.project.zhiyancommonsecurity.context.LoginUserBody;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用户详情服务抽象基类
 * 提供通用的权限构建逻辑
 *
 * author: yxy
 */
public abstract class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    /**
     * 构建Spring Security权限集合
     */
    protected Collection<GrantedAuthority> buildAuthorities(List<String> roles, Set<String> permissions) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // 添加角色权限（ROLE_前缀）
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }

        // 添加具体权限
        for (String permission : permissions) {
            authorities.add(new SimpleGrantedAuthority(permission));
        }

        return authorities;
    }

    /**
     * 构建LoginUserBody对象
     */
    protected LoginUserBody buildLoginUserBody(Long userId, String email, String name,
                                               String avatarUrl, String title, String institution,
                                               List<String> roles, Set<String> permissions,
                                               Boolean isLocked, String passwordHash) {
        Collection<GrantedAuthority> authorities = buildAuthorities(roles, permissions);

        return LoginUserBody.builder()
                .userId(userId)
                .email(email)
                .name(name)
                .avatarUrl(avatarUrl)
                .title(title)
                .institution(institution)
                .roles(roles)
                .permissions(permissions)
                .isLocked(isLocked)
                .passwordHash(passwordHash)
                .authorities(authorities)
                .build();
    }
}