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
        // 启动 Spring Boot 应用
        SpringApplication.run(ZhiyanNacosApplication.class, args);
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("666，开了是吧（指的是Nacos模块），是个人物");
        System.out.println("=".repeat(80));
        
        System.out.println("=".repeat(80));
        System.out.println("🎉 Nacos 模块启动完成！服务已就绪！");
        System.out.println("=".repeat(80) + "\n");
    }

}
