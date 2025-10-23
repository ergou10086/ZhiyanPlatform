package hbnu.project.zhiyanwiki;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {
        "hbnu.project.zhiyancommonbasic"
})
@EnableDiscoveryClient
public class ZhiyanWikiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhiyanWikiApplication.class, args);
    }

}
