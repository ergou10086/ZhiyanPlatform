package hbnu.project.zhiyanknowledge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 智研平台 - 知识管理模块启动类
 *
 * @author ErgouTree
 */
@SpringBootApplication(scanBasePackages = {
        "hbnu.project.zhiyanknowledge",
        "hbnu.project.zhiyancommonbasic",
        "hbnu.project.zhiyancommonoss"
})
@EnableJpaRepositories(basePackages = "hbnu.project.zhiyanknowledge.repository")
@EntityScan(basePackages = "hbnu.project.zhiyanknowledge.model.entity")
@EnableJpaAuditing
public class ZhiyanKnowledgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhiyanKnowledgeApplication.class, args);
        System.out.println("✅ 智研平台 - 知识管理模块启动成功！");
    }

}
