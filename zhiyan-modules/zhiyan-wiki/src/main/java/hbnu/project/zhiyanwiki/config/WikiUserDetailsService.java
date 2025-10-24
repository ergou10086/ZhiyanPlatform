package hbnu.project.zhiyanwiki.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Wiki模块的UserDetailsService实现
 * 
 * 注意：Wiki模块不负责用户认证（认证由auth模块处理），
 * 此服务主要用于满足Spring Security配置的依赖要求。
 * 实际的用户信息验证通过JWT过滤器完成。
 * 
 * @author ErgouTree
 */
@Slf4j
@Service
public class WikiUserDetailsService implements UserDetailsService {

    /**
     * 此方法在Wiki模块中不会被实际调用（因为使用JWT认证）
     * 仅用于满足Spring Security的配置要求
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.warn("Wiki模块不应该直接加载用户信息，请检查JWT配置是否正确");
        throw new UsernameNotFoundException(
            "Wiki模块不支持用户名密码登录，请使用JWT Token认证。用户认证请访问Auth模块。"
        );
    }
}

