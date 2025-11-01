package hbnu.project.zhiyansentineldashboard.service;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import hbnu.project.zhiyansentineldashboard.dto.DegradeRuleDTO;
import hbnu.project.zhiyansentineldashboard.dto.FlowRuleDTO;
import hbnu.project.zhiyansentineldashboard.dto.SystemRuleDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sentinel 规则管理服务
 *
 * @author ErgouTree
 */
@Slf4j
@Service
public class SentinelRuleService {

    /**
     * 获取所有流控规则
     *
     * @param app 应用名称
     * @return 流控规则列表
     */
    public List<FlowRuleDTO> getFlowRules(String app) {
        List<FlowRule> rules = FlowRuleManager.getRules();
        return rules.stream()
                .filter(rule -> app == null || app.isEmpty() || app.equals(rule.getResource()))
                .map(this::convertToFlowRuleDTO)
                .collect(Collectors.toList());
    }

    /**
     * 添加流控规则
     *
     * @param ruleDTO 流控规则
     * @return 是否成功
     */
    public boolean addFlowRule(FlowRuleDTO ruleDTO) {
        try {
            FlowRule rule = convertToFlowRule(ruleDTO);
            List<FlowRule> rules = new ArrayList<>(FlowRuleManager.getRules());
            rules.add(rule);
            FlowRuleManager.loadRules(rules);
            log.info("添加流控规则成功: resource={}, count={}", ruleDTO.getResource(), ruleDTO.getCount());
            return true;
        } catch (Exception e) {
            log.error("添加流控规则失败", e);
            return false;
        }
    }

    /**
     * 更新流控规则
     *
     * @param ruleDTO 流控规则
     * @return 是否成功
     */
    public boolean updateFlowRule(FlowRuleDTO ruleDTO) {
        try {
            List<FlowRule> rules = new ArrayList<>(FlowRuleManager.getRules());
            rules.removeIf(rule -> rule.getResource().equals(ruleDTO.getResource()));
            rules.add(convertToFlowRule(ruleDTO));
            FlowRuleManager.loadRules(rules);
            log.info("更新流控规则成功: resource={}", ruleDTO.getResource());
            return true;
        } catch (Exception e) {
            log.error("更新流控规则失败", e);
            return false;
        }
    }

    /**
     * 删除流控规则
     *
     * @param resource 资源名称
     * @return 是否成功
     */
    public boolean deleteFlowRule(String resource) {
        try {
            List<FlowRule> rules = FlowRuleManager.getRules().stream()
                    .filter(rule -> !rule.getResource().equals(resource))
                    .collect(Collectors.toList());
            FlowRuleManager.loadRules(rules);
            log.info("删除流控规则成功: resource={}", resource);
            return true;
        } catch (Exception e) {
            log.error("删除流控规则失败", e);
            return false;
        }
    }

    /**
     * 获取所有降级规则
     *
     * @param app 应用名称
     * @return 降级规则列表
     */
    public List<DegradeRuleDTO> getDegradeRules(String app) {
        List<DegradeRule> rules = DegradeRuleManager.getRules();
        return rules.stream()
                .filter(rule -> app == null || app.isEmpty() || app.equals(rule.getResource()))
                .map(this::convertToDegradeRuleDTO)
                .collect(Collectors.toList());
    }

    /**
     * 添加降级规则
     *
     * @param ruleDTO 降级规则
     * @return 是否成功
     */
    public boolean addDegradeRule(DegradeRuleDTO ruleDTO) {
        try {
            DegradeRule rule = convertToDegradeRule(ruleDTO);
            List<DegradeRule> rules = new ArrayList<>(DegradeRuleManager.getRules());
            rules.add(rule);
            DegradeRuleManager.loadRules(rules);
            log.info("添加降级规则成功: resource={}", ruleDTO.getResource());
            return true;
        } catch (Exception e) {
            log.error("添加降级规则失败", e);
            return false;
        }
    }

    /**
     * 获取系统规则
     *
     * @return 系统规则列表
     */
    public List<SystemRuleDTO> getSystemRules() {
        List<SystemRule> rules = SystemRuleManager.getRules();
        return rules.stream()
                .map(this::convertToSystemRuleDTO)
                .collect(Collectors.toList());
    }

