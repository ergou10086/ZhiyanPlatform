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
                .doOnNext(rawData -> log.info("[Coze Chat] 收到原始行: {}", rawData))
                // 过滤空行
                .filter(line -> !line.trim().isEmpty())
                // 直接解析每一行（Coze API 每行都是一个完整的 JSON 对象）
                .<CozeStreamMessage>handle((line, sink) -> {
                    log.debug("[Coze Chat] 开始解析行: {}", line.substring(0, Math.min(100, line.length())));
                    CozeStreamMessage message = parseCozeStreamLine(line);
                    if (message != null) {
                        log.info("[Coze Chat] 解析成功: event={}, content={}, type={}",
                                message.getEvent(), 
                                message.getContent() != null ? message.getContent().substring(0, Math.min(20, message.getContent().length())) : null,
                                message.getType());
                        sink.next(message);
                    } else {
                        log.debug("[Coze Chat] 解析结果为空，跳过该行");
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
     * 解析单行 JSON 数据（Coze API 新格式）
     * 每行都是一个完整的 JSON 对象
     *
     * @param line 单行 JSON 数据
     * @return 解析后的消息对象
     */
    private CozeStreamMessage parseCozeStreamLine(String line) {
        try {
            String trimmedLine = line.trim();
            
            // 跳过空行
            if (trimmedLine.isEmpty()) {
                return null;
            }
            
            // 检测结束标记
            if ("[DONE]".equals(trimmedLine) || "\"[DONE]\"".equals(trimmedLine)) {
                log.info("[Coze 解析] 检测到结束标记");
                return CozeStreamMessage.builder()
                        .event("done")
                        .timestamp(System.currentTimeMillis())
                        .build();
            }
            
            // 检查是否是有效的 JSON
            if (!trimmedLine.startsWith("{") && !trimmedLine.startsWith("[")) {
                log.warn("[Coze 解析] 不是有效的JSON格式，跳过: {}", trimmedLine.substring(0, Math.min(50, trimmedLine.length())));
                return null;
            }
            
            // 解析 JSON
            JsonNode jsonNode = objectMapper.readTree(trimmedLine);
            
            // 构建消息对象
            CozeStreamMessage.CozeStreamMessageBuilder builder = CozeStreamMessage.builder()
                    .timestamp(System.currentTimeMillis())
                    .rawData(trimmedLine);
            
            // 解析基本字段
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
                String status = jsonNode.get("status").asText();
                builder.status(status);
            }
            if (jsonNode.has("created_at")) {
                builder.createdAt(jsonNode.get("created_at").asLong());
            }
            if (jsonNode.has("completed_at")) {
                builder.completedAt(jsonNode.get("completed_at").asLong());
            }
            
            // 解析消息内容
            if (jsonNode.has("content")) {
                builder.content(jsonNode.get("content").asText());
            }
            if (jsonNode.has("role")) {
                builder.role(jsonNode.get("role").asText());
            }
            
            // 先解析 type 字段（type 优先级高于 status）
            String messageType = null;
            if (jsonNode.has("type")) {
                messageType = jsonNode.get("type").asText();
                builder.type(messageType);
            }
            
            // 根据 type 和 status 设置事件类型
            // type 字段的优先级更高，因为它更具体地描述了消息的性质
            if (messageType != null) {
                if ("answer".equals(messageType)) {
                    // answer 类型的消息是流式增量文本，用于打字机效果（最重要！）
                    builder.event("conversation.message.delta");
                } else if ("follow_up".equals(messageType)) {
                    // follow_up 是推荐的后续问题
                    builder.event("conversation.message.completed");
                } else if ("verbose".equals(messageType)) {
                    // verbose 是需要用户交互的消息
                    builder.event("conversation.chat.requires_action");
                } else if ("tool_output".equals(messageType)) {
                    // 工具调用输出
                    builder.event("conversation.tool.output");
                } else if ("tool_call".equals(messageType)) {
                    // 工具调用
                    builder.event("conversation.tool.call");
                } else {
                    // 未知类型，回退到 status
                    setEventFromStatus(builder, jsonNode);
                }
            } else {
                // 没有 type 字段，使用 status
                setEventFromStatus(builder, jsonNode);
            }
            
            // 解析 usage
            if (jsonNode.has("usage")) {
                JsonNode usageNode = jsonNode.get("usage");
                CozeStreamMessage.Usage usage = new CozeStreamMessage.Usage();
                
                if (usageNode.has("input_count")) {
                    usage.setInputTokens(usageNode.get("input_count").asInt());
                }
                if (usageNode.has("output_count")) {
                    usage.setOutputTokens(usageNode.get("output_count").asInt());
                }
                if (usageNode.has("token_count")) {
                    usage.setTotalTokens(usageNode.get("token_count").asInt());
                }
                
                builder.usage(usage);
            }
            
            // 解析错误信息
            if (jsonNode.has("last_error")) {
                JsonNode errorNode = jsonNode.get("last_error");
                if (errorNode.has("msg") && !errorNode.get("msg").asText().isEmpty()) {
                    builder.errorMessage(errorNode.get("msg").asText());
                }
                if (errorNode.has("code") && errorNode.get("code").asInt() != 0) {
                    builder.errorCode(String.valueOf(errorNode.get("code").asInt()));
                }
            }
            
            // 解析 metadata
            if (jsonNode.has("metadata")) {
                Map<String, Object> metadata = objectMapper.convertValue(
                        jsonNode.get("metadata"),
                        new TypeReference<>() {}
                );
                builder.metadata(metadata);
            }
            
            CozeStreamMessage result = builder.build();
            log.debug("[Coze 解析] 解析成功: event={}, type={}, content={}",
                    result.getEvent(),
                    result.getType(),
                    result.getContent() != null ? result.getContent().substring(0, Math.min(20, result.getContent().length())) : null);
            return result;
            
        } catch (Exception e) {
            log.error("[Coze 解析] 解析单行失败: {}", line.substring(0, Math.min(100, line.length())), e);
            return CozeStreamMessage.builder()
                    .event("error")
                    .errorMessage("解析数据失败: " + e.getMessage())
                    .rawData(line)
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
    }

    /**
     * 根据 status 字段设置事件类型（当 type 字段无法确定事件时使用）
     */
    private void setEventFromStatus(CozeStreamMessage.CozeStreamMessageBuilder builder, JsonNode jsonNode) {
        if (jsonNode.has("status")) {
            String status = jsonNode.get("status").asText();
            if ("created".equals(status)) {
                builder.event("conversation.chat.created");
            } else if ("in_progress".equals(status)) {
                builder.event("conversation.chat.in_progress");
            } else if ("completed".equals(status)) {
                builder.event("conversation.chat.completed");
            } else if ("failed".equals(status)) {
                builder.event("conversation.chat.failed");
            }
        }
    }

    /**
     * 解析 SSE 事件块（多行组成的完整事件）
     * SSE 格式: event: xxx\ndata: {...}\n\n
     * 
     * @deprecated 使用 parseCozeStreamLine 代替
     * @param lines SSE 事件块的所有行
     * @return 解析后的消息对象
     */
    @Deprecated
    private CozeStreamMessage parseCozeStreamEvent(java.util.List<String> lines) {
        try {
            String eventType = null;
            StringBuilder dataBuilder = new StringBuilder();

            // 遍历所有行，提取 event 和 data
            for (String line : lines) {
                if (line.isEmpty()) {
                    continue; // 跳过空行
                }

                if (line.startsWith("event:")) {
                    eventType = line.substring(6).trim();
                    log.info("[Coze 解析] 提取事件类型: {}", eventType);
                } else if (line.startsWith("data:")) {
                    String dataLine = line.substring(5).trim();
                    dataBuilder.append(dataLine);
                    log.info("[Coze 解析] 提取数据行: [{}]", dataLine);
                } else {
                    log.warn("[Coze 解析] 未知行格式: [{}]", line);
                }
            }

            String jsonData = dataBuilder.toString();
            log.info("[Coze 解析] 完整JSON数据 (长度={}): [{}]", jsonData.length(), jsonData);

            // 跳过心跳包或结束标记
            if (jsonData.isEmpty() || "[DONE]".equals(jsonData)) {
                log.info("[Coze 解析] 检测到结束标记");
                return CozeStreamMessage.builder()
                        .event("done")
                        .timestamp(System.currentTimeMillis())
                        .build();
            }

            // 检查是否是有效的 JSON（必须以 { 或 [ 开头）
            String trimmedData = jsonData.trim();
            if (!trimmedData.startsWith("{") && !trimmedData.startsWith("[")) {
                log.warn("[Coze 解析] 数据不是有效的JSON格式，跳过: [{}]", jsonData);
                return null;
            }

            // 解析 JSON
            JsonNode jsonNode;
            try {
                jsonNode = objectMapper.readTree(jsonData);
            } catch (Exception e) {
                log.error("[Coze 解析] JSON解析失败，原始数据: [{}]", jsonData, e);
                // 返回错误消息而不是null，这样前端可以收到错误提示
                return CozeStreamMessage.builder()
                        .event("error")
                        .errorMessage("解析Coze响应失败: " + e.getMessage())
                        .rawData(jsonData)
                        .timestamp(System.currentTimeMillis())
                        .build();
            }

            CozeStreamMessage.CozeStreamMessageBuilder builder = CozeStreamMessage.builder()
                    .timestamp(System.currentTimeMillis())
                    .rawData(jsonData);

            // 使用提取的事件类型，或从JSON中获取
            if (eventType != null) {
                builder.event(eventType);
            } else if (jsonNode.has("event")) {
                builder.event(jsonNode.get("event").asText());
            }

            // 解析基本字段
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

            // 解析 metadata
            if (jsonNode.has("metadata")) {
                Map<String, Object> metadata = objectMapper.convertValue(
                        jsonNode.get("metadata"),
                        new TypeReference<>() {}
                );
                builder.metadata(metadata);
            }

            CozeStreamMessage result = builder.build();
            log.debug("[Coze 解析] 解析成功，事件类型: {}, 内容长度: {}",
                    result.getEvent(),
                    result.getContent() != null ? result.getContent().length() : 0);
            return result;

        } catch (Exception e) {
            log.error("[Coze 解析] 解析SSE事件块失败，行数: {}, 原始行内容: {}", 
                    lines.size(), lines, e);
            // 打印每一行的详细信息
            for (int i = 0; i < lines.size(); i++) {
                log.error("[Coze 解析] 第{}行: [{}]", i, lines.get(i));
            }
            return CozeStreamMessage.builder()
                    .event("error")
                    .errorMessage("解析数据失败: " + e.getMessage())
                    .rawData(String.join("\n", lines))
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
    }

    /**
     * 解析 Coze 流式数据（旧方法，保留用于兼容）
     * Coze 返回格式: event: xxx\ndata: {...}\n\n
     */
    @Deprecated
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
