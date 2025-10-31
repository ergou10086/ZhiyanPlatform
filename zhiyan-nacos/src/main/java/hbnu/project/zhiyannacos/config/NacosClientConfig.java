package hbnu.project.zhiyannacos.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import hbnu.project.zhiyannacos.config.properties.NacosManagementProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * Nacos 客户端配置
 *
 * @author ErgouTree
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class NacosClientConfig {

    private final NacosManagementProperties properties;

    /**
     * 配置 Nacos ConfigService Bean（底层客户端）
     */
    @Bean
    public ConfigService configService() throws NacosException {
        log.info("初始化 Nacos ConfigService: serverAddr={}, namespace={}",
                properties.getServerAddr(), properties.getNamespace());

        Properties nacosProperties = new Properties();
        nacosProperties.put("serverAddr", properties.getServerAddr());
        nacosProperties.put("namespace", properties.getNamespace());
        nacosProperties.put("username", properties.getUsername());
        nacosProperties.put("password", properties.getPassword());

        return NacosFactory.createConfigService(nacosProperties);
    }

    /**
     * 配置 Nacos NamingService Bean（底层客户端）
     */
    @Bean
    public NamingService namingService() throws NacosException {
        log.info("初始化 Nacos NamingService: serverAddr={}, namespace={}",
                properties.getServerAddr(), properties.getNamespace());

        Properties nacosProperties = new Properties();
        nacosProperties.put("serverAddr", properties.getServerAddr());
        nacosProperties.put("namespace", properties.getNamespace());
        nacosProperties.put("username", properties.getUsername());
        nacosProperties.put("password", properties.getPassword());

        return NacosFactory.createNamingService(nacosProperties);
    }
}
