package hbnu.project.zhiyannacos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author ErgouTree
 */
@SpringBootApplication
@EnableDiscoveryClient
@ServletComponentScan
public class ZhiyanNacosApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhiyanNacosApplication.class, args);
        System.out.println("666，开了是吧（指的是Nacos模块），是个人物");
    }

}
