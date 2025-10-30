package hbnu.project.zhiyanaidify.controller;


import hbnu.project.zhiyanaidify.model.dto.DifyAppInfoDTO;
import hbnu.project.zhiyanaidify.model.response.ConversationListResponse;
import hbnu.project.zhiyanaidify.model.response.MessageListResponse;
import hbnu.project.zhiyanaidify.service.ConversationService;
import hbnu.project.zhiyanaidify.utils.SecurityHelper;
import hbnu.project.zhiyancommonbasic.domain.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 对话历史管理控制器
 *
 * @author ErgouTree
 */
@Slf4j
@RestController
@RequestMapping("/api/ai/conversations")
@RequiredArgsConstructor
@Tag(name = "AI 对话历史", description = "AI 对话历史管理接口，用于获取和管理对话记录")
public class DifyAIConversationController {

    private final ConversationService conversationService;

    private final SecurityHelper securityHelper;


    /**
     * 获取会话列表
     *
     * @param lastId 当前页最后一条记录的 ID（选填）
     * @param limit 返回记录数量（默认 20）
     * @param sortBy 排序字段（默认 -updated_at）
     * @return 会话列表
     */
    @GetMapping
    @Operation(
            summary = "获取会话列表",
            description = "获取当前用户的会话列表，默认返回最近 20 条。" + "支持分页：传递 lastId 获取下一页数据。"
    )
    public R<ConversationListResponse> getConversation(
            @Parameter(description = "当前页最后一条记录的 ID，用于分页") @RequestParam(required = false) String lastId,
            @Parameter(description = "返回记录数量，默认 20，最大 100") @RequestParam(required = false, defaultValue = "20") Integer limit,
            @Parameter(description = "排序字段，默认 -updated_at（按更新时间倒序）。" + "可选值：created_at, -created_at, updated_at, -updated_at") @RequestParam(required = false, defaultValue = "-updated_at") String sortBy
    ){
        Long userId = securityHelper.getUserId();
        log.info("[获取会话列表] userId={}, lastId={}, limit={}, sortBy={}",
                userId, lastId, limit, sortBy);

        ConversationListResponse response = conversationService.getConversations(
                userId, lastId, limit, sortBy
        );

        return R.ok(response, "获取会话列表成功");
    }


    /**
     * 获取会话历史消息
     *
     * @param conversationId 会话 ID
     * @param firstId 当前页第一条消息的 ID（选填）
     * @param limit 返回记录数量（默认 20）
     * @return 消息列表
     */
    @GetMapping("/{conversationId}/messages")
    @Operation(
            summary = "获取会话历史消息",
            description = "获取指定会话的历史消息列表，支持分页。" + "传递 firstId 获取更早的消息记录。"
    )
    public R<MessageListResponse> getMessages(
            @Parameter(description = "会话 ID") @PathVariable String conversationId,
            @Parameter(description = "当前页第一条消息的 ID，用于分页获取更早的消息") @RequestParam(required = false) String firstId,
            @Parameter(description = "返回记录数量，默认 20") @RequestParam(required = false, defaultValue = "20") Integer limit
    ){
        Long userId = securityHelper.getUserId();
        log.info("[获取会话消息] conversationId={}, userId={}, firstId={}, limit={}",
                conversationId, userId, firstId, limit);

        MessageListResponse response = conversationService.getMessages(
                conversationId, userId, firstId, limit
        );

        return R.ok(response, "获取会话消息成功");
    }



    /**
     * 删除会话
     *
     * @param conversationId 会话 ID
     * @return 删除结果
     */
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{conversationId}")
    @Operation(
            summary = "删除会话",
            description = "删除指定的会话及其所有历史消息"
    )
    public R<Boolean> deleteConversation(
            @Parameter(description = "会话 ID")
            @PathVariable String conversationId
    ) {
        Long userId = securityHelper.getUserId();
        log.info("[删除会话] conversationId={}, userId={}", conversationId, userId);

        boolean success = conversationService.deleteConversation(conversationId, userId);

        return success
                ? R.ok(true, "会话删除成功")
                : R.fail("会话删除失败");
    }


    /**
     * 获取应用的基本信息
     *
     * @return 应用信息
     */
    @GetMapping("/chatflow-info")
    @Operation(
            summary = "获取应用基本信息",
            description = "获取当前 Dify 应用的基本信息，包括名称、描述、标签、模式等"
    )
    public R<DifyAppInfoDTO> getAppInfo() {
        log.info("[获取应用信息] 开始请求");

        DifyAppInfoDTO appInfo = conversationService.getAppInfo();

        return R.ok(appInfo, "获取应用信息成功");
    }
}
