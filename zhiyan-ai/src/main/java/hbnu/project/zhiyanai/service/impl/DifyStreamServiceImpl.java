package hbnu.project.zhiyanai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hbnu.project.zhiyanai.config.properties.DifyProperties;
import hbnu.project.zhiyanai.model.dto.ChatRequest;
import hbnu.project.zhiyanai.model.dto.WorkflowChatRequest;
import hbnu.project.zhiyanai.service.DifyStreamService;
import hbnu.project.zhiyancommonsse.dto.DifyStreamMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

/**
 * Dify 流式调用服务
 * 使用 WebClient 实现 SSE 流式响应
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DifyStreamServiceImpl implements DifyStreamService {

    private final DifyProperties difyProperties;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    /**
     * 流式发送聊天消息
     *
     * @param request 聊天请求
     * @return SSE 事件流
     */
    @Override
    public Flux<String> sendChatMessageStream(ChatRequest request) {
        WebClient webClient = webClientBuilder
                .baseUrl(difyProperties.getApiUrl())
                .defaultHeader("Authorization", "Bearer " + difyProperties.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();

        // 设置流式模式
        request.setResponseMode("streaming");

        return webClient.post()
                .uri("/chat-messages")
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnNext(data -> log.debug("收到流式数据: {}", data))
                .doOnError(error -> log.error("流式调用错误: ", error))
                .doOnComplete(() -> log.info("流式调用完成"));
    }

    /**
     * 调用工作流并返回流式响应
     *
     * @param request 工作流请求
     * @return 流式消息
     */
    @Override
    public Flux<DifyStreamMessage> callWorkflowStream(WorkflowChatRequest request) {
        log.info("[Dify 工作流] 开始流式调用: query={}, conversationId={}",
                request.getQuery(), request.getConversationId());

        WebClient webClient = webClientBuilder
                .baseUrl(difyProperties.getApiUrl())
                .defaultHeader("Authorization", "Bearer " + difyProperties.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();

        // 确保是流式模式
        request.setResponseMode("streaming");

        return webClient.post()
                .uri("/workflows/run")
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(String.class)
                .map(this::parseDifyStreamData)
                .filter(msg -> msg != null)
                .doOnNext(msg -> log.debug("[Dify 工作流] 收到消息: event={}", msg.getEvent()))
                .doOnError(error -> log.error("[Dify 工作流] 调用错误", error))
                .doOnComplete(() -> log.info("[Dify 工作流] 调用完成"));
    }

    /**
     * 解析 Dify 流式数据
     * Dify 返回格式: data: {"event": "...", ...}
     */
    private DifyStreamMessage parseDifyStreamData(String data) {
        try {
            // Dify SSE 格式: "data: {...}"
            if (data.startsWith("data: ")) {
                String jsonData = data.substring(6).trim();

                // 跳过心跳包
                if ("[DONE]".equals(jsonData) || jsonData.isEmpty()) {
                    return null;
                }

                // 解析 JSON
                JsonNode jsonNode = objectMapper.readTree(jsonData);

                DifyStreamMessage.DifyStreamMessageBuilder builder = DifyStreamMessage.builder()
                        .timestamp(System.currentTimeMillis());

                // 解析基本字段
                if (jsonNode.has("event")) {
                    builder.event(jsonNode.get("event").asText());
                }
                if (jsonNode.has("conversation_id")) {
                    builder.conversationId(jsonNode.get("conversation_id").asText());
                }
                if (jsonNode.has("message_id")) {
                    builder.messageId(jsonNode.get("message_id").asText());
                }
                if (jsonNode.has("task_id")) {
                    builder.taskId(jsonNode.get("task_id").asText());
                }
                if (jsonNode.has("workflow_run_id")) {
                    builder.workflowRunId(jsonNode.get("workflow_run_id").asText());
                }

                // 解析节点信息
                if (jsonNode.has("node_id")) {
                    builder.nodeId(jsonNode.get("node_id").asText());
                }
                if (jsonNode.has("node_type")) {
                    builder.nodeType(jsonNode.get("node_type").asText());
                }
                if (jsonNode.has("node_execution_id")) {
                    builder.nodeExecutionId(jsonNode.get("node_execution_id").asText());
                }

                // 解析数据内容
                if (jsonNode.has("data")) {
                    JsonNode dataNode = jsonNode.get("data");
                    if (dataNode.isTextual()) {
                        builder.data(dataNode.asText());
                    } else {
                        builder.data(dataNode.toString());
                    }
                }

                // 解析答案（用于 answer 事件）
                if (jsonNode.has("answer")) {
                    builder.data(jsonNode.get("answer").asText());
                }

                // 解析错误信息
                if (jsonNode.has("error")) {
                    builder.errorMessage(jsonNode.get("error").asText());
                }
                if (jsonNode.has("error_code")) {
                    builder.errorCode(jsonNode.get("error_code").asText());
                }

                if (jsonNode.has("created_at")) {
                    builder.createdAt(jsonNode.get("created_at").asLong());
                }

                return builder.build();
            }

            return null;

        } catch (Exception e) {
            log.error("[Dify 工作流] 解析流式数据失败: data={}", data, e);
            return DifyStreamMessage.builder()
                    .event("error")
                    .errorMessage("解析数据失败: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
    }

    /**
     * 提取文本内容（用于简化的流式输出）
     */
    @Override
    public Flux<String> callWorkflowStreamSimple(WorkflowChatRequest request) {
        return callWorkflowStream(request)
                .filter(msg -> "message".equals(msg.getEvent()) ||
                        "chunk".equals(msg.getEvent()) ||
                        "answer".equals(msg.getEvent()))
                .map(msg -> msg.getData() != null ? msg.getData() : "");
    }
}
