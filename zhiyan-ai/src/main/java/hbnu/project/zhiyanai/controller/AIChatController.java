package hbnu.project.zhiyanai.controller;

import hbnu.project.zhiyanai.model.dto.AIChatRequest;
import hbnu.project.zhiyanai.model.dto.WorkflowChatRequest;
import hbnu.project.zhiyanai.model.response.AIChatResponse;
import hbnu.project.zhiyanai.model.response.DifyFileUploadResponse;
import hbnu.project.zhiyanai.service.AIChatService;
import hbnu.project.zhiyanai.service.DifyFileService;
import hbnu.project.zhiyanai.service.DifyStreamService;
import hbnu.project.zhiyanai.utils.SecurityHelper;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonsse.dto.DifyStreamMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 对话控制器
 * 支持文件上传、流式对话、工作流调用
 *
 * @author ErgouTree
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI 对话", description = "AI 智能对话接口，支持文件上传和流式响应")
public class AIChatController {

    private final AIChatService aiChatService;
    private final DifyStreamService difyStreamService;
    private final DifyFileService difyFileService;
    private final SecurityHelper securityHelper;

    /**
     * 发送对话消息（阻塞模式）
     *
     * @param request 对话请求
     * @return 对话响应
     */
    @PostMapping("/chat/message")
    @Operation(summary = "发送对话消息", description = "发送对话消息并等待完整响应（阻塞模式）")
    public ResponseEntity<AIChatResponse> sendMessage(
            @Valid @RequestBody AIChatRequest request
    ) {
        Long userId = securityHelper.getUserId();
        log.info("[AI 对话] 收到请求: userId={}, question={}", userId, request.getQuestion());

        AIChatResponse response = aiChatService.chat(request, userId);

        return ResponseEntity.ok(response);
    }

    /**
     * 上传文件到 Dify
     *
     * @param file 文件
     * @return 上传响应
     */
    @PostMapping("/files/upload")
    @Operation(summary = "上传文件到 Dify", description = "上传文件到 Dify，用于后续的对话上下文")
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
     * 批量上传文件到 Dify
     *
     * @param files 文件列表
     * @return 上传响应列表
     */
    @PostMapping("/files/upload/batch")
    @Operation(summary = "批量上传文件", description = "批量上传多个文件到 Dify")
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
     * 工作流对话（流式响应）
     *
     * @param query 用户问题
     * @param conversationId 对话 ID
     * @param fileIds Dify 文件 ID 列表（已上传到 Dify 的文件）
     * @param inputs 工作流输入变量
     * @return SSE 事件流
     */
    @PostMapping(value = "/workflow/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "工作流对话（流式）", description = "调用 Dify 工作流进行对话，支持文件上传，返回流式响应")
    public Flux<ServerSentEvent<DifyStreamMessage>> workflowChatStream(
            @Parameter(description = "用户问题") @RequestParam String query,
            @Parameter(description = "对话 ID") @RequestParam(required = false) String conversationId,
            @Parameter(description = "Dify 文件 ID 列表") @RequestParam(required = false) List<String> fileIds,
            @Parameter(description = "工作流输入变量") @RequestBody(required = false) Map<String, Object> inputs
    ) {
        Long userId = securityHelper.getUserId();
        log.info("[工作流对话] query={}, conversationId={}, fileIds={}, userId={}",
                query, conversationId, fileIds, userId);

        // 构建工作流请求
        WorkflowChatRequest request = WorkflowChatRequest.builder()
                .query(query)
                .conversationId(conversationId)
                .user(String.valueOf(userId))
                .fileIds(fileIds)
                .inputs(inputs != null ? inputs : new HashMap<>())
                .responseMode("streaming")
                .build();

        // 返回流式响应
        return difyStreamService.callWorkflowStream(request)
                .map(message -> ServerSentEvent.<DifyStreamMessage>builder()
                        .event(message.getEvent())
                        .data(message)
                        .build());
    }

    /**
     * 工作流对话（简化流式响应，仅返回文本）
     *
     * @param query 用户问题
     * @param conversationId 对话 ID
     * @param fileIds Dify 文件 ID 列表
     * @param inputs 工作流输入变量
     * @return SSE 文本流
     */
    @PostMapping(value = "/workflow/chat/stream/simple", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "工作流对话（简化流式）", description = "调用工作流对话，仅返回文本内容的流式响应")
    public Flux<ServerSentEvent<String>> workflowChatStreamSimple(
            @Parameter(description = "用户问题") @RequestParam String query,
            @Parameter(description = "对话 ID") @RequestParam(required = false) String conversationId,
            @Parameter(description = "Dify 文件 ID 列表") @RequestParam(required = false) List<String> fileIds,
            @Parameter(description = "工作流输入变量") @RequestBody(required = false) Map<String, Object> inputs
    ) {
        Long userId = securityHelper.getUserId();
        log.info("[工作流对话-简化] query={}, userId={}", query, userId);

        WorkflowChatRequest request = WorkflowChatRequest.builder()
                .query(query)
                .conversationId(conversationId)
                .user(String.valueOf(userId))
                .fileIds(fileIds)
                .inputs(inputs != null ? inputs : new HashMap<>())
                .responseMode("streaming")
                .build();

        return difyStreamService.callWorkflowStreamSimple(request)
                .map(text -> ServerSentEvent.<String>builder()
                        .data(text)
                        .build());
    }

    /**
     * 一步式：上传文件 + 工作流对话
     *
     * @param files 文件列表
     * @param query 用户问题
     * @param conversationId 对话 ID
     * @param inputs 工作流输入变量
     * @return SSE 事件流
     */
    @PostMapping(value = "/workflow/chat/upload-and-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "上传文件并对话", description = "一步完成：上传文件到 Dify，然后调用工作流进行流式对话")
    public Flux<ServerSentEvent<DifyStreamMessage>> uploadAndChat(
            @Parameter(description = "文件列表") @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @Parameter(description = "用户问题") @RequestParam String query,
            @Parameter(description = "对话 ID") @RequestParam(required = false) String conversationId,
            @Parameter(description = "工作流输入变量") @RequestBody(required = false) Map<String, Object> inputs
    ) {
        Long userId = securityHelper.getUserId();
        log.info("[上传并对话] query={}, filesCount={}, userId={}",
                query, files != null ? files.size() : 0, userId);

        // 1. 上传文件到 Dify
        List<String> fileIds = null;
        if (files != null && !files.isEmpty()) {
            List<DifyFileUploadResponse> uploadResponses = difyFileService.uploadFiles(files, userId);
            fileIds = uploadResponses.stream()
                    .map(DifyFileUploadResponse::getFileId)
                    .toList();
            log.info("[上传并对话] 文件上传完成: uploadedCount={}", fileIds.size());
        }

        // 2. 构建工作流请求
        WorkflowChatRequest request = WorkflowChatRequest.builder()
                .query(query)
                .conversationId(conversationId)
                .user(String.valueOf(userId))
                .fileIds(fileIds)
                .inputs(inputs != null ? inputs : new HashMap<>())
                .responseMode("streaming")
                .build();

        // 3. 返回流式响应
        return difyStreamService.callWorkflowStream(request)
                .map(message -> ServerSentEvent.<DifyStreamMessage>builder()
                        .event(message.getEvent())
                        .data(message)
                        .build());
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查 AI 服务是否正常运行")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("AI Service is running");
    }
}
