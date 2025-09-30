package hbnu.project.zhiyannacos.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 提供服务健康状态检查端点，用于 Nacos 服务发现和负载均衡
 *
 * @author ErgouTree
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    /**
     * 健康检查端点
     * 返回服务运行状态和基本信息
     *
     * @return 健康状态信息
     */
    @GetMapping
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "zhiyan-nacos");
        result.put("timestamp", LocalDateTime.now());
        result.put("message", "智研项目 Nacos 服务运行正常");
        return result;
    }

    /**
     * 服务信息端点
     * 返回服务的详细信息
     *
     * @return 服务信息
     */
    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> result = new HashMap<>();
        result.put("name", "zhiyan-nacos");
        result.put("version", "0.0.1-SNAPSHOT");
        result.put("description", "智研项目微服务注册中心（基于Nacos）");
        result.put("port", 8849);
        result.put("profiles", "dev");
        return result;
    }
}
