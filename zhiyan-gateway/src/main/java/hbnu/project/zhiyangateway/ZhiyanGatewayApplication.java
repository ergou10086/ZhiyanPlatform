package hbnu.project.zhiyangateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author ErgouTree
 */
@SpringBootApplication(
        exclude = {
                org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration.class
        },
        excludeName = {
                "org.redisson.spring.starter.RedissonAutoConfiguration",
                "org.redisson.spring.starter.RedissonAutoConfigurationV2"
        }
)
@ComponentScan(basePackages = {
        "hbnu.project.zhiyangateway",
        "hbnu.project.zhiyancommonbasic"
})
public class ZhiyanGatewayApplication {
    public static void main(String[] args) {

        SpringApplication.run(ZhiyanGatewayApplication.class, args);
        System.out.println("666，开了是吧（指的是网关模块）");
    }

}
