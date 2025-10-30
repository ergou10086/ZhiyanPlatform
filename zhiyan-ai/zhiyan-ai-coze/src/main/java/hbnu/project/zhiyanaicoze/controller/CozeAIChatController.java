package hbnu.project.zhiyanaicoze.controller;

import hbnu.project.zhiyanaicoze.config.properties.CozeProperties;
import hbnu.project.zhiyanaicoze.model.dto.CozeStreamMessage;
import hbnu.project.zhiyanaicoze.model.request.CozeChatRequest;
import hbnu.project.zhiyanaicoze.model.response.CozeChatResponse;
import hbnu.project.zhiyanaicoze.model.response.CozeFileDetailResponse;
import hbnu.project.zhiyanaicoze.model.response.CozeFileUploadResponse;
import hbnu.project.zhiyanaicoze.service.CozeFileService;
import hbnu.project.zhiyanaicoze.service.CozeStreamService;
import hbnu.project.zhiyanaicoze.utils.SecurityHelper;
import hbnu.project.zhiyancommonbasic.domain.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Coze AI 对话控制器
 * 支持流式对话
 *
 * @author ErgouTree
 */
@Slf4j
@RestController
@RequestMapping("/api/coze")
@RequiredArgsConstructor
@Tag(name = "Coze AI 对话", description = "Coze AI 智能对话接口，支持流式响应")
public class CozeAIChatController {

    private final CozeStreamService cozeStreamService;

    private final SecurityHelper securityHelper;

    private final CozeProperties cozeProperties;

    private final CozeFileService cozeFileService;


    /**
     * Coze 对话（流式响应，不带文件）
     *
     * @param query 用户问题
     * @param conversationId 对话 ID（可选，用于维持会话）
     * @param customVariables 自定义变量
     * @return SSE 事件流
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
            summary = "Coze 对话（流式）",
            description = "调用 Coze 智能体进行流式对话，不涉及文件上传。" +
                    "首次对话无需传 conversationId，Coze 会在响应中返回新的对话ID。" +
                    "后续对话使用返回的 conversationId 维持上下文。"
    )
    public Flux<ServerSentEvent<CozeStreamMessage>> chatStream(
            @Parameter(description = "用户问题") @RequestParam String query,
            @Parameter(description = "对话 ID（可选，用于维持会话）") @RequestParam(required = false) String conversationId,
            @Parameter(description = "自定义变量") @RequestBody(required = false) Map<String, String> customVariables
    ){
        // 获取用户ID
        Long userId = securityHelper.getUserId();
        String userIdentifier = getUserIdentifier(userId);

        log.info("[Coze 对话] query={}, conversationId={}, userId={}",
                query, conversationId, userIdentifier);

        // 构建消息列表
        List<CozeChatRequest.CozeMessage> messages = new ArrayList<>();
        messages.add(CozeChatRequest.CozeMessage.builder()
                .role("user")
                .content(query)
                .contentType("text")
                .build());

        // 构建聊天请求
        CozeChatRequest request = CozeChatRequest.builder()
                .botId(cozeProperties.getBotId())
                .userId(userIdentifier)
                .conversationId(conversationId)
                .stream(true)
                .additionalMessages(messages)
                .customVariables(customVariables)
                .autoSaveHistory(true)
                .build();

        // 返回流式响应
        return cozeStreamService.chatStream(request)
                .map(message -> ServerSentEvent.<CozeStreamMessage>builder()
                        .event(message.getEvent())
                        .data(message)
                        .build())
                .doOnComplete(() ->  log.info("[Coze 对话] 流式响应完成"));
    }


//    /**
//     * Coze 对话（高级版本）- 支持多轮对话和自定义变量
//     *
//     * @param request 完整的聊天请求
//     * @return SSE 事件流
//     */
//    @PostMapping(value = "/chat/stream/advanced", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    @Operation(
//            summary = "Coze 对话（高级流式）",
//            description = "支持完整的 Coze API 参数，包括多轮对话、自定义变量、元数据等"
//    )
//    public Flux<ServerSentEvent<CozeStreamMessage>> advancedChatStream(
//            @RequestBody CozeChatRequest request
//    ){
//        // 获取用户ID并设置
//        Long userId = securityHelper.getUserId();
//        String userIdentifier = getUserIdentifier(userId);
//        request.setUserId(userIdentifier);
//
//        // 设置智能体ID（如果未指定）
//        if (request.getBotId() == null || request.getBotId().isEmpty()) {
//            request.setBotId(cozeProperties.getBotId());
//        }
//
//        // 确保开启流式
//        request.setStream(true);
//
//        log.info("[Coze 对话高级] botId={}, userId={}, conversationId={}",
//                request.getBotId(), request.getUserId(), request.getConversationId());
//
//        // 返回流式响应
//        return cozeStreamService.chatStream(request)
//                .map(message -> ServerSentEvent.<CozeStreamMessage>builder()
//                        .event(message.getEvent())
//                        .data(message)
//                        .build())
//                .doOnComplete(() -> log.info("[Coze 对话高级] 流式响应完成"));
//    }


