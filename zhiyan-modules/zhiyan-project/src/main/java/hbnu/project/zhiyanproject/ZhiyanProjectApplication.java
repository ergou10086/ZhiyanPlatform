package hbnu.project.zhiyanproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 智研平台 - 项目管理模块启动类
 *
 * @author ErgouTree
 */
@SpringBootApplication(scanBasePackages = {
        "hbnu.project.zhiyanproject",
        "hbnu.project.zhiyancommonbasic",
        "hbnu.project.zhiyancommonsecurity"
})
@EnableJpaRepositories(basePackages = "hbnu.project.zhiyanproject.repository")
@EntityScan(basePackages = "hbnu.project.zhiyanproject.model.entity")
@EnableJpaAuditing
public class ZhiyanProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhiyanProjectApplication.class, args);
        System.out.println("✅ 智研平台 - 项目管理模块启动成功！");
    }

}
