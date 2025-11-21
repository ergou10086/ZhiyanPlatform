package hbnu.project.zhiyanknowledge.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JMX 禁用配置
 * 禁用所有 HikariCP 数据源的 JMX MBean 注册，避免与 Spring JMX 导出器冲突
 *
 * @author ErgouTree
 */
@Configuration
public class JmxDisableConfig {

    /**
     * Bean 后处理器，自动禁用所有 HikariDataSource 的 JMX 注册
     */
    @Bean
    public BeanPostProcessor hikariJmxDisableProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof HikariDataSource) {
                    ((HikariDataSource) bean).setRegisterMbeans(false);
                }
                return bean;
            }
        };
    }
}

