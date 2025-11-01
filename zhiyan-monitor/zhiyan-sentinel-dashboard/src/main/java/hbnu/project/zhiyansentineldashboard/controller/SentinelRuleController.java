package hbnu.project.zhiyansentineldashboard.controller;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyansentineldashboard.dto.DegradeRuleDTO;
import hbnu.project.zhiyansentineldashboard.dto.FlowRuleDTO;
import hbnu.project.zhiyansentineldashboard.dto.SystemRuleDTO;
import hbnu.project.zhiyansentineldashboard.service.SentinelRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Sentinel 规则管理控制器
 *
 * @author ErgouTree
 */
@Slf4j
@RestController
@RequestMapping("/sentinel/rules")
@RequiredArgsConstructor
@Tag(name = "Sentinel规则管理", description = "流控规则、降级规则、系统规则管理")
public class SentinelRuleController {

    private final SentinelRuleService sentinelRuleService;

    // ========== 流控规则 ==========

    @GetMapping("/flow")
    @Operation(summary = "获取流控规则", description = "获取指定应用的流控规则列表")
    public R<List<FlowRuleDTO>> getFlowRules(
            @Parameter(description = "应用名称（可选）") @RequestParam(required = false) String app) {
        try {
            List<FlowRuleDTO> rules = sentinelRuleService.getFlowRules(app);
            return R.ok(rules, "获取流控规则成功");
        } catch (Exception e) {
            log.error("获取流控规则失败", e);
            return R.fail("获取流控规则失败: " + e.getMessage());
        }
    }

    @PostMapping("/flow")
    @Operation(summary = "添加流控规则", description = "为指定资源添加流控规则")
    public R<Boolean> addFlowRule(@RequestBody FlowRuleDTO ruleDTO) {
        try {
            boolean result = sentinelRuleService.addFlowRule(ruleDTO);
            return result ? R.ok(true, "添加流控规则成功") : R.fail("添加流控规则失败");
        } catch (Exception e) {
            log.error("添加流控规则失败", e);
            return R.fail("添加流控规则失败: " + e.getMessage());
        }
    }

    @PutMapping("/flow")
    @Operation(summary = "更新流控规则", description = "更新指定资源的流控规则")
    public R<Boolean> updateFlowRule(@RequestBody FlowRuleDTO ruleDTO) {
        try {
            boolean result = sentinelRuleService.updateFlowRule(ruleDTO);
            return result ? R.ok(true, "更新流控规则成功") : R.fail("更新流控规则失败");
        } catch (Exception e) {
            log.error("更新流控规则失败", e);
            return R.fail("更新流控规则失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/flow/{resource}")
    @Operation(summary = "删除流控规则", description = "删除指定资源的流控规则")
    public R<Boolean> deleteFlowRule(
            @Parameter(description = "资源名称") @PathVariable String resource) {
        try {
            boolean result = sentinelRuleService.deleteFlowRule(resource);
            return result ? R.ok(true, "删除流控规则成功") : R.fail("删除流控规则失败");
        } catch (Exception e) {
            log.error("删除流控规则失败", e);
            return R.fail("删除流控规则失败: " + e.getMessage());
        }
    }

    // ========== 降级规则 ==========

    @GetMapping("/degrade")
    @Operation(summary = "获取降级规则", description = "获取指定应用的熔断降级规则列表")
    public R<List<DegradeRuleDTO>> getDegradeRules(
            @Parameter(description = "应用名称（可选）") @RequestParam(required = false) String app) {
        try {
            List<DegradeRuleDTO> rules = sentinelRuleService.getDegradeRules(app);
            return R.ok(rules, "获取降级规则成功");
        } catch (Exception e) {
            log.error("获取降级规则失败", e);
            return R.fail("获取降级规则失败: " + e.getMessage());
        }
    }

    @PostMapping("/degrade")
    @Operation(summary = "添加降级规则", description = "为指定资源添加熔断降级规则")
    public R<Boolean> addDegradeRule(@RequestBody DegradeRuleDTO ruleDTO) {
        try {
            boolean result = sentinelRuleService.addDegradeRule(ruleDTO);
            return result ? R.ok(true, "添加降级规则成功") : R.fail("添加降级规则失败");
        } catch (Exception e) {
            log.error("添加降级规则失败", e);
            return R.fail("添加降级规则失败: " + e.getMessage());
        }
    }

    // ========== 系统规则 ==========

    @GetMapping("/system")
    @Operation(summary = "获取系统规则", description = "获取系统级别的保护规则")
    public R<List<SystemRuleDTO>> getSystemRules() {
        try {
            List<SystemRuleDTO> rules = sentinelRuleService.getSystemRules();
            return R.ok(rules, "获取系统规则成功");
        } catch (Exception e) {
            log.error("获取系统规则失败", e);
            return R.fail("获取系统规则失败: " + e.getMessage());
        }
    }

    @PostMapping("/system")
    @Operation(summary = "添加系统规则", description = "添加系统级别的保护规则")
    public R<Boolean> addSystemRule(@RequestBody SystemRuleDTO ruleDTO) {
        try {
            boolean result = sentinelRuleService.addSystemRule(ruleDTO);
            return result ? R.ok(true, "添加系统规则成功") : R.fail("添加系统规则失败");
        } catch (Exception e) {
            log.error("添加系统规则失败", e);
            return R.fail("添加系统规则失败: " + e.getMessage());
        }
    }
}

