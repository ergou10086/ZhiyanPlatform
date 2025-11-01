package hbnu.project.zhiyanskywalking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * SkyWalking 监控管理平台启动类
 *
 * @author ErgouTree
 */
@Slf4j
@SpringBootApplication(
        scanBasePackages = {
                "hbnu.project.zhiyanskywalking",
                "hbnu.project.zhiyancommonbasic",
                "hbnu.project.zhiyancommonswagger"
        },
        exclude = {
                org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration.class,
                org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
                RedisAutoConfiguration.class,                // 排除 Redis 自动配置
                RedisRepositoriesAutoConfiguration.class,    // 排除 Redis Repository 自动配置
        }
)
public class ZhiyanSkywalkingApplication {

    public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext application = SpringApplication.run(ZhiyanSkywalkingApplication.class, args);
        Environment env = application.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port");
        String path = env.getProperty("server.servlet.context-path", "");

        log.info("\n----------------------------------------------------------\n\t" +
                "Application '{}' is running! Access URLs:\n\t" +
                "Local: \t\thttp://localhost:{}{}\n\t" +
                "External: \thttp://{}:{}{}\n\t" +
                "Swagger-UI: \thttp://{}:{}{}/doc.html\n\t" +
                "Profile(s): \t{}\n----------------------------------------------------------",
                env.getProperty("spring.application.name"),
                port,
                path,
                ip,
                port,
                path,
                ip,
                port,
                path,
                env.getActiveProfiles());

        System.out.println("666，SkyWalking模块启动，这个人开了");
    }

}
