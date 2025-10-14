package hbnu.project.zhiyanauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "hbnu.project.zhiyancommonbasic",
        "hbnu.project.zhiyancommonredis",
        "hbnu.project.zhiyancommonsecurity"
})
@EnableJpaAuditing
public class ZhiyanAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhiyanAuthApplication.class, args);
    }

}
