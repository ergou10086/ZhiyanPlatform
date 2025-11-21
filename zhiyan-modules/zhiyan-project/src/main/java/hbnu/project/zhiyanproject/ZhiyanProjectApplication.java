package hbnu.project.zhiyanproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

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
        "hbnu.project.zhiyancommonlog",  // 日志模块
        "hbnu.project.zhiyanactivelog",  // 操作日志模块
        "hbnu.project.zhiyanmessage",  // 消息模块（包含controller、service等）
})
@EntityScan(basePackages = {
        "hbnu.project.zhiyanproject.model.entity",
        "hbnu.project.zhiyanproject.model.dto",
        "hbnu.project.zhiyanmessage.model.entity",  // 消息模块的实体
        //"hbnu.project.zhiyanactivelog.model.entity",  // 扫描活动日志模块的Entity
        // activelog 使用独立数据源，不在此处扫描
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
