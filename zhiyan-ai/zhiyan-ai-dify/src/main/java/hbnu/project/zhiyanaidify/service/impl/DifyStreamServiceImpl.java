package hbnu.project.zhiyanaidify.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hbnu.project.zhiyanaidify.client.DifyApiClient;
import hbnu.project.zhiyanaidify.config.properties.DifyProperties;
import hbnu.project.zhiyanaidify.exception.DifyApiException;
import hbnu.project.zhiyanaidify.model.request.ChatRequest;
import hbnu.project.zhiyanaidify.model.request.StopChatRequest;
import hbnu.project.zhiyanaidify.model.response.StopChatResponse;
import hbnu.project.zhiyanaidify.service.DifyStreamService;
import hbnu.project.zhiyancommonbasic.exception.ServiceException;
import hbnu.project.zhiyancommonsse.dto.DifyStreamMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * Dify 流式调用服务
 * 使用 WebClient 实现 SSE 流式响应（Chatflow）
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
    private final DifyApiClient difyApiClient;

    /**
     * 解析 Dify 流式数据
     * Dify 返回格式: data: {"event": "...", ...}
     */
    private DifyStreamMessage parseDifyStreamData(String data) {
        try {
            log.debug("[Dify 解析] 开始解析数据: {}", data);
            
            String jsonData;
            
            // 格式1: 纯 JSON 格式（Workflow 模式）: {"event":"..."}
            if (data.trim().startsWith("{")) {
                jsonData = data.trim();
                log.debug("[Dify 解析] 检测到纯 JSON 格式");
            }
            // 格式2: SSE 格式: "data: {...}"
            else if (data.startsWith("data: ")) {
                jsonData = data.substring(6).trim();
                log.debug("[Dify 解析] 检测到 SSE 格式");
            }
            // 格式3: 事件行（SSE）: "event: message"
            else if (data.startsWith("event:")) {
                log.debug("[Dify 解析] 跳过事件行: {}", data);
                return null;
            }
            // 其他格式：空行或未知
            else {
                log.debug("[Dify 解析] 跳过非数据行: {}", data);
                return null;
            }

            // 跳过心跳包或空数据
            if (jsonData.isEmpty() || "[DONE]".equals(jsonData)) {
                log.debug("[Dify 解析] 跳过心跳包或空数据");
                return null;
            }

            log.debug("[Dify 解析] JSON 数据: {}", jsonData);
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

            DifyStreamMessage result = builder.build();
            log.debug("[Dify 解析] 解析成功，事件类型: {}", result.getEvent());
            return result;

        } catch (Exception e) {
            log.error("[Dify 解析] 解析流式数据失败: data={}", data, e);
            return DifyStreamMessage.builder()
                    .event("error")
                    .errorMessage("解析数据失败: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
    }


    /**
     * 调用 Chatflow 并返回流式响应（适用于聊天流应用）
     *
     * @param request 聊天请求
     * @return 流式消息
     */
    @Override
    public Flux<DifyStreamMessage> callChatflowStream(ChatRequest request) {
        log.info("🔄 [Dify Chatflow] 开始流式调用: query={}, conversationId={}, user={}",
                request.getQuery(), request.getConversationId(), request.getUser());

        // 打印完整的请求体用于调试
        try {
            String requestJson = objectMapper.writeValueAsString(request);
            log.info("[Dify Chatflow] 请求体: {}", requestJson);
        } catch (Exception e) {
            log.warn("[Dify Chatflow] 无法序列化请求体", e);
        }

        // 使用已配置好的 webClientBuilder（带有 DNS 解析器修复）
        WebClient webClient = webClientBuilder
                .baseUrl(difyProperties.getApiUrl())
                .defaultHeader("Authorization", "Bearer " + difyProperties.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "text/event-stream")  // ✅ 关键：告诉 Dify 我们要 SSE 流式响应
                .build();

        // 确保是流式模式
        request.setResponseMode("streaming");

        log.info("📡 [Dify Chatflow] 发送请求到: {}/chat-messages", difyProperties.getApiUrl());

        return webClient.post()
                .uri("/chat-messages")
                .bodyValue(request)
                .retrieve()
                .onStatus(
                    status -> !status.is2xxSuccessful(),
                    response -> {
                        log.error("❌ [Dify Chatflow] HTTP 错误: status={}", response.statusCode());
                        return response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("[Dify Chatflow] 错误响应体: {}", body);
                                    String errorMsg = "Dify API 返回错误 " + response.statusCode() + ": " + body;
                                    return Mono.error(new DifyApiException(errorMsg));
                                });
                    }
                )
                .bodyToFlux(String.class)
                // ⭐⭐⭐ 添加重试机制：网络错误时自动重试 3 次
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2))
                        .filter(throwable -> {
                            // 只对网络相关错误重试（DNS 解析、连接超时等）
                            boolean shouldRetry = throwable instanceof java.net.UnknownHostException
                                    || throwable instanceof java.net.ConnectException
                                    || throwable instanceof java.util.concurrent.TimeoutException
                                    || (throwable instanceof org.springframework.web.reactive.function.client.WebClientRequestException
                                        && throwable.getMessage().contains("resolve"));
                            
                            if (shouldRetry) {
                                log.warn("⚠️ [Dify Chatflow] 网络错误，2秒后重试: {}", throwable.getMessage());
                            }
                            return shouldRetry;
                        })
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                            log.error("❌ [Dify Chatflow] 重试 3 次后仍然失败");
                            return new DifyApiException("连接 Dify 服务失败，已重试 3 次: " + retrySignal.failure().getMessage());
                        })
                )
                .doOnSubscribe(sub -> log.info("🔄 [Dify Chatflow] 开始订阅流式响应"))
                // ⭐ 打印 Dify 返回的原始数据（用于调试流式传输）
                .doOnNext(rawData -> {
                    String preview = rawData.length() > 100 ? rawData.substring(0, 100) + "..." : rawData;
                    log.debug("📦 [Dify Chatflow] 收到原始数据: {}", preview);
                })
                // 使用 handle 而不是 map + filter，因为 map 不允许返回 null
                .<DifyStreamMessage>handle((data, sink) -> {
                    DifyStreamMessage message = parseDifyStreamData(data);
                    if (message != null) {
                        String dataPreview = message.getData() != null && message.getData().length() > 50 
                            ? message.getData().substring(0, 50) + "..." 
                            : message.getData();
                        log.debug("✅ [Dify Chatflow] 解析成功: event={}, data={}", 
                                message.getEvent(), dataPreview);
                        sink.next(message);  // 只有非 null 时才发出
                    } else {
                        log.debug("⚠️ [Dify Chatflow] 解析返回 null，原始数据: {}", data);
                    }
                })
                // ⭐⭐⭐ 添加背压策略，确保流式传输不会因缓冲而延迟
                .onBackpressureBuffer(100,  // 缓冲区最多100条消息
                    dropped -> log.warn("⚠️ [Dify Chatflow] 缓冲区满，丢弃消息"))
                .doOnNext(msg -> log.debug("📤 [Dify Chatflow] 向上游发送消息: event={}", msg.getEvent()))
                .doOnError(error -> {
                    log.error("❌ [Dify Chatflow] 调用错误", error);
                    
                    // 如果是 WebClientResponseException，打印响应体
                    if (error instanceof WebClientResponseException ex) {
                        log.error("[Dify Chatflow] 错误响应: status={}, body={}",
                                ex.getStatusCode(), ex.getResponseBodyAsString());
                    }
                    
                    // 如果是网络错误，提供更友好的错误提示
                    if (error instanceof java.net.UnknownHostException) {
                        log.error("[Dify Chatflow] DNS 解析失败: {}", error.getMessage());
                        log.error("[Dify Chatflow] 请检查：1) 网络连接；2) DNS 配置；3) Dify 服务地址是否正确");
                    } else if (error.getMessage() != null && error.getMessage().contains("resolve")) {
                        log.error("[Dify Chatflow] DNS 解析超时，已重试 3 次均失败");
                        log.error("[Dify Chatflow] 建议：1) 检查防火墙设置；2) 尝试使用 IP 地址替代域名；3) 检查 DNS 服务器配置");
                    }
                })
                .doOnComplete(() -> log.info("🏁 [Dify Chatflow] 调用完成"));
    }


    /**
     * 停止流式响应
     *
     * @param taskId 任务 ID
     * @param userId 用户 ID
     * @return 是否停止成功
     */
    @Override
    public boolean stopChatStream(String taskId, Long userId) {
        try{
            String userIdentifier = String.valueOf(userId);
            String apiKey = "Bearer " + difyProperties.getApiKey();

            log.info("[停止流式响应] taskId={}, userId={}", taskId, userId);

            StopChatRequest request = StopChatRequest.builder()
                    .user(userIdentifier)
                    .build();

            StopChatResponse response = difyApiClient.stopChatMessage(apiKey, taskId, request);

            boolean success = "success".equalsIgnoreCase(response.getResult());
            log.info("[停止流式响应] taskId={}, 结果={}", taskId, success);

            return success;
        }catch (DifyApiException | ServiceException e){
            log.error("[停止流式响应] 失败: taskId={}, error={}", taskId, e.getMessage(), e);
            throw new DifyApiException("停止流式响应失败: " + e.getMessage(), e);
        }
    }
}
