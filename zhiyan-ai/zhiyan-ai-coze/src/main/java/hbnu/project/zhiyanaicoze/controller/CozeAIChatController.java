package hbnu.project.zhiyanaicoze.controller;

import hbnu.project.common.log.annotation.AccessLog;
import hbnu.project.common.log.annotation.OperationLog;
import hbnu.project.common.log.annotation.OperationType;
import hbnu.project.zhiyanaicoze.config.properties.CozeProperties;
import hbnu.project.zhiyanaicoze.model.dto.CozeStreamMessage;
import hbnu.project.zhiyanaicoze.model.request.CozeChatRequest;
import hbnu.project.zhiyanaicoze.model.request.CozeChatStreamRequest;
import hbnu.project.zhiyanaicoze.model.response.CozeChatResponse;
import hbnu.project.zhiyanaicoze.model.response.CozeFileDetailResponse;
import hbnu.project.zhiyanaicoze.model.response.CozeFileUploadResponse;
import hbnu.project.zhiyanaicoze.service.CozeFileService;
import hbnu.project.zhiyanaicoze.service.CozeStreamService;
import hbnu.project.zhiyanaicoze.utils.SecurityHelper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hbnu.project.zhiyancommonbasic.domain.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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
@RequestMapping("/zhiyan/ai/coze")     // 原  /api/coze
@RequiredArgsConstructor
@Tag(name = "Coze AI 对话", description = "Coze AI 智能对话接口，支持流式响应")
@CrossOrigin(origins = {"http://localhost:8001", "http://127.0.0.1:8001"}, allowCredentials = "true")
@AccessLog("Coze AI 对话")
public class CozeAIChatController {

    private final CozeStreamService cozeStreamService;

    private final SecurityHelper securityHelper;

    private final CozeProperties cozeProperties;

    private final CozeFileService cozeFileService;

    private final ObjectMapper objectMapper;


