package hbnu.project.zhiyanai.service;

import hbnu.project.zhiyanai.model.dto.ChatRequest;
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
     * 调用 Chatflow 并返回简化的文本流（仅提取文本内容）
     *
     * @param request 聊天请求
     * @return 文本流
     */
    Flux<String> callChatflowStreamSimple(ChatRequest request);
}
