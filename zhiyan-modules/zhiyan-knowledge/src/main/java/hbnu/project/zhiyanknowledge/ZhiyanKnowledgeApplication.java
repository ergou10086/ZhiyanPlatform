package hbnu.project.zhiyanknowledge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
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
        "hbnu.project.zhiyancommonoss",
        "hbnu.project.zhiyancommonsecurity"
}, exclude = {
        org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration.class
})
@EnableJpaRepositories(basePackages = "hbnu.project.zhiyanknowledge.repository")
@EntityScan(basePackages = "hbnu.project.zhiyanknowledge.model.entity")
@EnableJpaAuditing
@EnableDiscoveryClient
@EnableFeignClients
public class ZhiyanKnowledgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhiyanKnowledgeApplication.class, args);
        System.out.println("✅ 智研平台 - 成果管理模块启动成功！");
    }

}
