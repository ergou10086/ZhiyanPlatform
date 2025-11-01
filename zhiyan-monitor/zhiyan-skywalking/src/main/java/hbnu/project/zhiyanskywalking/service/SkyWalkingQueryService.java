package hbnu.project.zhiyanskywalking.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import hbnu.project.zhiyanskywalking.config.SkyWalkingProperties;
import hbnu.project.zhiyanskywalking.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SkyWalking 查询服务
 * 通过 GraphQL API 查询追踪数据
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SkyWalkingQueryService {

    private final SkyWalkingProperties properties;

    /**
     * 获取所有服务列表
     *
     * @return 服务列表
     */
    public List<ServiceInfo> getServices() {
        String query = "{"
                + "  services: getAllServices(duration: {start: \"" + getStartTime() + "\", end: \"" + getEndTime() + "\", step: MINUTE}) {"
                + "    key: id"
                + "    label: name"
                + "    group"
                + "  }"
                + "}";

        JSONObject result = executeGraphQL(query);
        List<ServiceInfo> services = new ArrayList<>();

        if (result != null && result.containsKey("data")) {
            JSONObject data = result.getJSONObject("data");
            if (data.containsKey("services")) {
                JSONArray servicesArray = data.getJSONArray("services");
                for (int i = 0; i < servicesArray.size(); i++) {
                    JSONObject serviceObj = servicesArray.getJSONObject(i);
                    ServiceInfo service = ServiceInfo.builder()
                            .id(serviceObj.getStr("key"))
                            .name(serviceObj.getStr("label"))
                            .layer(serviceObj.getStr("group"))
                            .normal(true)
                            .build();
                    services.add(service);
                }
            }
        }

        log.info("获取服务列表成功，共{}个服务", services.size());
        return services;
    }

    /**
     * 获取追踪列表
     *
     * @param serviceId    服务ID
     * @param endpointName 端点名称（可选）
     * @param pageNum      页码
     * @param pageSize     每页大小
     * @return 追踪列表
     */
    public List<TraceInfo> getTraces(String serviceId, String endpointName, int pageNum, int pageSize) {
        String traceState = "ALL";  // ALL, SUCCESS, ERROR
        String queryOrder = "BY_DURATION";  // BY_DURATION, BY_START_TIME

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("{");
        queryBuilder.append("  queryBasicTraces(condition: {");
        queryBuilder.append("    serviceId: \"").append(serviceId).append("\",");
        if (endpointName != null && !endpointName.isEmpty()) {
            queryBuilder.append("    endpointName: \"").append(endpointName).append("\",");
        }
        queryBuilder.append("    traceState: ").append(traceState).append(",");
        queryBuilder.append("    queryOrder: ").append(queryOrder).append(",");
        queryBuilder.append("    queryDuration: {start: \"").append(getStartTime()).append("\", end: \"").append(getEndTime()).append("\", step: MINUTE},");
        queryBuilder.append("    paging: {pageNum: ").append(pageNum).append(", pageSize: ").append(pageSize).append(", needTotal: true}");
        queryBuilder.append("  }) {");
        queryBuilder.append("    traces {");
        queryBuilder.append("      key: segmentId");
        queryBuilder.append("      endpointNames");
        queryBuilder.append("      duration");
        queryBuilder.append("      start");
        queryBuilder.append("      isError");
        queryBuilder.append("      traceIds");
        queryBuilder.append("    }");
        queryBuilder.append("    total");
        queryBuilder.append("  }");
        queryBuilder.append("}");

        JSONObject result = executeGraphQL(queryBuilder.toString());
        List<TraceInfo> traces = new ArrayList<>();

        if (result != null && result.containsKey("data")) {
            JSONObject data = result.getJSONObject("data");
            if (data.containsKey("queryBasicTraces")) {
                JSONObject queryBasicTraces = data.getJSONObject("queryBasicTraces");
                JSONArray tracesArray = queryBasicTraces.getJSONArray("traces");

                for (int i = 0; i < tracesArray.size(); i++) {
                    JSONObject traceObj = tracesArray.getJSONObject(i);
                    JSONArray traceIds = traceObj.getJSONArray("traceIds");
                    String traceId = traceIds != null && traceIds.size() > 0 ? traceIds.getStr(0) : "";

                    TraceInfo trace = TraceInfo.builder()
                            .traceId(traceId)
                            .serviceName(serviceId)
                            .endpointName(traceObj.getStr("endpointNames"))
                            .duration(traceObj.getLong("duration"))
                            .startTime(traceObj.getLong("start"))
                            .isError(traceObj.getBool("isError"))
                            .build();
                    traces.add(trace);
                }
            }
        }

        log.info("获取追踪列表成功，共{}条记录", traces.size());
        return traces;
    }

    /**
     * 获取服务拓扑图
     *
     * @return 服务拓扑
     */
    public ServiceTopology getServiceTopology() {
        String query = "{"
                + "  topology: getGlobalTopology(duration: {start: \"" + getStartTime() + "\", end: \"" + getEndTime() + "\", step: MINUTE}) {"
                + "    nodes {"
                + "      id"
                + "      name"
                + "      type"
                + "      isReal"
                + "    }"
                + "    calls {"
                + "      source"
                + "      target"
                + "      id"
                + "      detectPoints"
                + "    }"
                + "  }"
                + "}";

        JSONObject result = executeGraphQL(query);
        ServiceTopology topology = new ServiceTopology();
        topology.setNodes(new ArrayList<>());
        topology.setCalls(new ArrayList<>());

        if (result != null && result.containsKey("data")) {
            JSONObject data = result.getJSONObject("data");
            if (data.containsKey("topology")) {
                JSONObject topologyObj = data.getJSONObject("topology");

                // 解析节点
                if (topologyObj.containsKey("nodes")) {
                    JSONArray nodesArray = topologyObj.getJSONArray("nodes");
                    for (int i = 0; i < nodesArray.size(); i++) {
                        JSONObject nodeObj = nodesArray.getJSONObject(i);
                        ServiceTopology.TopologyNode node = ServiceTopology.TopologyNode.builder()
                                .id(nodeObj.getStr("id"))
                                .name(nodeObj.getStr("name"))
                                .type(nodeObj.getStr("type"))
                                .isReal(nodeObj.getBool("isReal"))
                                .build();
                        topology.getNodes().add(node);
                    }
                }

                // 解析调用关系
                if (topologyObj.containsKey("calls")) {
                    JSONArray callsArray = topologyObj.getJSONArray("calls");
                    for (int i = 0; i < callsArray.size(); i++) {
                        JSONObject callObj = callsArray.getJSONObject(i);
                        ServiceTopology.TopologyCall call = ServiceTopology.TopologyCall.builder()
                                .source(callObj.getStr("source"))
                                .target(callObj.getStr("target"))
                                .detectPoint(callObj.getStr("detectPoints"))
                                .callCount(0L)  // SkyWalking 需要额外查询
                                .build();
                        topology.getCalls().add(call);
                    }
                }
            }
        }

        log.info("获取服务拓扑成功，节点数：{}，调用关系数：{}", topology.getNodes().size(), topology.getCalls().size());
        return topology;
    }

    /**
     * 获取服务性能指标
     *
     * @param serviceId 服务ID
     * @return 性能指标
     */
    public ServiceMetric getServiceMetrics(String serviceId) {
        String query = "{"
                + "  sla: readMetricsValue(condition: {name: \"service_sla\", entity: {scope: Service, serviceName: \"" + serviceId + "\", normal: true}}, duration: {start: \"" + getStartTime() + "\", end: \"" + getEndTime() + "\", step: MINUTE})"
                + "  cpm: readMetricsValue(condition: {name: \"service_cpm\", entity: {scope: Service, serviceName: \"" + serviceId + "\", normal: true}}, duration: {start: \"" + getStartTime() + "\", end: \"" + getEndTime() + "\", step: MINUTE})"
                + "  resp: readMetricsValue(condition: {name: \"service_resp_time\", entity: {scope: Service, serviceName: \"" + serviceId + "\", normal: true}}, duration: {start: \"" + getStartTime() + "\", end: \"" + getEndTime() + "\", step: MINUTE})"
                + "  apdex: readMetricsValue(condition: {name: \"service_apdex\", entity: {scope: Service, serviceName: \"" + serviceId + "\", normal: true}}, duration: {start: \"" + getStartTime() + "\", end: \"" + getEndTime() + "\", step: MINUTE})"
                + "}";

        JSONObject result = executeGraphQL(query);
        ServiceMetric.ServiceMetricBuilder builder = ServiceMetric.builder()
                .serviceName(serviceId)
                .timestamp(System.currentTimeMillis());

        if (result != null && result.containsKey("data")) {
            JSONObject data = result.getJSONObject("data");
            builder.successRate(data.getDouble("sla", 0.0) / 100.0);
            builder.cpm(data.getLong("cpm", 0L));
            builder.avgResponseTime(data.getLong("resp", 0L));
            builder.apdex(data.getDouble("apdex", 0.0) / 10000.0);
            builder.errorCount(0L);  // 需要额外查询
        }

        ServiceMetric metric = builder.build();
        log.info("获取服务指标成功: {}", serviceId);
        return metric;
    }

    /**
     * 执行 GraphQL 查询
     *
     * @param query GraphQL 查询语句
     * @return 查询结果
     */
    private JSONObject executeGraphQL(String query) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("query", query);

            HttpResponse response = HttpRequest.post(properties.getOap().getGraphqlEndpoint())
                    .header("Content-Type", "application/json")
                    .body(JSONUtil.toJsonStr(body))
                    .timeout(10000)
                    .execute();

            if (response.isOk()) {
                String responseBody = response.body();
                log.debug("GraphQL 响应: {}", responseBody);
                return JSONUtil.parseObj(responseBody);
            } else {
                log.error("GraphQL 查询失败: HTTP {}, {}", response.getStatus(), response.body());
            }
        } catch (Exception e) {
            log.error("执行 GraphQL 查询异常", e);
        }
        return null;
    }

    /**
     * 获取开始时间（最近1小时）
     */
    private String getStartTime() {
        long now = System.currentTimeMillis();
        long start = now - 3600000; // 1小时前
        return String.valueOf(start);
    }

    /**
     * 获取结束时间（当前时间）
     */
    private String getEndTime() {
        return String.valueOf(System.currentTimeMillis());
    }
}

