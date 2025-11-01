package hbnu.project.common.sentinel.util;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Sentinel 工具类
 * <p>
 * 提供便捷的 Sentinel API 封装
 * </p>
 *
 * @author ErgouTree
 */
@Slf4j
public class SentinelUtil {

    /**
     * 执行受保护的代码块
     *
     * @param resourceName 资源名称
     * @param supplier     业务逻辑
     * @param <T>          返回类型
     * @return 执行结果
     * @throws BlockException 限流异常
     */
    public static <T> T execute(String resourceName, Supplier<T> supplier) throws BlockException {
        Entry entry = null;
        try {
            entry = SphU.entry(resourceName);
            return supplier.get();
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }

    /**
     * 执行受保护的代码块（带降级）
     *
     * @param resourceName  资源名称
     * @param supplier      业务逻辑
     * @param fallback      降级逻辑
     * @param <T>           返回类型
     * @return 执行结果
     */
    public static <T> T executeWithFallback(String resourceName, Supplier<T> supplier, Supplier<T> fallback) {
        try {
            return execute(resourceName, supplier);
        } catch (BlockException e) {
            log.warn("[Sentinel] 资源 {} 被限流，执行降级逻辑", resourceName);
            return fallback.get();
        }
    }

    /**
     * 设置当前调用来源
     *
     * @param origin 来源标识
     */
    public static void setOrigin(String origin) {
        ContextUtil.enter(origin);
    }

    /**
     * 清除当前上下文
     */
    public static void clearContext() {
        ContextUtil.exit();
    }

    /**
     * 动态添加流控规则
     *
     * @param resource 资源名称
     * @param qps      QPS 阈值
     */
    public static void addFlowRule(String resource, int qps) {
        List<FlowRule> rules = new ArrayList<>(FlowRuleManager.getRules());
        
        FlowRule rule = new FlowRule();
        rule.setResource(resource);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(qps);
        rule.setLimitApp("default");
        
        rules.add(rule);
        FlowRuleManager.loadRules(rules);
        
        log.info("[Sentinel] 添加流控规则: resource={}, qps={}", resource, qps);
    }

    /**
     * 动态添加降级规则
     *
     * @param resource    资源名称
     * @param count       阈值
     * @param timeWindow  时间窗口（秒）
     * @param grade       降级策略（0-慢调用比例 1-异常比例 2-异常数）
     */
    public static void addDegradeRule(String resource, double count, int timeWindow, int grade) {
        List<DegradeRule> rules = new ArrayList<>(DegradeRuleManager.getRules());
        
        DegradeRule rule = new DegradeRule();
        rule.setResource(resource);
        rule.setCount(count);
        rule.setTimeWindow(timeWindow);
        rule.setGrade(grade);
        rule.setMinRequestAmount(5);
        
        rules.add(rule);
        DegradeRuleManager.loadRules(rules);
        
        log.info("[Sentinel] 添加降级规则: resource={}, count={}, timeWindow={}, grade={}", 
            resource, count, timeWindow, grade);
    }

    /**
     * 移除资源的所有规则
     *
     * @param resource 资源名称
     */
    public static void removeRules(String resource) {
        // 移除流控规则
        List<FlowRule> flowRules = new ArrayList<>();
        for (FlowRule rule : FlowRuleManager.getRules()) {
            if (!rule.getResource().equals(resource)) {
                flowRules.add(rule);
            }
        }
        FlowRuleManager.loadRules(flowRules);

        // 移除降级规则
        List<DegradeRule> degradeRules = new ArrayList<>();
        for (DegradeRule rule : DegradeRuleManager.getRules()) {
            if (!rule.getResource().equals(resource)) {
                degradeRules.add(rule);
            }
        }
        DegradeRuleManager.loadRules(degradeRules);

        log.info("[Sentinel] 移除资源所有规则: resource={}", resource);
    }

    /**
     * 获取所有流控规则
     *
     * @return 流控规则列表
     */
    public static List<FlowRule> getFlowRules() {
        return FlowRuleManager.getRules();
    }

    /**
     * 获取所有降级规则
     *
     * @return 降级规则列表
     */
    public static List<DegradeRule> getDegradeRules() {
        return DegradeRuleManager.getRules();
    }

    /**
     * 检查资源是否被限流
     *
     * @param resourceName 资源名称
     * @return true-被限流 false-未限流
     */
    public static boolean isBlocked(String resourceName) {
        Entry entry = null;
        try {
            entry = SphU.entry(resourceName);
            return false;
        } catch (BlockException e) {
            return true;
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }
}

