package hbnu.project.zhiyanaidify.service;

import hbnu.project.zhiyanaidify.model.request.ChatRequest;
import hbnu.project.zhiyancommonsse.dto.DifyStreamMessage;
import reactor.core.publisher.Flux;

/**
 * Dify 流式调用服务接口
 * 处理与 Dify Chatflow 的流式交互
 *
 * @author ErgouTree
 */
public interface DifyStreamService {

    /**
     * 调用 Chatflow 并返回流式响应（适用于聊天流应用）
     *
     * @param request 聊天请求
     * @return 流式消息
     */
    Flux<DifyStreamMessage> callChatflowStream(ChatRequest request);

    /**
     * 停止流式响应
     *
     * @param taskId 任务 ID
     * @param userId 用户 ID
     * @return 是否停止成功
     */
    boolean stopChatStream(String taskId, Long userId);
}
