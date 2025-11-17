package hbnu.project.zhiyanauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 认证主类
 *
 * @author ErgouTree
 */
@SpringBootApplication(scanBasePackages = {
        "hbnu.project.zhiyanauth",
        "hbnu.project.zhiyancommonbasic",
        "hbnu.project.zhiyancommonredis",
        "hbnu.project.zhiyancommonsecurity",
        "hbnu.project.zhiyancommonoss",
        "hbnu.project.zhiyanactivelog",
        "hbnu.project.zhiyancommonoauth",
})
@EnableJpaRepositories(basePackages = {
        "hbnu.project.zhiyanauth.repository",
        "hbnu.project.zhiyanactivelog.repository"  // 扫描活动日志模块的Repository
})
@EntityScan(basePackages = {
        "hbnu.project.zhiyanauth.model.entity",
        "hbnu.project.zhiyanactivelog.model.entity"  // 扫描活动日志模块的Entity
})
@EnableJpaAuditing
@EnableDiscoveryClient
@EnableFeignClients
public class ZhiyanAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZhiyanAuthApplication.class, args);
        System.out.println("666，开了是吧（指的是用户认证模块）");
    }
}
