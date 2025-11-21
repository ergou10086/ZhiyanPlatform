package hbnu.project.zhiyanaidify.controller;

import hbnu.project.common.log.annotation.AccessLog;
import hbnu.project.common.log.annotation.OperationLog;
import hbnu.project.common.log.annotation.OperationType;
import hbnu.project.zhiyanaidify.client.TaskSubmissionClient;
import hbnu.project.zhiyanaidify.model.dto.TaskResultContextDTO;
import hbnu.project.zhiyanaidify.model.request.TaskResultGenerateRequest;
import hbnu.project.zhiyanaidify.model.response.TaskResultGenerateResponse;
import hbnu.project.zhiyanaidify.service.TaskResultAIGenerateService;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 任务成果AI生成控制器
 * 提供AI生成任务成果草稿的RESTful API接口
 * 
 * @author Tokito
 */
@Slf4j
@RestController
@RequestMapping("/zhiyan/ai/achievement/generate")
@RequiredArgsConstructor
@Tag(name = "任务成果AI生成", description = "AI生成任务成果草稿相关接口")
@AccessLog("任务成果AI生成")
public class TaskResultAIGenerateController {
    
    private final TaskResultAIGenerateService aiGenerateService;
 
    private final TaskSubmissionClient taskSubmissionClient;

    /**
     * 生成任务成果草稿
     * 
     * @param request 生成请求
     * @return 生成任务响应（包含任务ID和初始状态）
     */
    @PostMapping("/draft")
    @Operation(summary = "生成任务成果草稿", description = "根据任务信息生成任务成果草稿，异步任务")
    @OperationLog(module = "任务成果AI生成", type = OperationType.INSERT, description = "生成任务成果草稿")
    public R<TaskResultGenerateResponse> generateDraft(
            @Parameter(description = "生成请求") @RequestBody TaskResultGenerateRequest request) {
        log.info("生成任务成果草稿: projectId={}, taskIds={}", request.getProjectId(), request.getTaskIds());
        
        // 从安全上下文获取用户ID
        Long userId = SecurityUtils.getUserId();
        request.setUserId(userId);
        
        // 调用服务生成草稿
        String jobId = aiGenerateService.generateTaskResultDraft(request);
        
        // 构建响应
        TaskResultGenerateResponse response = TaskResultGenerateResponse.builder()
                .jobId(jobId)
                .status("PENDING")
                .progress(0)
                .userId(userId)
                .projectId(request.getProjectId())
                .build();
        
        return R.ok(response, "生成任务已提交，请稍后查询状态");
    }

    /**
     * 查询生成状态
     * 
     * @param jobId 生成任务ID
     * @return 任务状态和结果
     */
    @GetMapping("/status/{jobId}")
    @Operation(summary = "查询生成状态", description = "查询AI生成任务的状态和结果")
    public R<TaskResultGenerateResponse> getStatus(
            @Parameter(description = "生成任务ID") @PathVariable String jobId) {
        log.info("查询生成状态: jobId={}", jobId);
        
        Long userId = SecurityUtils.getUserId();
        TaskResultGenerateResponse response = aiGenerateService.getGenerateStatus(jobId, userId);
        
        return R.ok(response);
    }

    /**
     * 取消生成
     * 
     * @param jobId 生成任务ID
     * @return 操作结果
     */
    @DeleteMapping("/cancel/{jobId}")
    @Operation(summary = "取消生成", description = "取消正在进行的AI生成任务")
    @OperationLog(module = "任务成果AI生成", type = OperationType.DELETE, description = "取消生成任务")
    public R<Void> cancelGenerate(
            @Parameter(description = "生成任务ID") @PathVariable String jobId) {
        log.info("取消生成: jobId={}", jobId);
        
        Long userId = SecurityUtils.getUserId();
        aiGenerateService.cancelGenerate(jobId, userId);
        
        return R.ok(null, "已取消生成");
    }

    /**
     * 获取用户的AI草稿列表
     * 
     * @return 草稿列表
     */
    @GetMapping("/drafts")
    @Operation(summary = "获取AI草稿列表", description = "获取当前用户的所有AI生成草稿")
    public R<List<TaskResultGenerateResponse>> getAIDrafts() {
        log.info("获取AI草稿列表");
        
        Long userId = SecurityUtils.getUserId();
        List<TaskResultGenerateResponse> drafts = aiGenerateService.getAIDrafts(userId);
        
        return R.ok(drafts);
    }

    @GetMapping("/task/{taskId}/context")
    @Operation(summary = "获取任务成果上下文", description = "根据任务ID获取任务详情及其所有提交记录，供AI使用")
    public R<TaskResultContextDTO> getTaskResultContext(
            @Parameter(description = "任务ID") @PathVariable Long taskId) {
        log.info("获取任务成果上下文: taskId={}", taskId);
        return taskSubmissionClient.getTaskResultContext(taskId);
    }

    @GetMapping("/tasks/context")
    @Operation(summary = "获取多个任务成果上下文", description = "根据多个任务ID获取对应的任务详情及提交记录列表，供AI使用")
    public R<List<TaskResultContextDTO>> getTasksResultContext(
            @Parameter(description = "任务ID列表") @RequestParam("taskIds") List<Long> taskIds) {
        log.info("获取多个任务成果上下文: taskIds={}", taskIds);

        List<TaskResultContextDTO> contexts = taskIds.stream()
                .map(id -> {
                    try {
                        R<TaskResultContextDTO> result = taskSubmissionClient.getTaskResultContext(id);
                        if (result != null && result.getCode() == 200 && result.getData() != null) {
                            return result.getData();
                        }
                        log.warn("获取任务成果上下文失败: taskId={}, code={}", id, result != null ? result.getCode() : null);
                    } catch (Exception e) {
                        log.error("获取任务成果上下文异常: taskId={}", id, e);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return R.ok(contexts);
    }
}

