package hbnu.project.zhiyansentineldashboard.service;

import com.alibaba.csp.sentinel.node.metric.MetricNode;
import com.alibaba.csp.sentinel.util.TimeUtil;
import hbnu.project.zhiyansentineldashboard.dto.AppInfo;
import hbnu.project.zhiyansentineldashboard.dto.MetricDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sentinel 监控指标服务
 *
 * @author ErgouTree
 */
@Slf4j
@Service
public class SentinelMetricService {

    // 简单的内存存储，日后应该使用数据库或时序数据库
    private final Map<String, List<MetricNode>> metricCache = new ConcurrentHashMap<>();
    private final Map<String, AppInfo> appCache = new ConcurrentHashMap<>();

    /**
     * 获取应用列表
     *
     * @return 应用列表
     */
    public List<AppInfo> getAppList() {
        return new ArrayList<>(appCache.values());
    }

    /**
     * 注册应用
     *
     * @param appName 应用名称
     */
    public void registerApp(String appName) {
        appCache.putIfAbsent(appName, AppInfo.builder()
                .app(appName)
                .appType(0)
                .machineCount(1)
                .healthyMachineCount(1)
                .lastFetch(System.currentTimeMillis())
                .build());
        log.info("注册应用: {}", appName);
    }

    /**
     * 获取监控指标
     *
     * @param app      应用名称
     * @param resource 资源名称
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 监控指标列表
     */
    public List<MetricDTO> getMetrics(String app, String resource, Long startTime, Long endTime) {
        List<MetricDTO> metrics = new ArrayList<>();
        
        // 从缓存中获取指标数据
        String key = app + ":" + resource;
        List<MetricNode> nodes = metricCache.getOrDefault(key, new ArrayList<>());

        for (MetricNode node : nodes) {
            if (node.getTimestamp() >= startTime && node.getTimestamp() <= endTime) {
                MetricDTO metric = MetricDTO.builder()
                        .app(app)
                        .resource(resource)
                        .timestamp(node.getTimestamp())
                        .passQps(node.getPassQps())
                        .successQps(node.getSuccessQps())
                        .blockQps(node.getBlockQps())
                        .exceptionQps(node.getExceptionQps())
                        .rt((double) node.getRt())
                        .occupiedPassQps(node.getOccupiedPassQps())
                        .build();
                metrics.add(metric);
            }
        }

        return metrics;
    }

    /**
     * 保存监控指标
     *
     * @param app      应用名称
     * @param resource 资源名称
     * @param metric   指标数据
     */
    public void saveMetric(String app, String resource, MetricNode metric) {
        String key = app + ":" + resource;
        metricCache.computeIfAbsent(key, k -> new ArrayList<>()).add(metric);
        
        // 更新应用最后心跳时间
        AppInfo appInfo = appCache.get(app);
        if (appInfo != null) {
            appInfo.setLastFetch(System.currentTimeMillis());
        }
    }

    /**
     * 获取实时指标
     *
     * @param app      应用名称
     * @param resource 资源名称
     * @return 最新的监控指标
     */
    public MetricDTO getRealtimeMetric(String app, String resource) {
        List<MetricDTO> metrics = getMetrics(app, resource, 
                System.currentTimeMillis() - 60000, // 最近1分钟
                System.currentTimeMillis());
        
        if (metrics.isEmpty()) {
            return MetricDTO.builder()
                    .app(app)
                    .resource(resource)
                    .timestamp(TimeUtil.currentTimeMillis())
                    .passQps(0L)
                    .successQps(0L)
                    .blockQps(0L)
                    .exceptionQps(0L)
                    .rt(0.0)
                    .occupiedPassQps(0L)
                    .build();
        }
        
        return metrics.getLast();
    }

    /**
     * 清理过期数据
     */
    public void cleanExpiredData() {
        long expireTime = System.currentTimeMillis() - 3600000; // 1小时前
        
        metricCache.forEach((key, nodes) -> nodes.removeIf(node -> node.getTimestamp() < expireTime));
        
        log.info("清理过期监控数据完成");
    }
}

