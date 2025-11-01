package hbnu.project.zhiyanskywalking.controller;

import hbnu.project.zhiyanskywalking.config.SkyWalkingProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 *
 * @author ErgouTree
 */
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
@Tag(name = "健康检查", description = "服务健康状态检查")
public class HealthController {

    private final SkyWalkingProperties properties;

    @GetMapping
    @Operation(summary = "健康检查", description = "返回服务运行状态")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "zhiyan-skywalking");
        result.put("timestamp", LocalDateTime.now());
        result.put("message", "SkyWalking 监控管理服务运行正常");
        result.put("oapServer", properties.getOap().getAddress());
        result.put("uiAddress", properties.getUi().getAddress());
        return result;
    }

    @GetMapping("/info")
    @Operation(summary = "服务信息", description = "返回服务详细信息")
    public Map<String, Object> info() {
        Map<String, Object> result = new HashMap<>();
        result.put("name", "zhiyan-skywalking");
        result.put("version", "0.0.1-SNAPSHOT");
        result.put("description", "智研平台 SkyWalking 监控管理服务");
        result.put("oapServer", properties.getOap().getAddress());
        result.put("graphqlEndpoint", properties.getOap().getGraphqlEndpoint());
        result.put("uiAddress", properties.getUi().getAddress());
        return result;
    }
}

