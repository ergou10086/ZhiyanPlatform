package hbnu.project.zhiyanaicoze;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.reactive.ReactiveManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Zhiyan AI Coze 应用启动类
 * 
 * AI 模块特点：
 * 1. 不需要数据库，排除数据源自动配置
 * 2. 不需要完整的 Security 认证（由网关/Auth 服务完成），排除所有 Security 自动配置
 * 3. 仅需要 SecurityHelper 工具类来解析 Token 获取用户信息
 * 4. 使用 WebFlux 响应式编程模型
 *
 * @author ErgouTree
 */
@SpringBootApplication(
        scanBasePackages = {
                "hbnu.project.zhiyanaicoze",
                "hbnu.project.zhiyancommonbasic"
        },
        exclude = {
                DataSourceAutoConfiguration.class,                              // 排除数据源自动配置（Coze 模块不需要数据库）
                RedisAutoConfiguration.class,                                    // 排除 Redis 自动配置
                RedisRepositoriesAutoConfiguration.class,                        // 排除 Redis Repository 自动配置
                ReactiveSecurityAutoConfiguration.class,                        // 排除 Spring Security WebFlux 自动配置
                ReactiveUserDetailsServiceAutoConfiguration.class,              // 排除 UserDetailsService WebFlux 自动配置
                ReactiveManagementWebSecurityAutoConfiguration.class            // 排除 Actuator Security WebFlux 自动配置
        }
)
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "hbnu.project.zhiyanaicoze.client")
@EnableConfigurationProperties
public class ZhiyanAiCozeApplication {

    public static void main(String[] args) {

        SpringApplication.run(ZhiyanAiCozeApplication.class, args);
        System.out.println("zhiyan coze模块启动成功，666，这个人开了");
    }

}
