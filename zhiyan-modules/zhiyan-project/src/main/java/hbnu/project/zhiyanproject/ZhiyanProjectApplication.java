package hbnu.project.zhiyanproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
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
        "hbnu.project.zhiyancommonsecurity",
        "hbnu.project.zhiyancommonoss",
        "hbnu.project.zhiyancommonswagger",
        "hbnu.project.common.log",  // 日志模块
        "hbnu.project.zhiyanactivelog",  // 操作日志模块
})
@EnableJpaRepositories(basePackages = {
        "hbnu.project.zhiyanproject.repository",
        "hbnu.project.zhiyanactivelog.repository"  // 扫描活动日志模块的Repository
})
@EntityScan(basePackages = {
        "hbnu.project.zhiyanproject.model.entity",
        "hbnu.project.zhiyanactivelog.model.entity"  // 扫描活动日志模块的Entity
})
@EnableJpaAuditing
@EnableFeignClients(basePackages = "hbnu.project.zhiyanproject.client")
@EnableDiscoveryClient
public class ZhiyanProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhiyanProjectApplication.class, args);
        System.out.println("✅ 智研平台 - 项目管理模块启动成功！");
    }

}
