package hbnu.project.zhiyansentineldashboard;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Sentinel Dashboard 流控监控管理平台启动类
 *
 * @author ErgouTree
 */
@Slf4j
@SpringBootApplication(
        scanBasePackages = {
                "hbnu.project.zhiyansentineldashboard",
                "hbnu.project.zhiyancommonbasic",
                "hbnu.project.zhiyancommonswagger"
        },
        exclude = {
                org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration.class,
                org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
                RedisAutoConfiguration.class,
                RedisRepositoriesAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
        }
)
@EnableScheduling
public class ZhiyanSentinelDashboardApplication {

    public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext application = SpringApplication.run(ZhiyanSentinelDashboardApplication.class, args);
        Environment env = application.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port");
        String path = env.getProperty("server.servlet.context-path", "");

        log.info("""
                        ----------------------------------------------------------
                        \t\
                        Application '{}' is running! Access URLs:
                        \t\
                        Local: \t\thttp://localhost:{}{}
                        \t\
                        External: \thttp://{}:{}{}
                        \t\
                        Swagger-UI: \thttp://{}:{}{}/doc.html
                        \t\
                        Profile(s): \t{}
                        ----------------------------------------------------------""",
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
    }

}
