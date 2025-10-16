package hbnu.project.zhiyancommonredis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

/**
 * 禁用数据源
 * @author ErgouTree
 */
@SpringBootApplication
public class ZhiyanCommonRedisApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZhiyanCommonRedisApplication.class, args);
        System.out.println("666，开了是吧（指的是Redis模块），是个人物");
    }

}
