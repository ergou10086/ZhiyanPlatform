package hbnu.project.zhiyanproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ZhiyanProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhiyanProjectApplication.class, args);
    }

}
