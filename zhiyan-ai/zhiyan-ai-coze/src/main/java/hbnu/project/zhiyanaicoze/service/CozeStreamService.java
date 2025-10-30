package hbnu.project.zhiyanaicoze.service;

import hbnu.project.zhiyanaicoze.model.dto.CozeStreamMessage;
import hbnu.project.zhiyanaicoze.model.request.CozeChatRequest;
import hbnu.project.zhiyanaicoze.model.response.CozeChatResponse;
import reactor.core.publisher.Flux;

/**
 * Coze 流式调用服务接口
 *
 * @author ErgouTree
 */
public interface CozeStreamService {

    /**
     * 调用 Coze 进行流式对话
     *
     * @param request 聊天请求
     * @return 流式消息
     */
    Flux<CozeStreamMessage> chatStream(CozeChatRequest request);

    /**
     * 查询对话详情
     *
     * @param conversationId 对话ID
     * @param chatId 聊天ID
     * @return 对话详情
     */
    CozeChatResponse getChatDetail(String conversationId, String chatId);
}
