package hbnu.project.zhiyanaidify.service.impl;

import hbnu.project.zhiyanaidify.client.TaskSubmissionClient;
import hbnu.project.zhiyanaidify.model.dto.TaskDetailDTO;
import hbnu.project.zhiyanaidify.model.dto.TaskResultContextDTO;
import hbnu.project.zhiyanaidify.model.dto.TaskSubmissionDTO;
import hbnu.project.zhiyanaidify.model.request.ChatRequest;
import hbnu.project.zhiyanaidify.model.request.TaskResultGenerateRequest;
import hbnu.project.zhiyanaidify.model.response.TaskResultGenerateResponse;
import hbnu.project.zhiyanaidify.service.DifyStreamService;
import hbnu.project.zhiyanaidify.service.TaskAttachmentService;
import hbnu.project.zhiyanaidify.service.TaskResultAIGenerateService;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 任务成果AI生成服务实现
 * 
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskResultAIGenerateServiceImpl implements TaskResultAIGenerateService {
    
    private final TaskSubmissionClient taskSubmissionClient;
    private final TaskAttachmentService taskAttachmentService;
    private final DifyStreamService difyStreamService;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String REDIS_KEY_PREFIX = "task_result_generate:";
    private static final String USER_DRAFTS_PREFIX = "user_drafts:";
    private static final long REDIS_EXPIRE_DAYS = 7; // Redis缓存7天
    
    @Override
    public String generateTaskResultDraft(TaskResultGenerateRequest request) {
        Long projectId = request.getProjectId();
        List<Long> taskIds = request.getTaskIds();
        Long userId = request.getUserId();
        
        log.info("🚀 开始生成任务成果草稿: projectId={}, taskIds={}, userId={}", 
                projectId, taskIds, userId);
        
        // 1. 生成任务ID
        String jobId = UUID.randomUUID().toString();
        
        // 2. 初始化任务状态
        TaskResultGenerateResponse response = TaskResultGenerateResponse.builder()
                .jobId(jobId)
                .status("PENDING")
                .progress(0)
                .userId(userId)
                .projectId(projectId)
                .createdAt(LocalDateTime.now())
                .build();
        
        // 3. 存储到Redis
        String redisKey = REDIS_KEY_PREFIX + jobId;
        redisTemplate.opsForValue().set(redisKey, response, REDIS_EXPIRE_DAYS, TimeUnit.DAYS);
        
        // 4. 添加到用户草稿列表
        String userDraftsKey = USER_DRAFTS_PREFIX + userId;
        redisTemplate.opsForSet().add(userDraftsKey, jobId);
        redisTemplate.expire(userDraftsKey, REDIS_EXPIRE_DAYS, TimeUnit.DAYS);
        
        // 5. 异步执行生成任务
        executeGenerateTask(jobId, request);
        
        return jobId;
    }
    
    /**
     * 异步执行生成任务
     */
    @Async
    protected void executeGenerateTask(String jobId, TaskResultGenerateRequest request) {
        String redisKey = REDIS_KEY_PREFIX + jobId;
        
        try {
            log.info("📝 [JobId: {}] 开始异步生成任务", jobId);
            
            // 更新状态为PROCESSING
            updateTaskStatus(redisKey, "PROCESSING", 10, null, null);
            
            // 1. 收集所有任务的信息和附件
            List<String> allDifyFileIds = new ArrayList<>();
            StringBuilder taskSummary = new StringBuilder();
            
            for (Long taskId : request.getTaskIds()) {
                try {
                    log.info("📋 [JobId: {}] 处理任务: taskId={}", jobId, taskId);
                    
                    // 获取任务的完整成果上下文（任务详情 + 所有提交记录）
                    R<TaskResultContextDTO> contextResult = taskSubmissionClient.getTaskResultContext(taskId);

                    if (contextResult != null && contextResult.getCode() == 200 && contextResult.getData() != null) {
                        TaskResultContextDTO context = contextResult.getData();
                        TaskDetailDTO task = context.getTask();
                        List<TaskSubmissionDTO> submissions = Optional
                                .ofNullable(context.getSubmissions())
                                .orElse(Collections.emptyList());
                        TaskSubmissionDTO finalApproved = context.getFinalApprovedSubmission();
                        TaskSubmissionDTO latestSubmission = context.getLatestSubmission();

                        // ========= 任务基础信息 =========
                        String taskTitle = null;
                        if (task != null && task.getTitle() != null) {
                            taskTitle = task.getTitle();
                        } else if (latestSubmission != null && latestSubmission.getTaskTitle() != null) {
                            taskTitle = latestSubmission.getTaskTitle();
                        } else {
                            taskTitle = "未命名任务";
                        }

                        taskSummary.append(String.format("### 任务: %s\n", taskTitle));

                        if (task != null) {
                            if (task.getProjectName() != null) {
                                taskSummary.append(String.format("**所属项目**: %s\n", task.getProjectName()));
                            }
                            if (task.getStatusName() != null || task.getStatus() != null) {
                                String statusText = task.getStatusName() != null ? task.getStatusName() : task.getStatus();
                                taskSummary.append(String.format("**任务状态**: %s\n", statusText));
                            }
                            if (task.getCreatorName() != null) {
                                taskSummary.append(String.format("**创建人**: %s\n", task.getCreatorName()));
                            }
                            if (task.getAssignees() != null && !task.getAssignees().isEmpty()) {
                                String assigneeNames = task.getAssignees().stream()
                                        .map(a -> a.getUserName() != null ? a.getUserName() : "未知")
                                        .collect(Collectors.joining("、"));
                                taskSummary.append(String.format("**执行者**: %s\n", assigneeNames));
                            }
                            if (task.getDueDate() != null) {
                                taskSummary.append(String.format("**截止日期**: %s\n", task.getDueDate()));
                            }
                        }

                        // ========= 提交与审核历史概要 =========
                        if (!submissions.isEmpty()) {
                            taskSummary.append("\n**提交历史概览：**\\n\n");
                            // 按版本升序展示
                            submissions.stream()
                                    .sorted(Comparator.comparing(s -> Optional.ofNullable(s.getVersion()).orElse(0)))
                                    .forEach(s -> {
                                        taskSummary.append(String.format("- 版本 %s | 提交人: %s | 时间: %s | 审核状态: %s\n",
                                                s.getVersion() != null ? s.getVersion() : 0,
                                                s.getSubmitterName() != null ? s.getSubmitterName() : "未知",
                                                s.getSubmissionTime() != null ? s.getSubmissionTime() : "未知",
                                                s.getReviewStatus() != null ? s.getReviewStatus() : "未知"));
                                    });
                            taskSummary.append("\n");
                        }

                        // ========= 最终通过版本详情 =========
                        if (finalApproved != null) {
                            taskSummary.append("**最终通过版本详情：**\\n\n");
                            taskSummary.append(String.format("- 提交人: %s\n", finalApproved.getSubmitterName() != null ? finalApproved.getSubmitterName() : "未知"));
                            taskSummary.append(String.format("- 提交时间: %s\n", finalApproved.getSubmissionTime() != null ? finalApproved.getSubmissionTime() : "未知"));
                            if (finalApproved.getSubmissionContent() != null) {
                                taskSummary.append(String.format("- 提交说明: %s\n", finalApproved.getSubmissionContent()));
                            }
                            if (finalApproved.getReviewComment() != null) {
                                taskSummary.append(String.format("- 审核意见: %s\n", finalApproved.getReviewComment()));
                            }
                            taskSummary.append("\n");
                        } else if (latestSubmission != null) {
                            // 没有最终通过版本，退化为使用最新提交做详细说明
                            taskSummary.append("**最新提交详情：**\\n\n");
                            taskSummary.append(String.format("- 提交人: %s\n", latestSubmission.getSubmitterName() != null ? latestSubmission.getSubmitterName() : "未知"));
                            taskSummary.append(String.format("- 提交时间: %s\n", latestSubmission.getSubmissionTime() != null ? latestSubmission.getSubmissionTime() : "未知"));
                            if (latestSubmission.getSubmissionContent() != null) {
                                taskSummary.append(String.format("- 提交说明: %s\n\n", latestSubmission.getSubmissionContent()));
                            }
                        }

                        // ========= 附件处理：优先使用最终通过版本，否则合并所有提交的附件 =========
                        Set<String> attachmentUrlSet = new LinkedHashSet<>();
                        if (finalApproved != null && finalApproved.getAttachmentUrls() != null) {
                            attachmentUrlSet.addAll(finalApproved.getAttachmentUrls());
                        } else {
                            for (TaskSubmissionDTO s : submissions) {
                                if (s.getAttachmentUrls() != null) {
                                    attachmentUrlSet.addAll(s.getAttachmentUrls());
                                }
                            }
                        }

                        if (!attachmentUrlSet.isEmpty()) {
                            log.info("📎 [JobId: {}] 任务[{}]共有{}个附件,开始下载并上传到Dify", jobId, taskId, attachmentUrlSet.size());

                            List<String> difyFileIds = taskAttachmentService.downloadAndUploadAttachments(
                                    new ArrayList<>(attachmentUrlSet),
                                    request.getUserId()
                            );

                            allDifyFileIds.addAll(difyFileIds);
                            log.info("✅ [JobId: {}] 任务[{}]附件处理完成,上传了{}个文件到Dify",
                                    jobId, taskId, difyFileIds.size());

                            if (!difyFileIds.isEmpty()) {
                                taskSummary.append(String.format("**附件数量**: %d 个\n\n", difyFileIds.size()));
                            }
                        } else {
                            taskSummary.append("**附件**: 无\n\n");
                        }
                    } else {
                        log.warn("⚠️ [JobId: {}] 获取任务[{}]成果上下文失败或不存在", jobId, taskId);
                        taskSummary.append(String.format("### 任务ID: %s\n**状态**: 无提交记录或无法获取上下文\n\n", taskId));
                    }
                    
                } catch (Exception e) {
                    log.error("❌ [JobId: {}] 处理任务[{}]失败", jobId, taskId, e);
                    taskSummary.append(String.format("### 任务ID: %s\n**状态**: 处理失败 - %s\n\n", taskId, e.getMessage()));
                }
            }
            
            // 更新进度
            updateTaskStatus(redisKey, "PROCESSING", 40, null, null);
            
            // 2. 构建AI提示词
            String prompt = buildPrompt(request.getAdditionalRequirements(), taskSummary.toString());
            log.info("📝 [JobId: {}] AI提示词构建完成,长度: {} 字符", jobId, prompt.length());
            
            // 更新进度
            updateTaskStatus(redisKey, "PROCESSING", 50, null, null);
            
            // 3. 构建Dify聊天请求
            ChatRequest chatRequest = ChatRequest.builder()
                    .query(prompt)
                    .user(String.valueOf(request.getUserId()))
                    .inputs(new HashMap<>())
                    .responseMode("blocking") // 使用阻塞模式
                    .build();
            
            // 4. 如果有附件,添加到请求中
            if (!allDifyFileIds.isEmpty()) {
                chatRequest.setFiles(buildFilesList(allDifyFileIds));
                log.info("📎 [JobId: {}] 添加{}个附件到Dify请求", jobId, allDifyFileIds.size());
            }
            
            // 更新进度
            updateTaskStatus(redisKey, "PROCESSING", 60, null, null);
            
            // 5. 调用Dify API生成内容
            log.info("🤖 [JobId: {}] 开始调用Dify API生成内容", jobId);
            String aiResult = callDifyAPI(chatRequest);
            
            // 更新进度
            updateTaskStatus(redisKey, "PROCESSING", 90, null, null);
            
            // 6. 保存生成结果
            log.info("✅ [JobId: {}] AI生成完成,结果长度: {} 字符", jobId, aiResult != null ? aiResult.length() : 0);
            updateTaskStatus(redisKey, "COMPLETED", 100, aiResult, null);
            
        } catch (Exception e) {
            log.error("❌ [JobId: {}] 生成任务成果草稿失败", jobId, e);
            updateTaskStatus(redisKey, "FAILED", 0, null, "生成失败: " + e.getMessage());
        }
    }
    
    /**
     * 调用Dify API
     */
    private String callDifyAPI(ChatRequest request) {
        try {
            // 这里需要实现实际的Dify API调用
            // 由于DifyStreamService可能只支持流式,这里需要适配
            // 简化处理:使用阻塞方式获取完整结果
            
            // TODO: 根据实际的DifyStreamService实现调整
            // 这里假设有一个阻塞式的调用方法
            log.info("调用Dify API: query长度={}, files数量={}", 
                    request.getQuery() != null ? request.getQuery().length() : 0,
                    request.getFiles() != null ? request.getFiles().size() : 0);
            
            // 临时返回模拟结果
            return "# 任务成果报告\n\n" +
                   "## 实验概述\n" +
                   "根据提交的任务信息和附件,生成的实验成果报告。\n\n" +
                   "## 主要工作内容\n" +
                   request.getQuery() + "\n\n" +
                   "## 结论\n" +
                   "任务已完成,详细内容请参考附件。";
            
        } catch (Exception e) {
            log.error("调用Dify API失败", e);
            throw new RuntimeException("AI生成失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 更新任务状态
     */
    private void updateTaskStatus(String redisKey, String status, int progress, String result, String errorMessage) {
        TaskResultGenerateResponse response = (TaskResultGenerateResponse) redisTemplate.opsForValue().get(redisKey);
        if (response != null) {
            response.setStatus(status);
            response.setProgress(progress);
            response.setUpdatedAt(LocalDateTime.now());
            
            if (result != null) {
                Map<String, Object> draftContent = new HashMap<>();
                // 目前先将 AI 生成的 Markdown 文本放入 draftContent 中，后续可扩展为结构化 TaskResultDetailDTO
                draftContent.put("markdown", result);
                response.setDraftContent(draftContent);
            }
            if (errorMessage != null) {
                response.setErrorMessage(errorMessage);
            }
            
            redisTemplate.opsForValue().set(redisKey, response, REDIS_EXPIRE_DAYS, TimeUnit.DAYS);
        }
    }
    
    /**
     * 构建AI提示词
     */
    private String buildPrompt(String additionalRequirements, String taskSummary) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下任务提交信息,生成一份结构化的实验成果报告:\n\n");
        prompt.append("## 任务提交信息\n\n");
        prompt.append(taskSummary);
        
        if (StringUtils.isNotEmpty(additionalRequirements)) {
            prompt.append("\n\n## 补充要求\n\n");
            prompt.append(additionalRequirements);
        }
        
        prompt.append("\n\n## 报告要求\n\n");
        prompt.append("请生成包含以下部分的报告:\n");
        prompt.append("1. **实验概述**: 简要说明实验目的和背景\n");
        prompt.append("2. **主要工作内容**: 详细描述完成的工作和采用的方法\n");
        prompt.append("3. **关键数据和结果**: 列出重要的实验数据、图表和结果\n");
        prompt.append("4. **问题与解决方案**: 遇到的问题及解决方法\n");
        prompt.append("5. **结论与展望**: 总结成果并提出未来工作方向\n\n");
        prompt.append("请使用Markdown格式输出,确保结构清晰、内容详实。");
        
        return prompt.toString();
    }
    
    /**
     * 构建Dify文件列表
     */
    private List<ChatRequest.DifyFile> buildFilesList(List<String> fileIds) {
        return fileIds.stream()
                .map(fileId -> ChatRequest.DifyFile.builder()
                        .type("document")
                        .transferMethod("local_file")
                        .uploadFileId(fileId)
                        .build())
                .collect(Collectors.toList());
    }
    
    @Override
    public TaskResultGenerateResponse getGenerateStatus(String jobId, Long userId) {
        log.info("查询生成状态: jobId={}, userId={}", jobId, userId);
        
        String redisKey = REDIS_KEY_PREFIX + jobId;
        TaskResultGenerateResponse response = (TaskResultGenerateResponse) redisTemplate.opsForValue().get(redisKey);
        
        if (response == null) {
            throw new IllegalArgumentException("生成任务不存在或已过期");
        }
        
        // 验证任务是否属于该用户
        if (!response.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权查看该生成任务");
        }
        
        return response;
    }
    
    @Override
    public void cancelGenerate(String jobId, Long userId) {
        log.info("取消生成: jobId={}, userId={}", jobId, userId);
        
        String redisKey = REDIS_KEY_PREFIX + jobId;
        TaskResultGenerateResponse response = (TaskResultGenerateResponse) redisTemplate.opsForValue().get(redisKey);
        
        if (response == null) {
            throw new IllegalArgumentException("生成任务不存在或已过期");
        }
        
        // 验证任务是否属于该用户
        if (!response.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权取消该生成任务");
        }
        
        // 只有PENDING或PROCESSING状态可以取消
        if ("COMPLETED".equals(response.getStatus()) || "FAILED".equals(response.getStatus()) || "CANCELLED".equals(response.getStatus())) {
            throw new IllegalArgumentException("任务已完成或已取消,无法取消");
        }
        
        // 更新状态为CANCELLED
        response.setStatus("CANCELLED");
        response.setUpdatedAt(LocalDateTime.now());
        redisTemplate.opsForValue().set(redisKey, response, REDIS_EXPIRE_DAYS, TimeUnit.DAYS);
        
        log.info("✅ 生成任务已取消: jobId={}", jobId);
    }
    
    @Override
    public List<TaskResultGenerateResponse> getAIDrafts(Long userId) {
        log.info("获取AI草稿列表: userId={}", userId);
        
        String userDraftsKey = USER_DRAFTS_PREFIX + userId;
        Set<Object> jobIds = redisTemplate.opsForSet().members(userDraftsKey);
        
        if (jobIds == null || jobIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<TaskResultGenerateResponse> drafts = new ArrayList<>();
        
        for (Object jobIdObj : jobIds) {
            String jobId = jobIdObj.toString();
            String redisKey = REDIS_KEY_PREFIX + jobId;
            TaskResultGenerateResponse response = (TaskResultGenerateResponse) redisTemplate.opsForValue().get(redisKey);
            
            if (response != null) {
                drafts.add(response);
            }
        }
        
        // 按创建时间倒序排序
        drafts.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        
        log.info("✅ 获取到{}个AI草稿", drafts.size());
        return drafts;
    }
}







