package hbnu.project.zhiyanaicoze;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Zhiyan AI Coze 应用启动类
 *
 * @author ErgouTree
 */
@SpringBootApplication(
        scanBasePackages = {
                "hbnu.project.zhiyanaicoze",
                "hbnu.project.zhiyancommonbasic"
        },
        exclude = {
                DataSourceAutoConfiguration.class,           // 排除数据源自动配置（Coze 模块不需要数据库）
                RedisAutoConfiguration.class,                // 排除 Redis 自动配置
                RedisRepositoriesAutoConfiguration.class,    // 排除 Redis Repository 自动配置
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
