package hbnu.project.zhiyanmessage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "hbnu.project.zhiyanmessage",
        "hbnu.project.zhiyancommonsse",
})
public class ZhiyanMessageApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhiyanMessageApplication.class, args);
    }

}
