package hbnu.project.zhiyanwiki;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = {
        "hbnu.project.zhiyanwiki",
        "hbnu.project.zhiyancommonbasic",
        "hbnu.project.zhiyancommonsecurity",
        "hbnu.project.zhiyancommonoss",
        "hbnu.project.zhiyancommonswagger",
        "hbnu.project.zhiyanactivelog",  // 操作日志模块
})
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableFeignClients
// 明确指定 JPA 和 MongoDB repositories 的扫描路径
@EnableJpaRepositories(basePackages = {
        "hbnu.project.zhiyanwiki.repository",
        "hbnu.project.zhiyanactivelog.repository"  // 扫描活动日志模块的Repository
})
@EnableMongoRepositories(basePackages = "hbnu.project.zhiyanwiki.repository")  // ✅ 添加MongoDB支持
@EntityScan(basePackages = {
        "hbnu.project.zhiyanwiki.model.entity",
        "hbnu.project.zhiyanactivelog.model.entity"  // 扫描活动日志模块的Entity
})
public class ZhiyanWikiApplication {

    public static void main(String[] args) {

        SpringApplication.run(ZhiyanWikiApplication.class, args);
        System.out.println("开了？我说的是Wiki模块");
    }

}
