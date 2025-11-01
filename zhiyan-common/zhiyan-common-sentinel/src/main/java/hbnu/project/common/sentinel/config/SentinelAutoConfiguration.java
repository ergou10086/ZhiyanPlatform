package hbnu.project.common.sentinel.config;

import cn.hutool.core.util.StrUtil;
import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import hbnu.project.common.sentinel.handler.DefaultBlockExceptionHandler;
import hbnu.project.common.sentinel.handler.GlobalBlockExceptionHandler;
import hbnu.project.common.sentinel.handler.SentinelExceptionHandler;
import hbnu.project.common.sentinel.init.SentinelInitRunner;
import hbnu.project.common.sentinel.provider.SentinelRuleProvider;
import hbnu.project.common.sentinel.util.SentinelUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel 自动配置类
 *
 * @author ErgouTree
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(SentinelProperties.class)
@ConditionalOnProperty(prefix = "zhiyan.sentinel", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SentinelAutoConfiguration {

    private final SentinelProperties properties;
    private final Environment environment;

    /**
     * 注册 Sentinel 切面，使 @SentinelResource 注解生效
     */
    @Bean
    @ConditionalOnMissingBean
    public SentinelResourceAspect sentinelResourceAspect() {
        log.info("[Sentinel] 注册 SentinelResourceAspect 切面");
        return new SentinelResourceAspect();
    }

    /**
     * 默认的限流降级处理器
     */
    @Bean
    @ConditionalOnMissingBean(SentinelExceptionHandler.class)
    public SentinelExceptionHandler defaultBlockExceptionHandler() {
        log.info("[Sentinel] 注册默认限流降级处理器");
        return new DefaultBlockExceptionHandler();
    }

    /**
     * 全局异常处理器
     */
    @Bean
    public GlobalBlockExceptionHandler globalBlockExceptionHandler(SentinelExceptionHandler handler) {
        log.info("[Sentinel] 注册全局限流异常处理器");
        return new GlobalBlockExceptionHandler(handler);
    }

    /**
     * Sentinel 规则提供者
     */
    @Bean
    @ConditionalOnMissingBean
    public SentinelRuleProvider sentinelRuleProvider() {
        log.info("[Sentinel] 注册 Sentinel 规则提供者");
        return new SentinelRuleProvider(properties);
    }

    /**
     * Sentinel 工具类
     */
    @Bean
    @ConditionalOnMissingBean
    public SentinelUtil sentinelUtil() {
        return new SentinelUtil();
    }

    /**
     * Sentinel 初始化运行器
     */
    @Bean
    public SentinelInitRunner sentinelInitRunner(SentinelRuleProvider ruleProvider) {
        log.info("[Sentinel] 注册 Sentinel 初始化运行器");
        return new SentinelInitRunner(properties, environment, ruleProvider);
    }

    /**
     * 配置全局默认流控规则
     */
    private void initGlobalFlowRules() {
        SentinelProperties.Global global = properties.getGlobal();
        
        if (global.getGlobalQps() <= 0 && global.getGlobalThread() <= 0) {
            return;
        }

        List<FlowRule> rules = new ArrayList<>();

        // 全局 QPS 限流
        if (global.getGlobalQps() > 0) {
            FlowRule qpsRule = new FlowRule();
            qpsRule.setResource("global-qps-limit");
            qpsRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
            qpsRule.setCount(global.getGlobalQps());
            rules.add(qpsRule);
            log.info("[Sentinel] 配置全局 QPS 限流：{}", global.getGlobalQps());
        }

        // 全局线程数限流
        if (global.getGlobalThread() > 0) {
            FlowRule threadRule = new FlowRule();
            threadRule.setResource("global-thread-limit");
            threadRule.setGrade(RuleConstant.FLOW_GRADE_THREAD);
            threadRule.setCount(global.getGlobalThread());
            rules.add(threadRule);
            log.info("[Sentinel] 配置全局线程数限流：{}", global.getGlobalThread());
        }

        FlowRuleManager.loadRules(rules);
    }
}

