package hbnu.project.zhiyanaidify.controller;

import hbnu.project.zhiyanaidify.model.request.ChatRequest;
import hbnu.project.zhiyanaidify.model.response.DifyFileUploadResponse;
import hbnu.project.zhiyanaidify.service.DifyFileService;
import hbnu.project.zhiyanaidify.service.DifyStreamService;
import hbnu.project.zhiyanaidify.utils.SecurityHelper;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonbasic.utils.id.UUID;
import hbnu.project.zhiyancommonsse.dto.DifyStreamMessage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 对话控制器
 * 支持文件上传、流式对话（Chatflow）
 *
 * @author ErgouTree
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI 对话", description = "AI 智能对话接口，支持文件上传和流式响应（基于 Dify Chatflow）")
public class DifyAIChatController {

    private final DifyStreamService difyStreamService;
    private final DifyFileService difyFileService;
    private final SecurityHelper securityHelper;


    /**
     * 上传本地文件到 Dify
     *
     * @param file 文件
     * @return 上传响应
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/files/upload")
    @Operation(summary = "上传本地文件到 Dify", description = "上传文件到 Dify，用于后续的对话上下文")
    public R<DifyFileUploadResponse> uploadFile(
            @Parameter(description = "文件") @RequestParam("file") MultipartFile file
    ) {
        Long userId = securityHelper.getUserId();
        log.info("[Dify 文件上传] 上传文件: fileName={}, size={}, userId={}",
                file.getOriginalFilename(), file.getSize(), userId);

        DifyFileUploadResponse response = difyFileService.uploadFile(file, userId);

        return R.ok(response, "文件上传成功");
    }


    /**
     * 批量上传本地文件到 Dify
     *
     * @param files 文件列表
     * @return 上传响应列表
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/files/upload/batch")
    @Operation(summary = "批量本地上传文件", description = "批量上传多个文件到 Dify")
    public R<List<DifyFileUploadResponse>> uploadFiles(
            @Parameter(description = "文件列表") @RequestParam("files") List<MultipartFile> files
    ) {
        Long userId = securityHelper.getUserId();
        log.info("[Dify 批量上传] 上传文件: fileCount={}, userId={}", files.size(), userId);

        List<DifyFileUploadResponse> responses = difyFileService.uploadFiles(files, userId);

        return R.ok(responses, String.format("成功上传 %d 个文件", responses.size()));
    }


    /**
     * 从知识库上传文件到 Dify
     *
     * @param fileIds 知识库文件 ID 列表
     * @return Dify 文件 ID 列表
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/files/upload/knowledge")
    @Operation(summary = "从知识库上传文件", description = "从知识库获取文件并上传到 Dify")
    public R<List<String>> uploadKnowledgeFiles(
            @Parameter(description = "知识库文件 ID 列表") @RequestBody List<Long> fileIds
    ) {
        Long userId = securityHelper.getUserId();
        log.info("[Dify 知识库文件上传] fileIds={}, userId={}", fileIds, userId);

        List<String> difyFileIds = difyFileService.uploadKnowledgeFiles(fileIds, userId);

        return R.ok(difyFileIds, String.format("成功上传 %d 个文件", difyFileIds.size()));
    }


    /**
     * Chatflow 对话（流式响应）- 适用于 Dify 聊天流应用
     *
     * @param query 用户问题
     * @param conversationId 对话 ID（UUID 格式，首次对话可不传）
     * @param fileIds Dify 文件 ID 列表（已上传到 Dify 的文件）
     * @param inputs 输入变量
     * @return SSE 事件流
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/chatflow/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
            summary = "Chatflow 对话（流式）",
            description = "调用 Dify Chatflow 进行对话。" +
                    "首次对话无需传 conversationId，Dify 会在响应中返回新的对话ID（UUID格式）。" +
                    "后续对话使用返回的 conversationId 维持上下文。"
    )
    public Flux<ServerSentEvent<DifyStreamMessage>> chatflowStream(
            @Parameter(description = "用户问题") @RequestParam String query,
            @Parameter(description = "对话 ID（UUID 格式，首次对话不传或传空）") @RequestParam(required = false) String conversationId,
            @Parameter(description = "Dify 文件 ID 列表") @RequestParam(required = false) List<String> fileIds,
            @Parameter(description = "输入变量") @RequestBody(required = false) Map<String, Object> inputs
    ) {
        // 获取用户ID，如果为null则使用默认值
        Long userId = securityHelper.getUserId();
        String userIdentifier = getUserIdentifier(userId);

        // 验证并处理 conversationId
        String validConversationId = validateConversationId(conversationId);

        log.info("[Chatflow 对话] query={}, conversationId={}, fileIds={}, userId={}",
                query, validConversationId, fileIds, userIdentifier);

        // 构建聊天请求
        ChatRequest request = ChatRequest.builder()
                .query(query)
                .conversationId(validConversationId)
                .user(userIdentifier)
                .inputs(inputs != null ? inputs : new HashMap<>())
                .responseMode("streaming")
                .build();

        // 如果有文件，添加文件
        if (fileIds != null && !fileIds.isEmpty()) {
            request.setFiles(buildChatFilesList(fileIds));
        }

        // 返回流式响应
        return difyStreamService.callChatflowStream(request)
                .map(message -> ServerSentEvent.<DifyStreamMessage>builder()
                        .event(message.getEvent())
                        .data(message)
                        .build());
    }


    /**
     * 上传文件并进行 Chatflow 对话（一站式接口）
     * 
     * @param query 用户问题
     * @param conversationId 对话 ID（可选）
     * @param knowledgeFileIds 知识库文件 ID 列表（可选）
     * @param localFiles 本地上传的文件列表（可选）
     * @param inputs 输入变量（可选）
     * @return SSE 事件流
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/chatflow/upload-and-chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
            summary = "上传文件并对话（一站式）",
            description = "支持两种文件上传方式：" +
                    "1. 从知识库上传：传递 knowledgeFileIds" +
                    "2. 本地文件上传：传递 localFiles（multipart/form-data）" +
                    "文件上传成功后自动进行流式对话。"
    )
    public Flux<ServerSentEvent<DifyStreamMessage>> uploadAndChatStream(
            @Parameter(description = "用户问题") @RequestParam String query,
            @Parameter(description = "对话 ID（UUID 格式，首次对话不传或传空）") @RequestParam(required = false) String conversationId,
            @Parameter(description = "知识库文件 ID 列表") @RequestParam(required = false) List<Long> knowledgeFileIds,
            @Parameter(description = "本地上传的文件列表") @RequestParam(required = false) List<MultipartFile> localFiles,
            @Parameter(description = "输入变量") @RequestParam(required = false) Map<String, Object> inputs
    ) {
        Long userId = securityHelper.getUserId();
        String userIdentifier = getUserIdentifier(userId);
        String validConversationId = validateConversationId(conversationId);

        log.info("[上传并对话] query={}, conversationId={}, knowledgeFileIds={}, localFilesCount={}, userId={}",
                query, validConversationId, knowledgeFileIds, 
                localFiles != null ? localFiles.size() : 0, userIdentifier);

        // 收集所有 Dify 文件 ID
        List<String> difyFileIds = new ArrayList<>();

        // 1. 处理知识库文件
        if (knowledgeFileIds != null && !knowledgeFileIds.isEmpty()) {
            log.info("[上传并对话] 从知识库上传 {} 个文件", knowledgeFileIds.size());
            List<String> knowledgeDifyIds = difyFileService.uploadKnowledgeFiles(knowledgeFileIds, userId);
            difyFileIds.addAll(knowledgeDifyIds);
        }

        // 2. 处理本地文件
        if (localFiles != null && !localFiles.isEmpty()) {
            log.info("[上传并对话] 从本地上传 {} 个文件", localFiles.size());
            List<DifyFileUploadResponse> localUploadResponses = difyFileService.uploadFiles(localFiles, userId);
            localUploadResponses.forEach(response -> difyFileIds.add(response.getFileId()));
        }

        log.info("[上传并对话] 总共上传了 {} 个文件到 Dify, fileIds={}", difyFileIds.size(), difyFileIds);

        // 3. 构建聊天请求
        ChatRequest request = ChatRequest.builder()
                .query(query)
                .conversationId(validConversationId)
                .user(userIdentifier)
                .inputs(inputs != null ? inputs : new HashMap<>())
                .responseMode("streaming")
                .build();

        // 4. 如果有文件，添加文件
        if (!difyFileIds.isEmpty()) {
            request.setFiles(buildChatFilesList(difyFileIds));
        }

        // 5. 返回流式响应
        return difyStreamService.callChatflowStream(request)
                .map(message -> ServerSentEvent.<DifyStreamMessage>builder()
                        .event(message.getEvent())
                        .data(message)
                        .build());
    }


    /**
     * 停止流式响应
     *
     * @param taskId 任务 ID（从流式响应中获取）
     * @return 停止结果
     */
    @PostMapping("/chat/stop/{taskId}")
    @Operation(
            summary = "停止流式响应",
            description = "停止正在进行的流式对话响应。" +
                    "taskId 可以从流式响应的 Chunk 中获取。" +
                    "仅支持流式模式。"
    )
    public R<Boolean> stopChatStream(
            @Parameter(description = "任务 ID") @PathVariable String taskId
    ) {
        Long userId = securityHelper.getUserId();
        log.info("[停止流式响应] taskId={}, userId={}", taskId, userId);

        boolean success = difyStreamService.stopChatStream(taskId, userId);

        return success
                ? R.ok(true, "流式响应已停止")
                : R.fail("停止流式响应失败");
    }


