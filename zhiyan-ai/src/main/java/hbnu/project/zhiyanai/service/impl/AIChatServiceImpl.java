package hbnu.project.zhiyanai.service.impl;

import hbnu.project.zhiyanai.client.DifyApiClient;
import hbnu.project.zhiyanai.client.KnowledgeServiceClient;
import hbnu.project.zhiyanai.config.properties.DifyProperties;
import hbnu.project.zhiyanai.exception.DifyApiException;
import hbnu.project.zhiyanai.model.dto.*;
import hbnu.project.zhiyanai.model.response.AIChatResponse;
import hbnu.project.zhiyanai.model.response.ChatResponse;
import hbnu.project.zhiyanai.service.AIChatService;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonbasic.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 对话服务实现
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIChatServiceImpl implements AIChatService {

    private final DifyApiClient difyApiClient;
    private final KnowledgeServiceClient knowledgeServiceClient;
    private final DifyProperties difyProperties;

    @Override
    public AIChatResponse chat(AIChatRequest request, Long userId) {
        try{
            // 1. 获取文件信息
            List<FileContext> fileContexts = getFileContexts(request.getFileIds(), userId);

            if (fileContexts.isEmpty()) {
                return AIChatResponse.builder()
                        .status("error")
                        .errorMessage("未能获取任何文件信息，请检查文件ID是否正确")
                        .responseTime(LocalDateTime.now())
                        .build();
            }

            // 2.构建Dify请求
            ChatRequest chatRequest = buildChatRequest(request, fileContexts, userId);

            // 3. 调用 Dify API
            String authHeader = "Bearer " + difyProperties.getApiKey();
            ChatResponse chatResponse = difyApiClient.sendChatMessage(authHeader, chatRequest);

            // 4. 构建响应
            return buildAIChatResponse(chatResponse);
        }catch (DifyApiException e) {
            log.error("AI 对话失败: ", e);
            return AIChatResponse.builder()
                    .status("error")
                    .errorMessage("对话失败: " + e.getMessage())
                    .responseTime(LocalDateTime.now())
                    .build();
        }
    }


    /**
     * 获取文件上下文信息
     */
    private List<FileContext> getFileContexts(List<Long> fileIds, Long userId) {
        try {
            log.info("开始获取文件信息: fileIds={}, count={}", fileIds, fileIds.size());

            R<List<FileContext>> result = knowledgeServiceClient.getFilesByIds(fileIds);

            if(result != null && result.getData() != null && result.getCode() == 200) {
                List<FileContext> files = result.getData();
                log.info("成功获取{}个文件信息", files.size());
                return files;
            }else {
                log.warn("获取文件信息失败，返回结果异常: {}", result);
                return List.of();
            }
        } catch (ServiceException e) {
            log.error("获取文件信息失败: ", e);
            // 返回基本的文件信息作为降级方案
            return fileIds.stream()
                    .map(id -> FileContext.builder()
                            .fileId(String.valueOf(id))
                            .fileName("文件-" + id)
                            .content("文件信息获取失败，仅提供文件ID")
                            .build())
                    .collect(Collectors.toList());
        }
    }


    /**
     * 构建 Dify Chat 请求
     */
    private ChatRequest buildChatRequest(
            AIChatRequest request,
            List<FileContext> fileContexts,
            Long userId
    ) {
        // 构建输入参数，包含文件信息
        Map<String, Object> inputs = new HashMap<>();

        // 文件信息格式为上下文文本
        String fileContext = formatFileContext(fileContexts);
        inputs.put("file_context", fileContext);
        inputs.put("file_count", fileContexts.size());

        // JSON列表显示文件信息
        List<Map<String, Object>> filesInfo = fileContexts.stream()
                .map(file -> {
                    Map<String, Object> fileMap = new HashMap<>();
                    fileMap.put("id", file.getFileId());
                    fileMap.put("name", file.getFileName());
                    fileMap.put("type", file.getFileType());
                    fileMap.put("size", file.getFileSize());
                    return fileMap;
                })
                .collect(Collectors.toList());
        inputs.put("files_info", filesInfo);

        return ChatRequest.builder()
                .query(request.getQuestion())
                .conversationId(request.getConversationId())
                .user(String.valueOf(userId))
                .inputs(inputs)
                .responseMode(Boolean.TRUE.equals(request.getStreamMode()) ? "streaming" : "blocking")
                // 注意：files 字段用于 Dify 上传的文件，这里的文件信息已经放在 inputs 中了
                .build();
    }


    /**
     * 格式化文件上下文
     */
    private String formatFileContext(List<FileContext> fileContexts) {
        StringBuilder context = new StringBuilder();
        context.append("用户选择了以下文件作为对话上下文：\n\n");

        for (int i = 0; i < fileContexts.size(); i++) {
            FileContext file = fileContexts.get(i);
            context.append(String.format("%d. 文件名: %s\n", i + 1, file.getFileName()));

            if (file.getFileType() != null) {
                context.append(String.format("   类型: %s\n", file.getFileType()));
            }

            if (file.getFileSizeFormatted() != null) {
                context.append(String.format("   大小: %s\n", file.getFileSizeFormatted()));
            } else if (file.getFileSize() != null) {
                context.append(String.format("   大小: %d bytes\n", file.getFileSize()));
            }

            if (file.getUploaderName() != null) {
                context.append(String.format("   上传者: %s\n", file.getUploaderName()));
            }

            if (file.getUploadAt() != null) {
                context.append(String.format("   上传时间: %s\n", file.getUploadAt()));
            }

            if (file.getContent() != null && !file.getContent().isEmpty()) {
                context.append(String.format("   内容摘要: %s\n", file.getContent()));
            }

            context.append("\n");
        }

        return context.toString();
    }


    /**
     * 构建 AI 对话响应
     */
    private AIChatResponse buildAIChatResponse(ChatResponse chatResponse) {
        return AIChatResponse.builder()
                .messageId(chatResponse.getMessageId())
                .conversationId(chatResponse.getConversationId())
                .answer(chatResponse.getAnswer())
                .status("success")
                .responseTime(LocalDateTime.now())
                .build();
    }
}