    /**
     * Coze 对话（流式响应，不带文件）
     * ⭐ 修复版本：使用@RequestBody接收参数，避免URL过长导致431错误
     *
     * @param requestBody 聊天请求体（包含query、conversationId、customVariables）
     * @param authorizationHeader Authorization请求头
     * @return SSE 事件流
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
            summary = "Coze 对话（流式）",
            description = "调用 Coze 智能体进行流式对话，不涉及文件上传。" +
                    "使用POST body传递参数，支持长文本输入。" +
                    "首次对话无需传 conversationId，Coze 会在响应中返回新的对话ID。" +
                    "后续对话使用返回的 conversationId 维持上下文。"
    )
    @CrossOrigin(origins = {"http://localhost:8001", "http://localhost:8002", "http://127.0.0.1:8001", "http://127.0.0.1:8002"}, allowCredentials = "true")
    @OperationLog(module = "Coze AI 对话", description = "调用 Coze 智能体进行流式对话，不涉及文件上传", type = OperationType.OTHER)
    public Flux<ServerSentEvent<String>> chatStream(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "聊天请求体") 
            @RequestBody CozeChatStreamRequest requestBody,
            @Parameter(description = "Authorization 请求头") @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ){
        log.info("[Coze 对话] ========== 收到请求 ==========");
        log.info("[Coze 对话] query长度={}, conversationId={}", 
                requestBody.getQuery() != null ? requestBody.getQuery().length() : 0, 
                requestBody.getConversationId());
        log.info("[Coze 对话] 请求方法: POST, 路径: /api/coze/chat/stream");
        log.info("[Coze 对话] Authorization 头是否存在: {}", authorizationHeader != null && !authorizationHeader.isEmpty());
        
        // 从请求头获取用户ID（WebFlux 支持）
        Long userId = securityHelper.getUserId(authorizationHeader);
        log.info("[Coze 对话] 获取到的 userId: {}", userId);
        
        // 对于流式响应，如果用户未登录，返回错误事件流
        if (userId == null) {
            log.warn("[Coze 访问拒绝] 用户未登录，禁止使用AI功能");
            CozeStreamMessage errorMessage = CozeStreamMessage.builder()
                    .event("error")
                    .errorMessage("请先登录后再使用AI功能")
                    .status("failed")
                    .build();
            try {
                String jsonData = objectMapper.writeValueAsString(errorMessage);
                return Flux.just(ServerSentEvent.<String>builder()
                        .event("error")
                        .data(jsonData)
                        .build());
            } catch (Exception e) {
                log.error("[Coze 对话] 序列化错误消息失败", e);
                return Flux.just(ServerSentEvent.<String>builder()
                        .event("error")
                        .data("{\"event\":\"error\",\"errorMessage\":\"系统错误\"}")
                        .build());
            }
        }
        
        String userIdentifier = String.valueOf(userId);

        // 从请求体提取参数
        String query = requestBody.getQuery();
        String conversationId = requestBody.getConversationId();
        Map<String, Object> customVariables = requestBody.getCustomVariables();

        log.info("[Coze 对话] query长度={}, conversationId={}, userId={}, customVariables={}",
                query != null ? query.length() : 0, conversationId, userIdentifier, customVariables);

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
                .map(message -> {
                    try {
                        // 手动序列化为单行 JSON 字符串（避免 Spring 自动格式化）
                        String jsonData = objectMapper.writeValueAsString(message);
                        log.info("[Coze 对话] 发送SSE消息 - event: {}, JSON数据: {}", 
                                message.getEvent(), jsonData);
                        
                        return ServerSentEvent.<String>builder()
                                .event(message.getEvent())
                                .data(jsonData)
                                .build();
                    } catch (Exception e) {
                        log.error("[Coze 对话] 序列化消息失败", e);
                        // 返回错误消息
                        return ServerSentEvent.<String>builder()
                                .event("error")
                                .data("{\"event\":\"error\",\"errorMessage\":\"序列化失败\"}")
                                .build();
                    }
                })
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
     * @param cozeFileIds 已上传到 Coze 的文件 ID 列表（可选，前端已上传）
     * @param knowledgeFileIds 知识库文件ID列表（可选）
     * @return SSE 事件流
     */
    @PostMapping(value = "/chat/stream-with-files", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
            summary = "Coze 高级对话（流式 + 文件）",
            description = "支持上传本地文件或引用知识库文件进行对话。" +
                    "可同时传递 localFiles（本地文件）、cozeFileIds（已上传的文件ID）和 knowledgeFileIds（知识库文件）。" +
                    "文件会先上传到 Coze，然后在对话中使用。"
    )
    @OperationLog(module = "Coze AI 对话", description = "调用 Coze 智能体支持上传本地文件或引用知识库文件进行对话", type = OperationType.OTHER)
    public Flux<ServerSentEvent<String>> chatStreamWithFiles(
            @Parameter(description = "用户问题") @RequestParam String query,
            @Parameter(description = "对话 ID（可选）") @RequestParam(required = false) String conversationId,
            @Parameter(description = "本地上传的文件列表") @RequestParam(required = false) List<MultipartFile> localFiles,
            @Parameter(description = "已上传到 Coze 的文件 ID 列表（前端已上传）") @RequestParam(required = false) List<String> cozeFileIds,
            @Parameter(description = "知识库文件 ID 列表") @RequestParam(required = false) List<Long> knowledgeFileIds,
            @Parameter(description = "自定义变量（JSON字符串）") @RequestParam(required = false) String customVariablesJson,
            @Parameter(description = "Authorization 请求头") @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ){
        Long userId = securityHelper.getUserId(authorizationHeader);
        
        // 对于流式响应，如果用户未登录，返回错误事件流
        if (userId == null) {
            log.warn("[Coze 访问拒绝] 用户未登录，禁止使用AI功能");
            CozeStreamMessage errorMessage = CozeStreamMessage.builder()
                    .event("error")
                    .errorMessage("请先登录后再使用AI功能")
                    .status("failed")
                    .build();
            try {
                String jsonData = objectMapper.writeValueAsString(errorMessage);
                return Flux.just(ServerSentEvent.<String>builder()
                        .event("error")
                        .data(jsonData)
                        .build());
            } catch (Exception e) {
                log.error("[Coze 高级对话] 序列化错误消息失败", e);
                return Flux.just(ServerSentEvent.<String>builder()
                        .event("error")
                        .data("{\"event\":\"error\",\"errorMessage\":\"系统错误\"}")
                        .build());
            }
        }
        
        String userIdentifier = String.valueOf(userId);

        log.info("[Coze 高级对话] query={}, conversationId={}, localFiles={}, cozeFileIds={}, knowledgeFiles={}, userId={}",
                query, conversationId,
                localFiles != null ? localFiles.size() : 0,
                cozeFileIds != null ? cozeFileIds.size() : 0,
                knowledgeFileIds != null ? knowledgeFileIds.size() : 0,
                userIdentifier);

        // 解析自定义变量（如果提供）
        Map<String, Object> customVariables = null;
        if (customVariablesJson != null && !customVariablesJson.trim().isEmpty()) {
            try {
                customVariables = objectMapper.readValue(customVariablesJson, 
                        new TypeReference<Map<String, Object>>() {});
                log.info("[Coze 高级对话] 解析自定义变量成功: {}", customVariables);
            } catch (Exception e) {
                log.warn("[Coze 高级对话] 解析自定义变量失败: {}", customVariablesJson, e);
            }
        }

        // 1. 收集所有 Coze 文件 ID（使用原生 file_ids 机制）
        List<String> allCozeFileIds = new ArrayList<>();
        
        // 🔥 如果前端已经上传了文件到 Coze，直接使用这些文件 ID
        if (cozeFileIds != null && !cozeFileIds.isEmpty()) {
            allCozeFileIds.addAll(cozeFileIds);
            log.info("[Coze 高级对话] 使用前端已上传的 {} 个文件ID: {}", cozeFileIds.size(), cozeFileIds);
        }

        // 2. 上传本地文件到 Coze，获取 file ID
        if(localFiles != null && !localFiles.isEmpty()) {
            log.info("[Coze 高级对话] 开始上传 {} 个本地文件到 Coze", localFiles.size());
            for (MultipartFile file : localFiles) {
                try {
                    CozeFileUploadResponse uploadResponse = cozeFileService.uploadFile(file, userId);
                    if (uploadResponse != null && uploadResponse.getData() != null 
                            && uploadResponse.getData().getFileId() != null) {
                        String cozeFileId = uploadResponse.getData().getFileId();
                        allCozeFileIds.add(cozeFileId);
                        log.info("[Coze 高级对话] 本地文件上传成功: fileName={}, cozeFileId={}", 
                                file.getOriginalFilename(), cozeFileId);
                    } else {
                        log.warn("[Coze 高级对话] 本地文件上传失败或响应无效: fileName={}", 
                                file.getOriginalFilename());
                    }
                } catch (Exception e) {
                    log.error("[Coze 高级对话] 上传本地文件失败: fileName={}", 
                            file.getOriginalFilename(), e);
                }
            }
        }

        // 3. 上传知识库文件到 Coze，获取 file ID
        if (knowledgeFileIds != null && !knowledgeFileIds.isEmpty()) {
            log.info("[Coze 高级对话] 开始上传 {} 个知识库文件到 Coze", knowledgeFileIds.size());
            try {
                List<CozeFileUploadResponse> uploadResponses = 
                        cozeFileService.uploadKnowledgeFilesWithDetails(knowledgeFileIds, userId);
                for (CozeFileUploadResponse response : uploadResponses) {
                    if (response != null && response.getData() != null 
                            && response.getData().getFileId() != null) {
                        String cozeFileId = response.getData().getFileId();
                        allCozeFileIds.add(cozeFileId);
                        log.info("[Coze 高级对话] 知识库文件上传成功: cozeFileId={}", cozeFileId);
                    }
                }
            } catch (Exception e) {
                log.error("[Coze 高级对话] 上传知识库文件失败", e);
            }
        }

        log.info("[Coze 高级对话] 总共获得 {} 个 Coze 文件ID", allCozeFileIds.size());

        // 4. 构建消息（使用 Coze 原生 file_ids 机制）
        List<CozeChatRequest.CozeMessage> messages = new ArrayList<>();
        
        // 构建用户消息
        CozeChatRequest.CozeMessage.CozeMessageBuilder messageBuilder = CozeChatRequest.CozeMessage.builder()
                .role("user")
                .content(query)  // 只包含用户原始问题，不需要添加任何文件说明
                .contentType("text");
        
        // 如果有文件，添加 file_ids（Coze 原生支持）
        if (!allCozeFileIds.isEmpty()) {
            messageBuilder.fileIds(allCozeFileIds);
            log.info("[Coze 高级对话] 添加文件ID到消息 file_ids 字段: {}", allCozeFileIds);
        }
        
        messages.add(messageBuilder.build());

        // 4. 构建请求
        CozeChatRequest request = CozeChatRequest.builder()
                .botId(cozeProperties.getBotId())
                .userId(userIdentifier)
                .conversationId(conversationId)
                .stream(true)
                .additionalMessages(messages)
                .customVariables(customVariables)
                .autoSaveHistory(true)
                .build();
        
        log.info("[Coze 高级对话] 请求已构建，消息数量: {}", messages.size());

        return cozeStreamService.chatStream(request)
                .map(message -> {
                    try {
                        // 手动序列化为单行 JSON 字符串（避免 Spring 自动格式化）
                        String jsonData = objectMapper.writeValueAsString(message);
                        log.info("[Coze 高级对话] 发送SSE消息 - event: {}, JSON数据: {}", 
                                message.getEvent(), jsonData);
                        
                        return ServerSentEvent.<String>builder()
                                .event(message.getEvent())
                                .data(jsonData)
                                .build();
                    } catch (Exception e) {
                        log.error("[Coze 高级对话] 序列化消息失败", e);
                        // 返回错误消息
                        return ServerSentEvent.<String>builder()
                                .event("error")
                                .data("{\"event\":\"error\",\"errorMessage\":\"序列化失败\"}")
                                .build();
                    }
                });
    }