    /**
     * 文件预览（在浏览器中显示）
     *
     * @param fileId Dify 文件 ID
     * @return 文件内容
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/files/{fileId}/preview")
    @Operation(
            summary = "文件预览",
            description = "预览已上传到 Dify 的文件内容（在浏览器中显示）"
    )
    public ResponseEntity<Resource> previewFile(
            @Parameter(description = "文件 ID")
            @PathVariable String fileId
    ) {
        Long userId = securityHelper.getUserId();
        log.info("[文件预览] fileId={}, userId={}", fileId, userId);

        Resource fileResource = difyFileService.previewFile(fileId, userId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                .body(fileResource);
    }


    /**
     * 创建新的对话会话
     *
     * @return 新创建的对话ID
     */
    @GetMapping("/conversation/new")
    @Operation(summary = "创建新对话", description = "创建一个新的对话会话并返回对话ID")
    public R<String> createNewConversation() {
        // 使用UUID工具类生成新的对话ID
        String newConversationId = String.valueOf(UUID.randomUUID());
        log.info("[AI 对话] 创建新对话会话: conversationId={}", newConversationId);
        return R.ok(newConversationId, "对话会话创建成功");
    }


    /**
     * 健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查 AI 服务是否正常运行")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("AI Service is running");
    }


    /**
     * 验证并处理 conversationId
     * - 如果是有效的 UUID，则返回原值
     * - 如果不是有效的 UUID 或为空，则返回 null（Dify 会创建新对话）
     *
     * @param conversationId 对话 ID
     * @return 有效的对话 ID 或 null
     */
    private String validateConversationId(String conversationId) {
        if (conversationId == null || conversationId.trim().isEmpty()) {
            log.debug("[Chatflow] conversationId 为空，将创建新对话");
            return null;
        }

        // 使用模块的UUID工具类验证UUID格式
        if (isValidUUID(conversationId)) {
            log.debug("[Chatflow] conversationId 有效: {}", conversationId);
            return conversationId;
        } else {
            log.warn("[Chatflow] conversationId 格式无效: {}，将创建新对话", conversationId);
            return null;
        }
    }