    /**
     * 添加系统规则
     *
     * @param ruleDTO 系统规则
     * @return 是否成功
     */
    public boolean addSystemRule(SystemRuleDTO ruleDTO) {
        try {
            SystemRule rule = convertToSystemRule(ruleDTO);
            List<SystemRule> rules = new ArrayList<>(SystemRuleManager.getRules());
            rules.add(rule);
            SystemRuleManager.loadRules(rules);
            log.info("添加系统规则成功");
            return true;
        } catch (Exception e) {
            log.error("添加系统规则失败", e);
            return false;
        }
    }

    // ===== 转换方法 =====

    private FlowRuleDTO convertToFlowRuleDTO(FlowRule rule) {
        return FlowRuleDTO.builder()
                .resource(rule.getResource())
                .grade(rule.getGrade())
                .count(rule.getCount())
                .limitApp(rule.getLimitApp())
                .strategy(rule.getStrategy())
                .controlBehavior(rule.getControlBehavior())
                .warmUpPeriodSec(rule.getWarmUpPeriodSec())
                .maxQueueingTimeMs(rule.getMaxQueueingTimeMs())
                .clusterMode(rule.isClusterMode())
                .build();
    }

    private FlowRule convertToFlowRule(FlowRuleDTO dto) {
        FlowRule rule = new FlowRule();
        rule.setResource(dto.getResource());
        rule.setGrade(dto.getGrade());
        rule.setCount(dto.getCount());
        rule.setLimitApp(dto.getLimitApp() != null ? dto.getLimitApp() : "default");
        rule.setStrategy(dto.getStrategy() != null ? dto.getStrategy() : 0);
        rule.setControlBehavior(dto.getControlBehavior() != null ? dto.getControlBehavior() : 0);
        if (dto.getWarmUpPeriodSec() != null) {
            rule.setWarmUpPeriodSec(dto.getWarmUpPeriodSec());
        }
        if (dto.getMaxQueueingTimeMs() != null) {
            rule.setMaxQueueingTimeMs(dto.getMaxQueueingTimeMs());
        }
        rule.setClusterMode(dto.getClusterMode() != null && dto.getClusterMode());
        return rule;
    }

    private DegradeRuleDTO convertToDegradeRuleDTO(DegradeRule rule) {
        return DegradeRuleDTO.builder()
                .resource(rule.getResource())
                .grade(rule.getGrade())
                .count(rule.getCount())
                .timeWindow(rule.getTimeWindow())
                .minRequestAmount(rule.getMinRequestAmount())
                .slowRatioThreshold((int) rule.getSlowRatioThreshold())
                .statIntervalMs(rule.getStatIntervalMs())
                .build();
    }

    private DegradeRule convertToDegradeRule(DegradeRuleDTO dto) {
        DegradeRule rule = new DegradeRule();
        rule.setResource(dto.getResource());
        rule.setGrade(dto.getGrade());
        rule.setCount(dto.getCount());
        rule.setTimeWindow(dto.getTimeWindow());
        if (dto.getMinRequestAmount() != null) {
            rule.setMinRequestAmount(dto.getMinRequestAmount());
        }
        if (dto.getSlowRatioThreshold() != null) {
            rule.setSlowRatioThreshold(dto.getSlowRatioThreshold());
        }
        if (dto.getStatIntervalMs() != null) {
            rule.setStatIntervalMs(dto.getStatIntervalMs());
        }
        return rule;
    }

    private SystemRuleDTO convertToSystemRuleDTO(SystemRule rule) {
        return SystemRuleDTO.builder()
                .highestSystemLoad(rule.getHighestSystemLoad())
                .avgRt(rule.getAvgRt())
                .maxThread(rule.getMaxThread())
                .qps(rule.getQps())
                .highestCpuUsage(rule.getHighestCpuUsage())
                .build();
    }

    private SystemRule convertToSystemRule(SystemRuleDTO dto) {
        SystemRule rule = new SystemRule();
        if (dto.getHighestSystemLoad() != null) {
            rule.setHighestSystemLoad(dto.getHighestSystemLoad());
        }
        if (dto.getAvgRt() != null) {
            rule.setAvgRt(dto.getAvgRt());
        }
        if (dto.getMaxThread() != null) {
            rule.setMaxThread(dto.getMaxThread());
        }
        if (dto.getQps() != null) {
            rule.setQps(dto.getQps());
        }
        if (dto.getHighestCpuUsage() != null) {
            rule.setHighestCpuUsage(dto.getHighestCpuUsage());
        }
        return rule;
    }
}

