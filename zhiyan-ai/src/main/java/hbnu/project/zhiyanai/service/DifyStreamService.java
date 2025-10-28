package hbnu.project.zhiyanai.service;

import hbnu.project.zhiyanai.model.dto.ChatRequest;
import hbnu.project.zhiyanai.model.dto.WorkflowChatRequest;
import hbnu.project.zhiyancommonsse.dto.DifyStreamMessage;
import reactor.core.publisher.Flux;

/**
 * Dify 流式调用服务接口
 * 处理与 Dify API 的流式交互
 *
 * @author ErgouTree
 */
public interface DifyStreamService {

    /**
     * 流式发送聊天消息
     *
     * @param request 聊天请求
     * @return SSE 事件流（字符串形式）
     */
    Flux<String> sendChatMessageStream(ChatRequest request);

    /**
     * 调用工作流并返回流式响应
     *
     * @param request 工作流请求
     * @return 流式消息
     */
    Flux<DifyStreamMessage> callWorkflowStream(WorkflowChatRequest request);

    /**
     * 调用工作流并返回简化的文本流（仅提取文本内容）
     *
     * @param request 工作流请求
     * @return 文本流
     */
    Flux<String> callWorkflowStreamSimple(WorkflowChatRequest request);
}
