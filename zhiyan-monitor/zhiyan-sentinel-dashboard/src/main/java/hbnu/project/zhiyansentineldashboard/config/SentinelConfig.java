package hbnu.project.zhiyansentineldashboard.config;

import com.alibaba.csp.sentinel.init.InitExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sentinel 配置类
 *
 * @author ErgouTree
 */
@Slf4j
@Configuration
public class SentinelConfig {

    /**
     * 初始化 Sentinel
     */
    @Bean
    public ApplicationRunner sentinelInitRunner() {
        return args -> {
            log.info("=== 初始化 Sentinel 组件 ===");
            try {
                InitExecutor.doInit();
                log.info("✅ Sentinel 初始化成功");
            } catch (Exception e) {
                log.error("❌ Sentinel 初始化失败", e);
            }
        };
    }
}

