package hbnu.project.zhiyanaidify.service;

import hbnu.project.zhiyanaidify.model.dto.DifyAppInfoDTO;
import hbnu.project.zhiyanaidify.model.response.ConversationListResponse;
import hbnu.project.zhiyanaidify.model.response.MessageListResponse;

/**
 * dify chatflow对话历史管理服务接口
 *
 * @author ErgouTree
 */
public interface ConversationService {

    /**
     * 获取用户的会话列表
     *
     * @param userId 用户 ID
     * @param lastId 当前页最后一条记录的 ID（选填）
     * @param limit 返回记录数量
     * @param sortBy 排序字段
     * @return 会话列表
     */
    ConversationListResponse getConversations(Long userId, String lastId, Integer limit, String sortBy);

    /**
     * 删除会话
     *
     * @param conversationId 会话 ID
     * @param userId 用户 ID
     * @return 是否删除成功
     */
    boolean deleteConversation(String conversationId, Long userId);

    /**
     * 获取会话历史消息
     *
     * @param conversationId 会话 ID
     * @param userId 用户 ID
     * @param firstId 当前页第一条消息的 ID（选填）
     * @param limit 返回记录数量
     * @return 消息列表
     */
    MessageListResponse getMessages(String conversationId, Long userId, String firstId, Integer limit);

    /**
     * 获取 Dify 应用基本信息
     *
     * @return 应用信息
     */
    DifyAppInfoDTO getAppInfo();
}
