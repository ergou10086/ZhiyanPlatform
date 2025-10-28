package hbnu.project.zhiyanai.service;

import hbnu.project.zhiyanai.model.dto.AIChatRequest;
import hbnu.project.zhiyanai.model.response.AIChatResponse;

/**
 * AI 对话服务接口
 *
 * @author ErgouTree
 */
public interface AIChatService {

    /**
     * 发送对话消息
     *
     * @param request 对话请求
     * @param userId 用户 ID
     * @return 对话响应
     */
    AIChatResponse chat(AIChatRequest request, Long userId);
}
