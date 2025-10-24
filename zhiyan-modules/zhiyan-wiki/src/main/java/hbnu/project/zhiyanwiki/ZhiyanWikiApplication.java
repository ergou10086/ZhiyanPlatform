package hbnu.project.zhiyanwiki;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = {
        "hbnu.project.zhiyanwiki",
        "hbnu.project.zhiyancommonbasic",
        "hbnu.project.zhiyancommonsecurity",
        "hbnu.project.zhiyancommonoss",
        "hbnu.project.zhiyancommonswagger",
})
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableFeignClients
public class ZhiyanWikiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhiyanWikiApplication.class, args);
    }

}
