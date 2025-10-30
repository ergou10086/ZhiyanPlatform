package hbnu.project.zhiyanaicoze.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hbnu.project.zhiyanaicoze.config.properties.CozeProperties;
import hbnu.project.zhiyanaicoze.exception.CozeApiException;
import hbnu.project.zhiyanaicoze.model.dto.CozeStreamMessage;
import hbnu.project.zhiyanaicoze.model.request.CozeChatRequest;
import hbnu.project.zhiyanaicoze.model.response.CozeChatResponse;
import hbnu.project.zhiyanaicoze.service.CozeStreamService;
import hbnu.project.zhiyancommonbasic.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Coze 流式调用服务实现
 * 调用此接口发起一次对话，支持添加上下文和流式响应。
 * 会话、对话和消息的概念说明，可参考基础概念。
 * 接口说明
 * 发起对话接口用于向指定智能体发起一次对话，支持在对话时添加对话的上下文消息，以便智能体基于历史消息做出合理的回复。开发者可以按需选择响应方式，即流式或非流式响应，响应方式决定了开发者获取智能体回复的方式。关于获取智能体回复的详细说明可参考通过对话接口获取智能体回复。
 * 流式响应：智能体在生成回复的同时，将回复消息以数据流的形式逐条发送给客户端。处理结束后，服务端会返回一条完整的智能体回复。详细说明可参考流式响应。
 * 非流式响应：无论对话是否处理完毕，立即发送响应消息。开发者可以通过接口查看对话详情确认本次对话处理结束后，再调用查看对话消息详情接口查看模型回复等完整响应内容。详细说明可参考非流式响应。
 * 创建会话 API 和发起对话 API 的区别如下：
 * 创建会话：
 * 主要用于初始化一个新的会话环境。
 * 一个会话是Bot和用户之间的一段问答交互，可以包含多条消息。
 * 创建会话时，可以选择携带初始的消息内容。
 * 发起对话：
 * 用于在已经存在的会话中发起一次对话。
 * 支持添加上下文和流式响应。
 * 可以基于历史消息进行上下文关联，提供更符合语境的回复。
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CozeStreamServiceImpl implements CozeStreamService {

    private final CozeProperties cozeProperties;
    private final WebClient cozeWebClient;
    private final ObjectMapper objectMapper;

    /**
     * 调用 Coze 进行流式对话
     *
     * @param request 聊天请求
     * @return 流式消息
     */
    @Override
    public Flux<CozeStreamMessage> chatStream(CozeChatRequest request) {
        log.info("[Coze Chat] 开始流式调用: botId={}, userId={}, conversationId={}",
                request.getBotId(), request.getUserId(), request.getConversationId());

        // 打印完整的请求体用于调试
        try {
            String requestJson = objectMapper.writeValueAsString(request);
            log.info("[Coze Chat] 请求体: {}", requestJson);
        } catch (Exception e) {
            log.warn("[Coze Chat] 无法序列化请求体", e);
        }

        // 确保是流式模式
        request.setStream(true);

        log.info("[Coze Chat] 发送请求到: {}/v3/chat", cozeProperties.getApiUrl());

        return cozeWebClient.post()
                .uri("/v3/chat")
                .bodyValue(request)
                .retrieve()
                .onStatus(
                        status -> !status.is2xxSuccessful(),
                        response -> {
                            log.error("[Coze Chat] HTTP 错误: status={}", response.statusCode());
                            return response.bodyToMono(String.class)
                                    .doOnNext(body -> log.error("[Coze Chat] 错误响应体: {}", body))
                                    .then(Mono.error(new CozeApiException("Coze API 返回错误: " + response.statusCode())));
                        }
                )
                .bodyToFlux(String.class)
                .doOnSubscribe(sub -> log.info("[Coze Chat] 开始订阅流式响应"))
                .doOnComplete(() -> log.info("[Coze Chat] 流式响应完成"))
                // 打印 Coze 返回的原始数据（用于调试）
                .doOnNext(rawData -> log.debug("[Coze Chat] 收到原始数据: {}", rawData))
                // 解析流式数据
                .<CozeStreamMessage>handle((data, sink) -> {
                    CozeStreamMessage message = parseCozeStreamData(data);
                    if (message != null) {
                        log.debug("[Coze Chat] 解析成功: event={}, content={}",
                                message.getEvent(), message.getContent());
                        sink.next(message);
                    }
                })
                .doOnError(error -> {
                    log.error("[Coze Chat] 调用错误", error);
                    if (error instanceof WebClientResponseException ex) {
                        log.error("[Coze Chat] 错误响应: status={}, body={}",
                                ex.getStatusCode(), ex.getResponseBodyAsString());
                    }
                })
                .doOnComplete(() -> log.info("[Coze Chat] 调用完成"));
    }


    /**
     * 查看对话的详细信息。
     * 在非流式会话场景中，调用发起对话接口后，可以先轮询此 API 确认本轮对话已结束（status=completed），再调用接口查看对话消息详情查看本轮对话的模型回复。
     *
     * @param conversationId 对话ID
     * @param chatId         聊天ID
     * @return 对话详情
     */
    @Override
    public CozeChatResponse getChatDetail(String conversationId, String chatId) {
        log.info("[Coze Chat] 查询对话详情: conversationId={}, chatId={}", conversationId, chatId);

        try{
            return cozeWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("v3/chat/retrieve")
                            .queryParam("conversation_id", conversationId)
                            .queryParam("chat_id", chatId)
                            .build())
                    .retrieve()
                    .bodyToMono(CozeChatResponse.class)
                    .doOnSuccess(response -> log.info("[Coze Chat] 查询成功: status={}", response.getStatus()))
                    .doOnError(error -> log.error("[Coze Chat] 查询失败", error))
                    .block();
        }catch (ServiceException | CozeApiException e){
            log.error("[Coze Chat] 查询对话详情失败", e);
            throw new ServiceException("查询对话详情失败: " + e.getMessage());
        }
    }



    /**
     * 解析 Coze 流式数据
     * Coze 返回格式: event: xxx\ndata: {...}\n\n
     */
    private CozeStreamMessage parseCozeStreamData(String data) {
        try {
            log.debug("[Coze 解析] 开始解析数据: {}", data);

            String jsonData;

            // 格式1: SSE 格式: "data: {...}"
            if (data.startsWith("data:")) {
                jsonData = data.substring(5).trim();
                log.debug("[Coze 解析] 检测到 SSE data 格式");
            }
            // 格式2: 事件行: "event: xxx"
            else if (data.startsWith("event:")) {
                log.debug("[Coze 解析] 跳过事件行: {}", data);
                return null;
            }
            // 格式3: 纯 JSON 格式
            else if (data.trim().startsWith("{")) {
                jsonData = data.trim();
                log.debug("[Coze 解析] 检测到纯 JSON 格式");
            }
            // 其他格式：空行或未知
            else {
                log.debug("[Coze 解析] 跳过非数据行: {}", data);
                return null;
            }

            // 跳过心跳包或结束标记
            if (jsonData.isEmpty() || "[DONE]".equals(jsonData)) {
                log.debug("[Coze 解析] 检测到结束标记");
                return CozeStreamMessage.builder()
                        .event("done")
                        .timestamp(System.currentTimeMillis())
                        .build();
            }

            log.debug("[Coze 解析] JSON 数据: {}", jsonData);

            // 解析 JSON
            JsonNode jsonNode = objectMapper.readTree(jsonData);

            CozeStreamMessage.CozeStreamMessageBuilder builder = CozeStreamMessage.builder()
                    .timestamp(System.currentTimeMillis())
                    .rawData(jsonData);

            // 解析基本字段
            if (jsonNode.has("event")) {
                builder.event(jsonNode.get("event").asText());
            }
            if (jsonNode.has("conversation_id")) {
                builder.conversationId(jsonNode.get("conversation_id").asText());
            }
            if (jsonNode.has("chat_id")) {
                builder.chatId(jsonNode.get("chat_id").asText());
            }
            if (jsonNode.has("id")) {
                builder.id(jsonNode.get("id").asText());
            }
            if (jsonNode.has("bot_id")) {
                builder.botId(jsonNode.get("bot_id").asText());
            }
            if (jsonNode.has("status")) {
                builder.status(jsonNode.get("status").asText());
            }
            if (jsonNode.has("created_at")) {
                builder.createdAt(jsonNode.get("created_at").asLong());
            }
            if (jsonNode.has("completed_at")) {
                builder.completedAt(jsonNode.get("completed_at").asLong());
            }

            // 解析消息内容（根据不同事件类型）
            if (jsonNode.has("content")) {
                builder.content(jsonNode.get("content").asText());
            }

            if (jsonNode.has("role")) {
                builder.role(jsonNode.get("role").asText());
            }

            if (jsonNode.has("type")) {
                builder.type(jsonNode.get("type").asText());
            }

            // 解析 usage（Token 使用情况）
            if (jsonNode.has("usage")) {
                JsonNode usageNode = jsonNode.get("usage");
                CozeStreamMessage.Usage usage = new CozeStreamMessage.Usage();

                if (usageNode.has("input_tokens")) {
                    usage.setInputTokens(usageNode.get("input_tokens").asInt());
                }
                if (usageNode.has("output_tokens")) {
                    usage.setOutputTokens(usageNode.get("output_tokens").asInt());
                }
                if (usageNode.has("total_tokens")) {
                    usage.setTotalTokens(usageNode.get("total_tokens").asInt());
                }

                builder.usage(usage);
            }

            // 解析错误信息
            if (jsonNode.has("error")) {
                JsonNode errorNode = jsonNode.get("error");
                if (errorNode.has("message")) {
                    builder.errorMessage(errorNode.get("message").asText());
                }
                if (errorNode.has("code")) {
                    builder.errorCode(errorNode.get("code").asText());
                }
            }

//            // 解析 metadata
//            if (jsonNode.has("metadata")) {
//                builder.metadata(objectMapper.convertValue(
//                        jsonNode.get("metadata"),
//                        Map.class
//                ));
//            }

            // 解析 metadata
            if (jsonNode.has("metadata")) {
                // 使用 TypeReference 明确指定 Map<String, Object> 类型
                Map<String, Object> metadata = objectMapper.convertValue(
                        jsonNode.get("metadata"),
                        new TypeReference<>() {
                        }
                );
                builder.metadata(metadata);
            }

            CozeStreamMessage result = builder.build();
            log.debug("[Coze 解析] 解析成功，事件类型: {}, 内容长度: {}",
                    result.getEvent(),
                    result.getContent() != null ? result.getContent().length() : 0);
            return result;

        } catch (Exception e) {
            log.error("[Coze 解析] 解析流式数据失败: data={}", data, e);
            return CozeStreamMessage.builder()
                    .event("error")
                    .errorMessage("解析数据失败: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
    }
}
