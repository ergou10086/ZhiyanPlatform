package hbnu.project.zhiyancommonseata.config;

import com.zaxxer.hikari.HikariDataSource;
import io.seata.rm.datasource.DataSourceProxy;
import io.seata.spring.annotation.GlobalTransactionScanner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Seata 分布式事务配置
 * 提供数据源代理、事务扫描器等核心组件
 *
 * @author ErgouTree
 */
@Slf4j
@AutoConfiguration
@ConditionalOnProperty(prefix = "seata", name = "enabled", havingValue = "true", matchIfMissing = false)
public class SeataConfiguration {

    /**
     * 配置 Seata 数据源代理
     * 拦截所有数据库操作，实现分支事务管理
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public DataSource druidDataSource() {
        return new HikariDataSource();
    }


    /**
     * Seata 数据源代理
     * 将普通数据源包装为 Seata 代理数据源
     */
    @Primary
    @Bean("dataSource")
    public DataSourceProxy dataSourceProxy(DataSource druidDataSource) {
        log.info("初始化 Seata 数据源代理");
        return new DataSourceProxy(druidDataSource);
    }


    /**
     * 全局事务扫描器
     * 扫描 @GlobalTransactional 注解
     */
    @Bean
    @ConditionalOnMissingBean
    public GlobalTransactionScanner globalTransactionScanner(
            @Value("${spring.application.name}") String applicationId,  // 从配置获取服务名作为applicationId
            @Value("${seata.tx-service-group}") String txServiceGroup) {  // 从配置获取事务服务组
        log.info("初始化 Seata 全局事务扫描器，applicationId: {}, txServiceGroup: {}", applicationId, txServiceGroup);
        return new GlobalTransactionScanner(applicationId, txServiceGroup);
    }
}
