package hbnu.project.zhiyanknowledge.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Knowledge模块的UserDetailsService实现
 * 
 * 注意：Knowledge模块不负责用户认证（认证由auth模块处理），
 * 此服务主要用于满足Spring Security配置的依赖要求。
 * 实际的用户信息验证通过JWT过滤器完成。
 * 
 * @author ErgouTree
 */
@Slf4j
@Service
public class KnowledgeUserDetailsService implements UserDetailsService {

    /**
     * 此方法在Knowledge模块中不会被实际调用（因为使用JWT认证）
     * 仅用于满足Spring Security的配置要求
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.warn("Knowledge模块不应该直接加载用户信息，请检查JWT配置是否正确");
        throw new UsernameNotFoundException(
            "Knowledge模块不支持用户名密码登录，请使用JWT Token认证。用户认证请访问Auth模块。"
        );
    }
}
