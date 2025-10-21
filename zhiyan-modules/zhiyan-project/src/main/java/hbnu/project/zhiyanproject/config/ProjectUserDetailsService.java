package hbnu.project.zhiyanproject.config;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 项目模块的 UserDetailsService 实现
 * 
 * 注意：
 * - 项目模块不负责用户登录认证
 * - 用户信息通过 JWT Token 在 JwtAuthenticationFilter 中解析
 * - 此实现仅用于满足 Spring Security 配置的依赖要求
 * 
 * 使用场景：
 * - SecurityConfig 需要 UserDetailsService bean 来构建认证提供者
 * - 但项目模块只处理已认证的请求（JWT 验证）
 * - 不提供用户名密码登录功能（由 zhiyan-auth 模块负责）
 * 
 * @author ErgouTree
 */
@Service
public class ProjectUserDetailsService implements UserDetailsService {

    /**
     * 加载用户详情
     * 
     * 注意：此方法在项目模块中不应被调用
     * 所有用户信息都通过 JWT Token 在 JwtAuthenticationFilter 中提取
     * 
     * @param username 用户名
     * @return UserDetails 用户详情
     * @throws UsernameNotFoundException 总是抛出此异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 项目模块不支持用户名密码登录
        // 所有请求都应该携带 JWT Token
        throw new UsernameNotFoundException(
            "项目模块不支持用户名密码登录，请使用 JWT Token 访问。" +
            "用户认证请访问 zhiyan-auth 模块。"
        );
    }
}

