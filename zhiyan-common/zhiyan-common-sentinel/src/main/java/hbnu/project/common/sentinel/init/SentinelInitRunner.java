package hbnu.project.common.sentinel.init;

import cn.hutool.core.util.StrUtil;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import hbnu.project.common.sentinel.config.SentinelProperties;
import hbnu.project.common.sentinel.provider.SentinelRuleProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

/**
 * Sentinel 初始化运行器
 * <p>
 * 在应用启动时初始化 Sentinel 配置
 * </p>
 *
 * @author ErgouTree
 */
@Slf4j
@RequiredArgsConstructor
public class SentinelInitRunner implements ApplicationRunner, Ordered {

    private final SentinelProperties properties;
    private final Environment environment;
    private final SentinelRuleProvider ruleProvider;

    @Override
    public void run(ApplicationArguments args) {
        log.info("========================================");
        log.info("    Sentinel 流控保护模块初始化");
        log.info("========================================");

        // 获取应用名称
        String appName = environment.getProperty("spring.application.name", "unknown-service");
        
        // 配置 Dashboard 连接
        configureDashboard(appName);

        // 配置日志
        configureLog();

        // 加载规则
        loadRules(appName);

        log.info("========================================");
        log.info("  Sentinel 初始化完成");
        log.info("  应用名称: {}", appName);
        log.info("  Dashboard: {}", properties.getDashboard().getAddress());
        log.info("  客户端端口: {}", properties.getDashboard().getClientPort());
        log.info("  饥饿加载: {}", properties.getEager());
        log.info("  Nacos数据源: {}", properties.getNacos().getEnabled() ? "已启用" : "未启用");
        log.info("========================================");
    }

    /**
     * 配置 Dashboard 连接
     */
    private void configureDashboard(String appName) {
        SentinelProperties.Dashboard dashboard = properties.getDashboard();

        // 设置应用名称
        System.setProperty("project.name", appName);
        
        // 设置 Dashboard 地址
        if (StrUtil.isNotBlank(dashboard.getHost())) {
            System.setProperty(TransportConfig.CONSOLE_SERVER, dashboard.getAddress());
            log.info("[Sentinel] Dashboard 地址: {}", dashboard.getAddress());
        }

        // 设置客户端端口
        if (dashboard.getClientPort() != null) {
            System.setProperty(TransportConfig.SERVER_PORT, String.valueOf(dashboard.getClientPort()));
            log.info("[Sentinel] 客户端通信端口: {}", dashboard.getClientPort());
        }

        // 设置心跳周期
        if (dashboard.getHeartbeatIntervalMs() != null) {
            System.setProperty(TransportConfig.HEARTBEAT_INTERVAL_MS, 
                String.valueOf(dashboard.getHeartbeatIntervalMs()));
        }
    }

    /**
     * 配置日志
     */
    private void configureLog() {
        SentinelProperties.Log log = properties.getLog();

        if (StrUtil.isNotBlank(log.getDir())) {
            System.setProperty("csp.sentinel.log.dir", log.getDir());
            this.log.info("[Sentinel] 日志目录: {}", log.getDir());
        }

        if (log.getSwitchPid() != null) {
            System.setProperty("csp.sentinel.log.use.pid", String.valueOf(log.getSwitchPid()));
        }
    }

    /**
     * 加载规则
     */
    private void loadRules(String appName) {
        if (properties.getNacos().getEnabled()) {
            log.info("[Sentinel] 从 Nacos 加载规则配置...");
            try {
                ruleProvider.loadRulesFromNacos(appName);
                log.info("[Sentinel] Nacos 规则配置加载成功");
            } catch (Exception e) {
                log.warn("[Sentinel] Nacos 规则配置加载失败: {}", e.getMessage());
                log.info("[Sentinel] 将使用本地默认规则");
            }
        } else {
            log.info("[Sentinel] Nacos 数据源未启用，使用本地规则");
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }
}

