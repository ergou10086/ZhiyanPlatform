package hbnu.project.zhiyancommonsecurity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "hbnu.project.zhiyancommonsecurity",
        "hbnu.project.zhiyancommonbasic",
        "hbnu.project.zhiyanauth"
})
public class ZhiyanCommonSecurityApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhiyanCommonSecurityApplication.class, args);
    }

}