    /**
     * Coze流式对话（支持文件上传 + 知识库引用）
     * 一站式接口：上传文件 + 对话
     *
     * @param query 用户问题
     * @param conversationId 对话 ID（可选）
     * @param localFiles 本地上传的文件（可选）
     * @param knowledgeFileIds 知识库文件ID列表（可选）
     * @param customVariables 自定义变量
     * @return SSE 事件流
     */
    @PostMapping(value = "/chat/stream-with-files", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
            summary = "Coze 高级对话（流式 + 文件）",
            description = "支持上传本地文件或引用知识库文件进行对话。" +
                    "可同时传递 localFiles（本地文件）和 knowledgeFileIds（知识库文件）。" +
                    "文件会先上传到 Coze，然后在对话中使用。"
    )
    public Flux<ServerSentEvent<CozeStreamMessage>> chatStreamWithFiles(
            @Parameter(description = "用户问题") @RequestParam String query,
            @Parameter(description = "对话 ID（可选）") @RequestParam(required = false) String conversationId,
            @Parameter(description = "本地上传的文件列表") @RequestParam(required = false) List<MultipartFile> localFiles,
            @Parameter(description = "知识库文件 ID 列表") @RequestParam(required = false) List<Long> knowledgeFileIds,
            @Parameter(description = "自定义变量") @RequestParam(required = false) Map<String, String> customVariables
    ){
        Long userId = securityHelper.getUserId();
        String userIdentifier = getUserIdentifier(userId);

        log.info("[Coze 高级对话] query={}, conversationId={}, localFiles={}, knowledgeFiles={}, userId={}",
                query, conversationId,
                localFiles != null ? localFiles.size() : 0,
                knowledgeFileIds != null ? knowledgeFileIds.size() : 0,
                userIdentifier);

        // 收集所有 Coze 文件 ID
        List<String> cozeFileIds = new ArrayList<>();

        // 上传本地文件
        if(localFiles != null || !localFiles.isEmpty()) {
            log.info("[Coze 高级对话] 上传 {} 个本地文件", localFiles.size());
            List<CozeFileUploadResponse> uploadResponses = cozeFileService.uploadFiles(localFiles, userId);
            uploadResponses.forEach(response -> {
                if(response.getData() != null) {
                    cozeFileIds.add(response.getData().getFileId());
                }
            });
        }

        // 2. 从知识库上传文件
        if (knowledgeFileIds != null && !knowledgeFileIds.isEmpty()) {
            log.info("[Coze 高级对话] 从知识库上传 {} 个文件", knowledgeFileIds.size());
            List<String> knowledgeCozeIds = cozeFileService.uploadKnowledgeFiles(knowledgeFileIds, userId);
            cozeFileIds.addAll(knowledgeCozeIds);
        }

        log.info("[Coze 高级对话] 总共准备了 {} 个文件", cozeFileIds.size());

        // 3. 构建消息（包含文件引用）
        List<CozeChatRequest.CozeMessage> messages = new ArrayList<>();

        // 如果有文件，添加文件引用到消息中
        if(!cozeFileIds.isEmpty()) {
            StringBuilder contentWithFiles = new StringBuilder(query);
            contentWithFiles.append("\n\n附件文件ID: ");
            contentWithFiles.append(String.join(", ", cozeFileIds));
            messages.add(CozeChatRequest.CozeMessage.builder()
                    .role("user")
                    .content(contentWithFiles.toString())
                    .contentType("text")
                    .build());
        }else{
            messages.add(CozeChatRequest.CozeMessage.builder()
                    .role("user")
                    .content(query)
                    .contentType("text")
                    .build());
        }

        // 4. 构建请求（在 extra_params 中传递文件 ID）
        CozeChatRequest request = CozeChatRequest.builder()
                .botId(cozeProperties.getBotId())
                .userId(userIdentifier)
                .conversationId(conversationId)
                .stream(true)
                .additionalMessages(messages)
                .customVariables(customVariables)
                .autoSaveHistory(true)
                .build();

        // 如果有文件，添加到 extra_params
        if (!cozeFileIds.isEmpty()) {
            Map<String, Object> extraParams = Map.of("file_ids", cozeFileIds);
            request.setExtraParams(extraParams);
        }

        return cozeStreamService.chatStream(request)
                .map(message -> ServerSentEvent.<CozeStreamMessage>builder()
                        .event(message.getEvent())
                        .data(message)
                        .build());
    }


