package hbnu.project.zhiyanauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "hbnu.project.zhiyanauth",
        "hbnu.project.zhiyancommonbasic",
        "hbnu.project.zhiyancommonredis",
        "hbnu.project.zhiyancommonsecurity"
})
@EnableJpaRepositories(basePackages = "hbnu.project.zhiyanauth.repository")
@EntityScan(basePackages = "hbnu.project.zhiyanauth.model.entity")
@EnableJpaAuditing
public class ZhiyanAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZhiyanAuthApplication.class, args);
        System.out.println("666，开了是吧（指的是用户认证模块）");
    }
}
