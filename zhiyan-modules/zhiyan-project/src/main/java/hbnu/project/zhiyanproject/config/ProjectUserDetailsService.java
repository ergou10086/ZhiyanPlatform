package hbnu.project.zhiyanproject.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 项目服务的 UserDetailsService 实现
 * 
 * 注意：这个服务不是用来做登录认证的（登录由 auth 服务处理）
 * 这里提供一个简单的实现只是为了满足 Spring Security 配置的依赖
 * 实际的用户认证和权限验证通过 JWT Token 在 JwtAuthenticationFilter 中完成
 * 
 * @author Tokito
 */
@Slf4j
@Service
public class ProjectUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("ProjectUserDetailsService.loadUserByUsername 被调用: username={}", username);
        log.debug("注意：项目服务不应该直接调用此方法，用户认证通过 JWT Token 完成");
        
        // 返回一个默认用户，实际不会被使用
        // 真正的用户信息从 JWT Token 中解析
        return User.builder()
                .username(username)
                .password("") // 密码为空，因为不会用到
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
    }
}

