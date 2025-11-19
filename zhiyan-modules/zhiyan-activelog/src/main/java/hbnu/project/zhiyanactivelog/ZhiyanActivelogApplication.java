package hbnu.project.zhiyanactivelog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 活动日志主类
 *
 * @author ErgouTree
 */
@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration.class,
})
public class ZhiyanActivelogApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhiyanActivelogApplication.class, args);
        System.out.println("666，开了？？我指的是服务");
    }

}
