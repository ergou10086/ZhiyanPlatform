package hbnu.project.zhiyansentineldashboard.controller;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyansentineldashboard.dto.AppInfo;
import hbnu.project.zhiyansentineldashboard.dto.MetricDTO;
import hbnu.project.zhiyansentineldashboard.service.SentinelMetricService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Sentinel 监控指标控制器
 *
 * @author ErgouTree
 */
@Slf4j
@RestController
@RequestMapping("/sentinel/metrics")
@RequiredArgsConstructor
@Tag(name = "Sentinel监控数据", description = "实时监控指标查询")
public class SentinelMetricController {

    private final SentinelMetricService sentinelMetricService;

    @GetMapping("/apps")
    @Operation(summary = "获取应用列表", description = "获取所有已注册的应用列表")
    public R<List<AppInfo>> getAppList() {
        try {
            List<AppInfo> apps = sentinelMetricService.getAppList();
            return R.ok(apps, "获取应用列表成功");
        } catch (Exception e) {
            log.error("获取应用列表失败", e);
            return R.fail("获取应用列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/apps/{appName}")
    @Operation(summary = "注册应用", description = "注册新的监控应用")
    public R<Boolean> registerApp(
            @Parameter(description = "应用名称") @PathVariable String appName) {
        try {
            sentinelMetricService.registerApp(appName);
            return R.ok(true, "注册应用成功");
        } catch (Exception e) {
            log.error("注册应用失败", e);
            return R.fail("注册应用失败: " + e.getMessage());
        }
    }

    @GetMapping
    @Operation(summary = "获取监控指标", description = "获取指定应用和资源的历史监控数据")
    public R<List<MetricDTO>> getMetrics(
            @Parameter(description = "应用名称", required = true) @RequestParam String app,
            @Parameter(description = "资源名称", required = true) @RequestParam String resource,
            @Parameter(description = "开始时间（时间戳）") @RequestParam(required = false, defaultValue = "0") Long startTime,
            @Parameter(description = "结束时间（时间戳）") @RequestParam(required = false) Long endTime) {
        try {
            if (endTime == null) {
                endTime = System.currentTimeMillis();
            }
            if (startTime == 0) {
                startTime = endTime - 3600000; // 默认最近1小时
            }
            
            List<MetricDTO> metrics = sentinelMetricService.getMetrics(app, resource, startTime, endTime);
            return R.ok(metrics, "获取监控指标成功");
        } catch (Exception e) {
            log.error("获取监控指标失败", e);
            return R.fail("获取监控指标失败: " + e.getMessage());
        }
    }

    @GetMapping("/realtime")
    @Operation(summary = "获取实时指标", description = "获取指定资源的实时监控指标")
    public R<MetricDTO> getRealtimeMetric(
            @Parameter(description = "应用名称", required = true) @RequestParam String app,
            @Parameter(description = "资源名称", required = true) @RequestParam String resource) {
        try {
            MetricDTO metric = sentinelMetricService.getRealtimeMetric(app, resource);
            return R.ok(metric, "获取实时指标成功");
        } catch (Exception e) {
            log.error("获取实时指标失败", e);
            return R.fail("获取实时指标失败: " + e.getMessage());
        }
    }
}

