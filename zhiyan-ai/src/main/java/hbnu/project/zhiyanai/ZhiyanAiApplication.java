package hbnu.project.zhiyanai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Zhiyan AI 应用启动类
 * AI 模块特点：
 * 1. 不需要数据库，排除数据源自动配置
 * 2. 不需要完整的 Security 认证（由网关/Auth 服务完成），排除所有 Security 自动配置
 * 3. 仅需要 SecurityUtils 工具类来解析 Token 获取用户信息
 * 4. 使用 Redis 缓存对话历史
 *
 * @author ErgouTree
 */
@SpringBootApplication(
        scanBasePackages = {
                "hbnu.project.zhiyanai",
                "hbnu.project.zhiyancommonsse",
                "hbnu.project.zhiyancommonbasic",
                "hbnu.project.zhiyancommonredis"  // Redis 模块（用于缓存对话）
        },
        exclude = {
                DataSourceAutoConfiguration.class,              // 排除数据源自动配置
                SecurityAutoConfiguration.class,                // 排除 Spring Security 自动配置
                UserDetailsServiceAutoConfiguration.class,      // 排除 UserDetailsService 自动配置
                ManagementWebSecurityAutoConfiguration.class    // 排除 Actuator Security 自动配置
        }
)
@EnableConfigurationProperties
@EnableFeignClients
@EnableDiscoveryClient
public class ZhiyanAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhiyanAiApplication.class, args);
        System.out.println("========================================");
        System.out.println("666,这个人开了（指AI模块）");
        System.out.println("========================================");
    }

}
