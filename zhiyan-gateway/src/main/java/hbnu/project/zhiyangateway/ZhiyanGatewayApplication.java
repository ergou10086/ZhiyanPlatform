package hbnu.project.zhiyangateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "hbnu.project.zhiyangateway",
        "hbnu.project.zhiyancommonbasic",
        "hbnu.project.zhiyancommonredis",
        "hbnu.project.zhiyancommonsecurity"
})
public class ZhiyanGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhiyanGatewayApplication.class, args);
    }

}
