package hbnu.project.zhiyanai.client;

import hbnu.project.zhiyanai.config.DifyFeignConfig;
import hbnu.project.zhiyanai.model.dto.ChatRequest;
import hbnu.project.zhiyanai.model.response.ChatResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Dify API Feign 客户端
 *
 * @author ErgouTree
 */
@FeignClient(
        name = "dify-api",
        url = "${dify.api-url}",
        configuration = DifyFeignConfig.class
)
public interface DifyApiClient {

    /**
     * 发送聊天消息到 Dify
     *
     * @param apiKey API Key
     * @param request 聊天请求
     * @return 聊天响应
     */
    @PostMapping("/chat-message")
    ChatResponse sendChatMessage(
            @RequestHeader("Authorization") String apiKey,
            @RequestBody ChatRequest request
    );
}
