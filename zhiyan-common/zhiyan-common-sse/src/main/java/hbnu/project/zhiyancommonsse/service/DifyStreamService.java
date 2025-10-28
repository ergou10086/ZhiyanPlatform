package hbnu.project.zhiyancommonsse.service;

import hbnu.project.zhiyancommonsse.core.DifyStreamEmitter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * Dify 流式调用服务
 * 处理与 Dify API 的流式交互
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DifyStreamService {

    private final WebClient.Builder webClientBuilder;


    /**
     * 调用 Dify API 并通过 SSE 流式返回结果
     *
     * @param conversationId 对话 ID
     * @param apiUrl Dify API 地址
     * @param apiKey API Key
     * @param requestBody 请求体
     */
    public void callDifyStream(
            String conversationId,
            String apiUrl,
            String apiKey,
            Map<String, Object> requestBody
    ){

        log.info("[Dify Stream] 开始流式调用: conversationId={}", conversationId);

        WebClient webClient = webClientBuilder
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();

        // 设置流失模式
        requestBody.put("response_mode", "streaming");

        webClient.post()
                .uri("/chat-messages")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnNext(chunk -> {
                    // 解析并发送每个数据块
                    handleDifyChunk(conversationId, chunk);
                })
                .doOnError(error -> {
                    log.error("[Dify Stream] 调用错误: conversationId={}", conversationId, error);
                    DifyStreamEmitter.sendError(conversationId, "AI 调用失败: " + error.getMessage());
                })
                .doOnComplete(() -> {
                    log.info("[Dify Stream] 调用完成: conversationId={}", conversationId);
                    DifyStreamEmitter.completeEmitter(conversationId);
                })
                .subscribe();

    }



    /**
     * 处理 Dify 返回的数据块
     */
    private void handleDifyChunk(String conversationId, String chunk) {
        try {
            // Dify 返回的是 SSE 格式：data: {...}
            if (chunk.startsWith("data: ")) {
                String jsonData = chunk.substring(6).trim();

                // 跳过心跳包
                if ("[DONE]".equals(jsonData)) {
                    return;
                }

                // 解析 JSON 并提取文本
                // TODO:这里简化处理，实际需要根据 Dify 返回格式解析
                String text = extractTextFromDifyResponse(jsonData);

                if (text != null && !text.isEmpty()) {
                    DifyStreamEmitter.sendChunk(conversationId, text);
                }
            }
        } catch (Exception e) {
            log.error("[Dify Stream] 处理数据块失败: conversationId={}", conversationId, e);
        }
    }



    /**
     * 从 Dify 响应中提取文本
     * 需要根据实际 Dify API 响应格式调整
     */
    private String extractTextFromDifyResponse(String jsonData) {
        // TODO: 实现JSON解析逻辑

        return jsonData; // 临时返回原始数据
    }
}