    /**
     * Coze文件管理 - 上传文件
     *
     * @param file 文件
     * @return 上传响应
     */
    @PostMapping("/files/upload")
    @Operation(summary = "上传文件到 Coze", description = "上传单个文件到 Coze 服务器，返回 file_id 供后续对话使用")
    public R<CozeFileUploadResponse> uploadFile(
            @Parameter(description = "文件") @RequestParam("file") MultipartFile file
    ) {
        Long userId = securityHelper.getUserId();
        log.info("[Coze 文件上传] fileName={}, size={}, userId={}",
                file.getOriginalFilename(), file.getSize(), userId);

        CozeFileUploadResponse response = cozeFileService.uploadFile(file, userId);

        return R.ok(response, "文件上传成功");
    }



    /**
     * 文件管理 - 批量上传
     *
     * @param files 文件列表
     * @return 上传响应列表
     */
    @PostMapping("/files/upload/batch")
    @Operation(summary = "批量上传文件", description = "批量上传多个文件到 Coze")
    public R<List<CozeFileUploadResponse>> uploadFiles(
            @Parameter(description = "文件列表") @RequestParam("files") List<MultipartFile> files
    ){
        Long userId = securityHelper.getUserId();
        log.info("[Coze 批量上传] fileCount={}, userId={}", files.size(), userId);

        List<CozeFileUploadResponse> responses = cozeFileService.uploadFiles(files, userId);

        return R.ok(responses, String.format("成功上传 %d 个文件", responses.size()));
    }


    /**
     * 文件管理 - 查询文件详情
     *
     * @param fileId 文件ID
     * @return 文件详情
     */
    @GetMapping("/files/{fileId}")
    @Operation(summary = "查询文件详情", description = "查询已上传到 Coze 的文件详细信息")
    public R<CozeFileDetailResponse> getFileDetail(
            @Parameter(description = "Coze 文件 ID") @PathVariable String fileId
    ) {
        Long userId = securityHelper.getUserId();
        log.info("[Coze 文件详情] fileId={}, userId={}", fileId, userId);

        CozeFileDetailResponse response = cozeFileService.getFileDetail(fileId);

        return R.ok(response, "查询成功");
    }


    /**
     * 查询对话详情
     *
     * @param conversationId 对话ID
     * @param chatId 聊天ID
     * @return 对话详情
     */
    @GetMapping("/chat/detail")
    @Operation(
            summary = "查询对话详情",
            description = "查询指定对话的详细信息，包括状态、token使用量等"
    )
    public R<CozeChatResponse> getChatDetail(
            @Parameter(description = "对话ID") @RequestParam String conversationId,
            @Parameter(description = "聊天ID") @RequestParam String chatId
    ){
        Long userId = securityHelper.getUserId();
        log.info("[Coze 对话详情] conversationId={}, chatId={}, userId={}",
                conversationId, chatId, userId);

        CozeChatResponse response = cozeStreamService.getChatDetail(conversationId, chatId);

        return R.ok(response, "查询成功");
    }


    /**
     * 健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查 Coze AI 服务是否正常运行")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Coze AI Service is running");
    }


    /**
     * 获取用户标识
     *
     * @param userId 用户 ID
     * @return 用户标识字符串
     */
    private String getUserIdentifier(Long userId) {
        if (userId == null) {
            log.warn("[Coze 访问拒绝] 用户未登录，禁止使用AI功能");
            throw new SecurityException("请先登录后再使用AI功能");
        }
        return String.valueOf(userId);
    }
}
