package hbnu.project.zhiyanmessgae;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = {
        "hbnu.project.zhiyanmessgae",
        "hbnu.project.zhiyancommonbasic",
        "hbnu.project.zhiyancommonsecurity",
})
@EnableDiscoveryClient
//@EnableJpaAuditing
public class ZhiyanMessgaeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhiyanMessgaeApplication.class, args);
        System.out.println("666，开了？我说的是服务");
    }

}

