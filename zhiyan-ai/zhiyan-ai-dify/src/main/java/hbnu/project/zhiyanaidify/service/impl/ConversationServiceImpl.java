package hbnu.project.zhiyanaidify.service.impl;


import hbnu.project.zhiyanaidify.client.DifyApiClient;
import hbnu.project.zhiyanaidify.config.properties.DifyProperties;
import hbnu.project.zhiyanaidify.exception.DifyApiException;
import hbnu.project.zhiyanaidify.model.dto.DifyAppInfoDTO;
import hbnu.project.zhiyanaidify.model.response.ConversationListResponse;
import hbnu.project.zhiyanaidify.model.response.DeleteConversationResponse;
import hbnu.project.zhiyanaidify.model.response.MessageListResponse;
import hbnu.project.zhiyanaidify.service.ConversationService;
import hbnu.project.zhiyancommonbasic.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 对话历史管理服务实现
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final DifyApiClient difyApiClient;

    private final DifyProperties difyProperties;


    /**
     * 获取用户的会话列表
     *
     * @param userId 用户 ID
     * @param lastId 当前页最后一条记录的 ID（选填）
     * @param limit  返回记录数量
     * @param sortBy 排序字段
     * @return 会话列表
     */
    @Override
    public ConversationListResponse getConversations(Long userId, String lastId, Integer limit, String sortBy) {
        try{
            String userIdentifier = String.valueOf(userId);
            String apiKey = difyProperties.getApiKey();

            log.info("[获取会话列表] userId={}, lastId={}, limit={}, sortBy={}",
                    userId, lastId, limit, sortBy);

            ConversationListResponse response = difyApiClient.getConversations(
                    apiKey, userIdentifier, lastId, limit, sortBy
            );

            log.info("[获取会话列表] 成功返回 {} 条会话记录",
                    response.getData() != null ? response.getData().size() : 0);

            return response;
        }catch (ServiceException | DifyApiException e){
            log.error("[获取会话列表] 失败: userId={}, error={}", userId, e.getMessage(), e);
            throw new DifyApiException("获取会话列表失败: " + e.getMessage(), e);
        }
    }


    /**
     * 删除会话
     *
     * @param conversationId 会话 ID
     * @param userId         用户 ID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteConversation(String conversationId, Long userId) {
        try{
            String userIdentifier = String.valueOf(userId);
            String apiKey = "Bearer " + difyProperties.getApiKey();

            log.info("[删除会话] conversationId={}, userId={}", conversationId, userId);

            DifyApiClient.DeleteConversationRequest request =
                    DifyApiClient.DeleteConversationRequest.builder()
                            .user(userIdentifier)
                            .build();

            DeleteConversationResponse response = difyApiClient.deleteConversation(apiKey, userIdentifier, request);

            boolean success = "success".equalsIgnoreCase(response.getResult());

            log.info("[删除会话] conversationId={}, 结果={}", conversationId, success);

            return success;
        }catch (ServiceException |  DifyApiException e){
            log.error("[删除会话] 失败: conversationId={}, error={}", conversationId, e.getMessage(), e);
            throw new DifyApiException("删除会话失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取会话历史消息
     *
     * @param conversationId 会话 ID
     * @param userId         用户 ID
     * @param firstId        当前页第一条消息的 ID（选填）
     * @param limit          返回记录数量
     * @return 消息列表
     */
    @Override
    public MessageListResponse getMessages(String conversationId, Long userId, String firstId, Integer limit) {
        try{
            String userIdentifier = String.valueOf(userId);
            String apiKey = "Bearer " + difyProperties.getApiKey();

            log.info("[获取会话消息] conversationId={}, userId={}, firstId={}, limit={}",
                    conversationId, userId, firstId, limit);

            MessageListResponse response = difyApiClient.getMessages(
                    apiKey, conversationId, userIdentifier, firstId, limit
            );

            log.info("[获取会话消息] 成功返回 {} 条消息记录",
                    response.getData() != null ? response.getData().size() : 0);

            return response;
        }catch (ServiceException |  DifyApiException e){
            log.error("[获取会话消息] 失败: conversationId={}, error={}", conversationId, e.getMessage(), e);
            throw new DifyApiException("获取会话消息失败: " + e.getMessage(), e);
        }
    }


    /**
     * 获取 Dify 应用基本信息
     *
     * @return 应用信息
     */
    @Override
    public DifyAppInfoDTO getAppInfo() {
        try {
            String apiKey = "Bearer " + difyProperties.getApiKey();

            log.info("[获取应用信息] 开始请求");

            DifyAppInfoDTO appInfo = difyApiClient.getAppInfo(apiKey);

            log.info("[获取应用信息] 成功: appName={}, mode={}",
                    appInfo.getName(), appInfo.getMode());

            return appInfo;
        } catch (Exception e) {
            log.error("[获取应用信息] 失败: error={}", e.getMessage(), e);
            throw new DifyApiException("获取应用信息失败: " + e.getMessage(), e);
        }
    }
}
