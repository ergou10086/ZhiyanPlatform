package hbnu.project.common.sentinel.provider;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import hbnu.project.common.sentinel.config.SentinelProperties;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Properties;

/**
 * Sentinel 规则提供者
 * <p>
 * 从 Nacos 动态加载 Sentinel 规则
 * </p>
 *
 * @author ErgouTree
 */
@Slf4j
public record SentinelRuleProvider(SentinelProperties sentinelProperties) {

    /**
     * 从 Nacos 加载所有规则
     *
     * @param appName 应用名称
     */
    public void loadRulesFromNacos(String appName) {
        SentinelProperties.Nacos nacos = sentinelProperties.getNacos();

        if (!nacos.getEnabled()) {
            log.info("[Sentinel] Nacos 数据源未启用");
            return;
        }

        try {
            // 加载流控规则
            loadFlowRulesFromNacos(appName, nacos);

            // 加载降级规则
            loadDegradeRulesFromNacos(appName, nacos);

            // 加载系统规则
            loadSystemRulesFromNacos(appName, nacos);

            log.info("[Sentinel] 所有规则从 Nacos 加载完成");
        } catch (Exception e) {
            log.error("[Sentinel] 从 Nacos 加载规则失败", e);
            throw new RuntimeException("Failed to load rules from Nacos", e);
        }
    }

    /**
     * 加载流控规则
     */
    private void loadFlowRulesFromNacos(String appName, SentinelProperties.Nacos nacos) {
        String dataId = appName + "-flow-rules." + nacos.getDataIdSuffix();

        log.info("[Sentinel] 加载流控规则，dataId: {}", dataId);

        ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new NacosDataSource<>(
                nacos.getServerAddr(),
                nacos.getGroupId(),
                dataId,
                source -> JSON.parseObject(source, new TypeReference<>() {
                })
        );

        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());

        // 使用 loadConfig 方法加载初始规则
        List<FlowRule> rules;
        try {
            rules = flowRuleDataSource.loadConfig();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (CollUtil.isNotEmpty(rules)) {
            log.info("[Sentinel] 流控规则加载成功，规则数量: {}", rules.size());
        } else {
            log.info("[Sentinel] 流控规则为空");
        }
    }

    /**
     * 加载降级规则
     */
    private void loadDegradeRulesFromNacos(String appName, SentinelProperties.Nacos nacos) {
        String dataId = appName + "-degrade-rules." + nacos.getDataIdSuffix();

        log.info("[Sentinel] 加载降级规则，dataId: {}", dataId);

        ReadableDataSource<String, List<DegradeRule>> degradeRuleDataSource = new NacosDataSource<>(
                nacos.getServerAddr(),
                nacos.getGroupId(),
                dataId,
                source -> JSON.parseObject(source, new TypeReference<>() {
                })
        );

        DegradeRuleManager.register2Property(degradeRuleDataSource.getProperty());

        // 使用 loadConfig 方法加载初始规则
        List<DegradeRule> rules;
        try {
            rules = degradeRuleDataSource.loadConfig();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (CollUtil.isNotEmpty(rules)) {
            log.info("[Sentinel] 降级规则加载成功，规则数量: {}", rules.size());
        } else {
            log.info("[Sentinel] 降级规则为空");
        }
    }

    /**
     * 加载系统规则
     */
    private void loadSystemRulesFromNacos(String appName, SentinelProperties.Nacos nacos) {
        String dataId = appName + "-system-rules." + nacos.getDataIdSuffix();

        log.info("[Sentinel] 加载系统规则，dataId: {}", dataId);

        ReadableDataSource<String, List<SystemRule>> systemRuleDataSource = new NacosDataSource<>(
                nacos.getServerAddr(),
                nacos.getGroupId(),
                dataId,
                source -> JSON.parseObject(source, new TypeReference<>() {
                })
        );

        SystemRuleManager.register2Property(systemRuleDataSource.getProperty());

        // 使用 loadConfig 方法加载初始规则
        List<SystemRule> rules;
        try {
            rules = systemRuleDataSource.loadConfig();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (CollUtil.isNotEmpty(rules)) {
            log.info("[Sentinel] 系统规则加载成功，规则数量: {}", rules.size());
        } else {
            log.info("[Sentinel] 系统规则为空");
        }
    }

    /**
     * 创建 Nacos 配置
     */
    private Properties createNacosProperties(SentinelProperties.Nacos nacos) {
        Properties properties = new Properties();
        properties.setProperty("serverAddr", nacos.getServerAddr());
        properties.setProperty("namespace", nacos.getNamespace());
        properties.setProperty("username", nacos.getUsername());
        properties.setProperty("password", nacos.getPassword());
        return properties;
    }
}

