package hbnu.project.zhiyangateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author ErgouTree
 */
@SpringBootApplication
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
