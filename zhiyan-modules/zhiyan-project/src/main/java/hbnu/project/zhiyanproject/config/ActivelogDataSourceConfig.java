package hbnu.project.zhiyanproject.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Activelog 模块数据源配置
 * 为操作日志模块配置独立的数据源，避免操作日志表被创建到业务数据库
 *
 * @author ErgouTree
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "hbnu.project.zhiyanactivelog.repository",  // activelog 的 repository 包
        entityManagerFactoryRef = "activelogEntityManagerFactory",
        transactionManagerRef = "activelogTransactionManager"
)
public class ActivelogDataSourceConfig {

    /**
     * Activelog 数据源属性配置
     */
    @Bean
    @ConfigurationProperties("spring.datasource.activelog")
    public DataSourceProperties activelogDataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * Activelog 数据源
     */
    @Bean
    @ConfigurationProperties("spring.datasource.activelog.hikari")
    public DataSource activelogDataSource(@Qualifier("activelogDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    /**
     * Activelog EntityManagerFactory
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean activelogEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("activelogDataSource") DataSource dataSource) {
        
        // JPA 属性配置
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        properties.put("hibernate.hbm2ddl.auto", "update");  // activelog 使用 update 策略
        properties.put("hibernate.show_sql", false);
        
        return builder
                .dataSource(dataSource)
                .packages("hbnu.project.zhiyanactivelog.model.entity")  // activelog 的 entity 包
                .persistenceUnit("activelog")
                .properties(properties)
                .build();
    }

    /**
     * Activelog 事务管理器
     */
    @Bean
    public PlatformTransactionManager activelogTransactionManager(
            @Qualifier("activelogEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}

