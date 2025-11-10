package hbnu.project.zhiyanproject.controller;

import hbnu.project.common.log.annotation.AccessLog;
import hbnu.project.common.log.annotation.OperationLog;
import hbnu.project.common.log.annotation.OperationType;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonidempotent.annotation.Idempotent;
import hbnu.project.zhiyancommonidempotent.enums.IdempotentType;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import hbnu.project.zhiyanproject.model.dto.TaskSubmissionDTO;
import hbnu.project.zhiyanproject.model.form.ReviewSubmissionRequest;
import hbnu.project.zhiyanproject.model.form.SubmitTaskRequest;
import hbnu.project.zhiyanproject.service.TaskSubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务提交控制器
 * 处理任务提交、审核相关的接口
 *
 * @author ErgouTree
 */
@Slf4j
@RestController
@RequestMapping("/api/projects/tasks/submissions")
@RequiredArgsConstructor
@Tag(name = "任务提交管理", description = "任务提交、审核、撤回等接口")
@SecurityRequirement(name = "Bearer Authentication")
@AccessLog("任务提交管理")
public class TaskSubmissionController {

    private final TaskSubmissionService submissionService;

    // ==================== 提交任务接口 ====================

    /**
     * 提交任务
     * 业务场景：任务执行者完成任务后提交审核
     */
    @PostMapping("/{taskId}/submit")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "提交任务", description = "任务执行者提交任务，等待项目负责人或任务创建者审核")
    @OperationLog(module = "任务提交", type = OperationType.INSERT, description = "提交任务", recordParams = true, recordResult = true)
    @Idempotent(type = IdempotentType.SPEL, key = "#taskId + ':' + #userId", timeout = 3, message = "任务提交中，请勿重复提交")
    public R<TaskSubmissionDTO> submitTask(
            @PathVariable @Parameter(description = "任务ID") Long taskId,
            @Valid @RequestBody SubmitTaskRequest request) {

        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            log.warn("提交任务失败: 用户未登录");
            return R.fail("请先登录");
        }

        log.info("用户[{}]提交任务[{}]", userId, taskId);

        try {
            TaskSubmissionDTO result = submissionService.submitTask(taskId, request, userId);
            return R.ok(result, "任务提交成功，等待审核");
        } catch (IllegalArgumentException e) {
            log.warn("提交任务失败: {}", e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    // ==================== 审核任务接口 ====================

    /**
     * 审核任务提交
     * 业务场景：项目负责人或任务创建者审核任务提交
     */
    @PutMapping("/{submissionId}/review")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "审核任务提交", description = "项目负责人或任务创建者审核任务提交（批准/拒绝）")
    @OperationLog(module = "任务提交", type = OperationType.UPDATE, description = "审核任务提交", recordParams = true, recordResult = true)
    @Idempotent(type = IdempotentType.SPEL, key = "#submissionId + ':' + #reviewerId", timeout = 2, message = "审核中，请勿重复操作")
    public R<TaskSubmissionDTO> reviewSubmission(
            @PathVariable @Parameter(description = "提交记录ID") Long submissionId,
            @Valid @RequestBody ReviewSubmissionRequest request) {

        Long reviewerId = SecurityUtils.getUserId();
        if (reviewerId == null) {
            log.warn("审核任务失败: 用户未登录");
            return R.fail("请先登录");
        }

        log.info("用户[{}]审核提交记录[{}]，结果: {}", reviewerId, submissionId, request.getReviewStatus());

        try {
            TaskSubmissionDTO result = submissionService.reviewSubmission(submissionId, request, reviewerId);
            String message = request.getReviewStatus().name().equals("APPROVED")
                    ? "审核通过" : "审核拒绝";
            return R.ok(result, message);
        } catch (IllegalArgumentException e) {
            log.warn("审核任务失败: {}", e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    /**
     * 撤回提交
     * 业务场景：提交人在审核前撤回提交
     */
    @PutMapping("/{submissionId}/revoke")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "撤回提交", description = "提交人主动撤回待审核的提交")
    @OperationLog(module = "任务提交", type = OperationType.UPDATE, description = "撤回提交", recordParams = true, recordResult = false)
    @Idempotent(type = IdempotentType.SPEL, key = "#submissionId", timeout = 1, message = "撤回中，请勿重复操作")
    public R<TaskSubmissionDTO> revokeSubmission(
            @PathVariable @Parameter(description = "提交记录ID") Long submissionId) {

        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            log.warn("撤回提交失败: 用户未登录");
            return R.fail("请先登录");
        }

        log.info("用户[{}]撤回提交记录[{}]", userId, submissionId);

        try {
            TaskSubmissionDTO result = submissionService.revokeSubmission(submissionId, userId);
            return R.ok(result, "撤回成功");
        } catch (IllegalArgumentException e) {
            log.warn("撤回提交失败: {}", e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    // ==================== 查询接口 ====================

    /**
     * 获取提交记录详情
     */
    @GetMapping("/{submissionId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取提交记录详情", description = "查询提交记录的详细信息")
    @OperationLog(module = "任务提交", type = OperationType.QUERY, description = "查询提交记录详情", recordParams = true, recordResult = false)
    public R<TaskSubmissionDTO> getSubmissionDetail(
            @PathVariable @Parameter(description = "提交记录ID") Long submissionId) {

        log.debug("查询提交记录详情: submissionId={}", submissionId);

        try {
            TaskSubmissionDTO result = submissionService.getSubmissionDetail(submissionId);
            return R.ok(result);
        } catch (IllegalArgumentException e) {
            log.warn("查询提交记录失败: {}", e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    /**
     * 获取任务的所有提交记录
     */
    @GetMapping("/task/{taskId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取任务提交记录", description = "查询指定任务的所有提交记录（按版本倒序）")
    public R<List<TaskSubmissionDTO>> getTaskSubmissions(
            @PathVariable @Parameter(description = "任务ID") Long taskId) {

        log.debug("查询任务提交记录: taskId={}", taskId);

        try {
            List<TaskSubmissionDTO> results = submissionService.getTaskSubmissions(taskId);
            return R.ok(results);
        } catch (Exception e) {
            log.error("查询任务提交记录失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取任务的最新提交
     */
    @GetMapping("/task/{taskId}/latest")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取任务最新提交", description = "查询任务的最新一次提交记录")
    public R<TaskSubmissionDTO> getLatestSubmission(
            @PathVariable @Parameter(description = "任务ID") Long taskId) {

        log.debug("查询任务最新提交: taskId={}", taskId);

        try {
            TaskSubmissionDTO result = submissionService.getLatestSubmission(taskId);
            return R.ok(result);
        } catch (IllegalArgumentException e) {
            log.warn("查询任务最新提交失败: {}", e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    /**
     * 获取待审核的提交列表
     */
    @GetMapping("/pending")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取待审核提交列表", description = "分页获取所有待审核的提交记录")
    public R<Page<TaskSubmissionDTO>> getPendingSubmissions(
            @RequestParam(defaultValue = "0") @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submissionTime"));

        try {
            Page<TaskSubmissionDTO> results = submissionService.getPendingSubmissions(pageable);
            return R.ok(results);
        } catch (Exception e) {
            log.error("查询待审核提交失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取项目的待审核提交列表
     */
    @GetMapping("/project/{projectId}/pending")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取项目待审核提交", description = "分页获取指定项目的待审核提交记录")
    public R<Page<TaskSubmissionDTO>> getProjectPendingSubmissions(
            @PathVariable @Parameter(description = "项目ID") Long projectId,
            @RequestParam(defaultValue = "0") @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submissionTime"));

        try {
            Page<TaskSubmissionDTO> results = submissionService.getProjectPendingSubmissions(projectId, pageable);
            return R.ok(results);
        } catch (Exception e) {
            log.error("查询项目待审核提交失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取我的提交历史
     */
    @GetMapping("/my-submissions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取我的提交历史", description = "分页获取当前用户的所有提交记录")
    public R<Page<TaskSubmissionDTO>> getMySubmissions(
            @RequestParam(defaultValue = "0") @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") int size) {

        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submissionTime"));

        try {
            Page<TaskSubmissionDTO> results = submissionService.getUserSubmissions(userId, pageable);
            return R.ok(results);
        } catch (Exception e) {
            log.error("查询我的提交历史失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    // ==================== 统计接口 ====================

    /**
     * 统计待审核的提交数量
     */
    @GetMapping("/count/pending")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "统计待审核提交数量", description = "统计所有待审核的提交记录数量")
    public R<Long> countPendingSubmissions() {
        try {
            long count = submissionService.countPendingSubmissions();
            return R.ok(count);
        } catch (Exception e) {
            log.error("统计待审核提交失败", e);
            return R.fail("统计失败: " + e.getMessage());
        }
    }

    /**
     * 统计项目的待审核提交数量
     */
    @GetMapping("/count/project/{projectId}/pending")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "统计项目待审核提交数量", description = "统计指定项目的待审核提交记录数量")
    public R<Long> countProjectPendingSubmissions(
            @PathVariable @Parameter(description = "项目ID") Long projectId) {
        try {
            long count = submissionService.countProjectPendingSubmissions(projectId);
            return R.ok(count);
        } catch (Exception e) {
            log.error("统计项目待审核提交失败", e);
            return R.fail("统计失败: " + e.getMessage());
        }
    }

    /**
     * 查询我创建的任务中的待审核提交
     * 业务场景：查看我创建的任务的待审核提交，过滤已删除的项目
     */
    @GetMapping("/my-created-tasks/pending")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "查询我创建的任务的待审核提交", description = "分页查询我创建的任务中的待审核提交记录")
    public R<Page<TaskSubmissionDTO>> getMyCreatedTasksPendingSubmissions(
            @RequestParam(defaultValue = "0") @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") int size) {

        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            log.warn("查询失败: 用户未登录");
            return R.fail("请先登录");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submissionTime"));

        try {
            Page<TaskSubmissionDTO> results = submissionService.getMyCreatedTasksPendingSubmissions(userId, pageable);
            return R.ok(results);
        } catch (Exception e) {
            log.error("查询我创建的任务的待审核提交失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    /**
     * 统计我创建的任务中的待审核提交数量
     */
    @GetMapping("/count/my-created-tasks/pending")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "统计我创建的任务的待审核数量", description = "统计我创建的任务中的待审核提交记录数量")
    public R<Long> countMyCreatedTasksPendingSubmissions() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            log.warn("统计失败: 用户未登录");
            return R.fail("请先登录");
        }

        try {
            long count = submissionService.countMyCreatedTasksPendingSubmissions(userId);
            return R.ok(count);
        } catch (Exception e) {
            log.error("统计我创建的任务的待审核数量失败", e);
            return R.fail("统计失败: " + e.getMessage());
        }
    }
}