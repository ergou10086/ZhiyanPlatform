package hbnu.project.zhiyanai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author ErgouTree
 */
@SpringBootApplication(scanBasePackages = {
        "hbnu.project.zhiyanai",
})
@EnableConfigurationProperties
@EnableFeignClients
@EnableDiscoveryClient
public class ZhiyanAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhiyanAiApplication.class, args);
    }

}
