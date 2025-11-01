package hbnu.project.zhiyanskywalking.controller;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyanskywalking.dto.*;
import hbnu.project.zhiyanskywalking.service.SkyWalkingQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SkyWalking 监控查询Controller
 *
 * @author ErgouTree
 */
@Slf4j
@RestController
@RequestMapping("/skywalking")
@RequiredArgsConstructor
@Tag(name = "SkyWalking监控", description = "分布式追踪和性能监控API")
public class MonitorController {

    private final SkyWalkingQueryService skyWalkingQueryService;

    @GetMapping("/services")
    @Operation(summary = "获取所有服务", description = "获取所有已注册的微服务列表")
    public R<List<ServiceInfo>> getServices() {
        try {
            List<ServiceInfo> services = skyWalkingQueryService.getServices();
            return R.ok(services, "获取服务列表成功");
        } catch (Exception e) {
            log.error("获取服务列表失败", e);
            return R.fail("获取服务列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/traces")
    @Operation(summary = "获取追踪列表", description = "分页获取分布式追踪记录")
    public R<List<TraceInfo>> getTraces(
            @Parameter(description = "服务ID", example = "zhiyan-auth", required = true) 
            @RequestParam String serviceId,
            @Parameter(description = "端点名称（可选）", example = "/api/auth/login") 
            @RequestParam(required = false) String endpointName,
            @Parameter(description = "页码", example = "1") 
            @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页大小", example = "10") 
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            List<TraceInfo> traces = skyWalkingQueryService.getTraces(serviceId, endpointName, pageNum, pageSize);
            return R.ok(traces, "获取追踪列表成功");
        } catch (Exception e) {
            log.error("获取追踪列表失败: serviceId={}", serviceId, e);
            return R.fail("获取追踪列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/topology")
    @Operation(summary = "获取服务拓扑图", description = "获取微服务间的调用拓扑关系")
    public R<ServiceTopology> getTopology() {
        try {
            ServiceTopology topology = skyWalkingQueryService.getServiceTopology();
            return R.ok(topology, "获取服务拓扑成功");
        } catch (Exception e) {
            log.error("获取服务拓扑失败", e);
            return R.fail("获取服务拓扑失败: " + e.getMessage());
        }
    }

    @GetMapping("/metrics/{serviceId}")
    @Operation(summary = "获取服务指标", description = "获取指定服务的性能指标")
    public R<ServiceMetric> getServiceMetrics(
            @Parameter(description = "服务ID", example = "zhiyan-auth", required = true) 
            @PathVariable String serviceId) {
        try {
            ServiceMetric metrics = skyWalkingQueryService.getServiceMetrics(serviceId);
            return R.ok(metrics, "获取服务指标成功");
        } catch (Exception e) {
            log.error("获取服务指标失败: serviceId={}", serviceId, e);
            return R.fail("获取服务指标失败: " + e.getMessage());
        }
    }
}

