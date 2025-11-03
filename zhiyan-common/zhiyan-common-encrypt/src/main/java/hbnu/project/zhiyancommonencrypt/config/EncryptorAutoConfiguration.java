package hbnu.project.zhiyancommonencrypt.config;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import hbnu.project.zhiyancommonencrypt.core.EncryptContext;
import hbnu.project.zhiyancommonencrypt.core.EncryptorManager;
import hbnu.project.zhiyancommonencrypt.interceptor.MybatisDecryptInterceptor;
import hbnu.project.zhiyancommonencrypt.interceptor.MybatisEncryptInterceptor;
import hbnu.project.zhiyancommonencrypt.properties.EncryptorProperties;
import hbnu.project.zhiyancommonencrypt.utils.EncryptUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 加密器自动配置类
 * 自动配置数据库字段加密功能
 *
 * @author ErgouTree
 * @version 2.0.0
 */
@Slf4j
@AutoConfiguration(after = MybatisPlusAutoConfiguration.class)
@EnableConfigurationProperties(EncryptorProperties.class)
@ConditionalOnClass(MybatisPlusAutoConfiguration.class)
@ConditionalOnProperty(prefix = "zhiyan.encrypt", name = "enabled", havingValue = "true")
public class EncryptorAutoConfiguration {

    /**
     * 创建加密上下文
     */
    @Bean
    public EncryptContext encryptContext(EncryptorProperties properties) {
        EncryptContext context = EncryptContext.from(properties);

        // 将配置同步到 EncryptUtils
        EncryptUtils.setDefaultAesKey(properties.getAesKey());
        EncryptUtils.setDefaultSm4Key(properties.getSm4Key());
        if (properties.getRsaPublicKey() != null && properties.getRsaPrivateKey() != null) {
            EncryptUtils.setDefaultRsaKeys(properties.getRsaPublicKey(), properties.getRsaPrivateKey());
        }

        log.info("加密上下文初始化完成 - 默认算法: {}", properties.getAlgorithm());
        return context;
    }

    /**
     * 创建加密器管理器
     */
    @Bean
    @ConditionalOnClass(MybatisPlusProperties.class)
    @ConditionalOnProperty(prefix = "zhiyan.encrypt.database", name = "enabled", havingValue = "true")
    public EncryptorManager encryptorManager(MybatisPlusProperties mybatisPlusProperties) {
        String typeAliasesPackage = mybatisPlusProperties.getTypeAliasesPackage();
        EncryptorManager manager = new EncryptorManager(typeAliasesPackage);
        log.info("加密器管理器初始化完成 - 扫描包: {}", typeAliasesPackage);
        return manager;
    }

    /**
     * MyBatis 加密拦截器
     */
    @Bean
    @ConditionalOnProperty(prefix = "zhiyan.encrypt.database", name = "enabled", havingValue = "true")
    public MybatisEncryptInterceptor mybatisEncryptInterceptor(
        EncryptorManager encryptorManager,
        EncryptorProperties properties
    ) {
        log.info("MyBatis 加密拦截器已注册");
        return new MybatisEncryptInterceptor(encryptorManager, properties);
    }

    /**
     * MyBatis 解密拦截器
     */
    @Bean
    @ConditionalOnProperty(prefix = "zhiyan.encrypt.database", name = "enabled", havingValue = "true")
    public MybatisDecryptInterceptor mybatisDecryptInterceptor(
        EncryptorManager encryptorManager,
        EncryptorProperties properties
    ) {
        log.info("MyBatis 解密拦截器已注册");
        return new MybatisDecryptInterceptor(encryptorManager, properties);
    }
}
