package hbnu.project.zhiyanauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "hbnu.project.zhiyanauth",
        "hbnu.project.zhiyancommonbasic",
        "hbnu.project.zhiyancommonredis"
})
public class ZhiyanAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhiyanAuthApplication.class, args);
    }

}
