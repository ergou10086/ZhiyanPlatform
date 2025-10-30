package hbnu.project.zhiyanaidify.client;

import hbnu.project.zhiyanaidify.config.DifyFeignConfig;
import hbnu.project.zhiyanaidify.model.dto.DifyAppInfoDTO;
import hbnu.project.zhiyanaidify.model.request.ChatRequest;
import hbnu.project.zhiyanaidify.model.request.StopChatRequest;
import hbnu.project.zhiyanaidify.model.response.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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


    /**
     * 停止响应
     * 仅支持流式模式
     *
     * @param apiKey API Key
     * @param taskId 任务 ID，可在流式返回 Chunk 中获取
     * @param request 停止请求（包含用户标识）
     * @return 停止响应
     */
    @PostMapping("/chat-messages/{task_id}/stop")
    StopChatResponse stopChatMessage(
            @RequestHeader("Authorization") String apiKey,
            @PathVariable("task_id") String taskId,
            @RequestBody StopChatRequest request
    );


    /**
     * 文件预览
     * 预览或下载已上传的文件
     *
     * @param apiKey API Key
     * @param fileId 文件 ID
     * @return 文件内容（字节流）
     */
    @GetMapping("/files/{file_id}/preview")
    ResponseEntity<byte[]> previewFile(
            @RequestHeader("Authorization") String apiKey,
            @PathVariable("file_id") String fileId
    );


    /**
     * 获取会话列表
     *
     * @param apiKey API Key
     * @param user 用户标识
     * @param lastId （选填）当前页最后一条记录的 ID，默认 null
     * @param limit 一次请求返回多少条记录，默认 20 条
     * @param sortBy 排序字段，默认 -updated_at（按更新时间倒序排列）
     * @return 会话列表响应
     */
    @GetMapping("/conversations")
    ConversationListResponse getConversations(
            @RequestHeader("Authorization") String apiKey,
            @RequestParam("user") String user,
            @RequestParam(value = "last_id", required = false) String lastId,
            @RequestParam(value = "limit", required = false, defaultValue = "20") Integer limit,
            @RequestParam(value = "sort_by", required = false, defaultValue = "-updated_at") String sortBy
    );


    /**
     * 删除会话
     *
     * @param apiKey API Key
     * @param conversationId 会话 ID
     * @return 删除响应
     */
    @DeleteMapping("/conversations/{conversation_id}")
    DeleteConversationResponse deleteConversation(
            @RequestHeader("Authorization") String apiKey,
            @PathVariable("conversation_id") String conversationId,
            @RequestBody DeleteConversationRequest request
    );


    /**
     * 获取会话历史消息
     *
     * @param apiKey API Key
     * @param conversationId 会话 ID
     * @param user 用户标识
     * @param firstId （选填）当前页第一条聊天记录的 ID，默认 null
     * @param limit 一次请求返回多少条记录，默认 20 条
     * @return 消息列表响应
     */
    @GetMapping("/messages")
    MessageListResponse getMessages(
            @RequestHeader("Authorization") String apiKey,
            @RequestParam("conversation_id") String conversationId,
            @RequestParam("user") String user,
            @RequestParam(value = "first_id", required = false) String firstId,
            @RequestParam(value = "limit", required = false, defaultValue = "20") Integer limit
    );


    /**
     * 获取应用基本信息
     *
     * @param apiKey API Key
     * @return 应用信息
     */
    @GetMapping("/info")
    DifyAppInfoDTO getAppInfo(
            @RequestHeader("Authorization") String apiKey
    );



    /**
     * 删除会话请求体
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class DeleteConversationRequest {
        private String user;
    }
}
