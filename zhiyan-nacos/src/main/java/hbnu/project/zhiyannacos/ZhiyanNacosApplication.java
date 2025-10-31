package hbnu.project.zhiyannacos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author ErgouTree
 */
@SpringBootApplication(
        scanBasePackages = {
            "hbnu.project.zhiyannacos",
            "hbnu.project.zhiyancommonbasic",
            "hbnu.project.zhiyancommonswagger",
        }, exclude = {
            DataSourceAutoConfiguration .class,              // 排除数据源自动配置
            RedisAutoConfiguration.class,                // 排除 Redis 自动配置
            RedisRepositoriesAutoConfiguration.class,    // 排除 Redis Repository 自动配置
        }
)
@EnableDiscoveryClient
@EnableScheduling
@ServletComponentScan
public class ZhiyanNacosApplication {

    public static void main(String[] args) {
        // 启动 Spring Boot 应用
        SpringApplication.run(ZhiyanNacosApplication.class, args);
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("666，开了是吧（指的是Nacos模块），是个人物");
        System.out.println("=".repeat(80));

        System.out.println("=".repeat(80));
        System.out.println("🎉 Nacos 模块启动完成！服务已就绪！");
        System.out.println("=".repeat(80) + "\n");
    }

}
