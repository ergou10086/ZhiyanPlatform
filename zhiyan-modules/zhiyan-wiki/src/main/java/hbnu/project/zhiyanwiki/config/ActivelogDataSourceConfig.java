package hbnu.project.zhiyanwiki.config;

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
        basePackages = "hbnu.project.zhiyanactivelog.repository",
        entityManagerFactoryRef = "activelogEntityManagerFactory",
        transactionManagerRef = "activelogTransactionManager"
)
public class ActivelogDataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.activelog")
    public DataSourceProperties activelogDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.activelog.hikari")
    public DataSource activelogDataSource(@Qualifier("activelogDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean activelogEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("activelogDataSource") DataSource dataSource) {
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.show_sql", false);
        
        return builder
                .dataSource(dataSource)
                .packages("hbnu.project.zhiyanactivelog.model.entity")
                .persistenceUnit("activelog")
                .properties(properties)
                .build();
    }

    @Bean
    public PlatformTransactionManager activelogTransactionManager(
            @Qualifier("activelogEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}