    /**
     * Coze文件管理 - 上传文件
     *
     * @param file 文件
     * @return 上传响应
     */
    @PostMapping("/files/upload")
    @Operation(summary = "上传文件到 Coze", description = "上传单个文件到 Coze 服务器，返回 file_id 供后续对话使用")
    @OperationLog(module = "Coze AI 对话", type = OperationType.UPLOAD, description = "上传单个文件到 Coze 服务器")
    public R<CozeFileUploadResponse> uploadFile(
            @Parameter(description = "文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Authorization 请求头") @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        Long userId = securityHelper.getUserId(authorizationHeader);
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
    @OperationLog(module = "Coze AI 对话", type = OperationType.UPLOAD, description = "批量上传多个文件到 Coze")
    public R<List<CozeFileUploadResponse>> uploadFiles(
            @Parameter(description = "文件列表") @RequestParam("files") List<MultipartFile> files,
            @Parameter(description = "Authorization 请求头") @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ){
        Long userId = securityHelper.getUserId(authorizationHeader);
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
    @OperationLog(module = "Coze AI 对话", type = OperationType.GRANT, description = "查询已上传到 Coze 的文件详细信息")
    public R<CozeFileDetailResponse> getFileDetail(
            @Parameter(description = "Coze 文件 ID") @PathVariable String fileId,
            @Parameter(description = "Authorization 请求头") @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        Long userId = securityHelper.getUserId(authorizationHeader);
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
    @OperationLog(module = "Coze AI 对话", type = OperationType.QUERY, description = "查询对话详情")
    public R<CozeChatResponse> getChatDetail(
            @Parameter(description = "对话ID") @RequestParam String conversationId,
            @Parameter(description = "聊天ID") @RequestParam String chatId,
            @Parameter(description = "Authorization 请求头") @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ){
        Long userId = securityHelper.getUserId(authorizationHeader);
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