    /**
     * 使用您自己的UUID工具类验证UUID格式
     *
     * @param uuid 要验证的UUID字符串
     * @return 是否有效
     */
    private boolean isValidUUID(String uuid) {
        try {
            // 使用模块的UUID工具类进行验证
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }


    /**
     * 获取用户标识
     * - 如果有 userId，使用 userId
     * - 如果没有 userId（未登录），拒绝访问
     *
     * @param userId 用户 ID
     * @return 用户标识字符串
     */
    private String getUserIdentifier(Long userId) {
        if (userId == null) {
            // 未登录：抛出异常，由全局异常处理器处理（返回401或403）
            log.warn("[AI 访问拒绝] 用户未登录，禁止使用AI功能");
            throw new SecurityException("请先登录后再使用AI功能");
        }
        // 已登录：返回用户ID作为标识
        return String.valueOf(userId);
    }


    /**
     * 构建聊天文件列表（根据 Dify Chat API 规范）
     */
    private List<ChatRequest.DifyFile> buildChatFilesList(List<String> fileIds) {
        return fileIds.stream()
                .map(fileId -> ChatRequest.DifyFile.builder()
                        .type("file")  // 文件类型
                        .transferMethod("local_file")  // 本地文件
                        .uploadFileId(fileId)  // 上传的文件 ID
                        .build())
                .toList();
    }
}