package hbnu.project.zhiyanactivelog.controller;

import hbnu.project.zhiyanactivelog.model.entity.*;
import hbnu.project.zhiyanactivelog.model.vo.UnifiedOperationLogVO;
import hbnu.project.zhiyanactivelog.service.OperationLogMyselfActionplusService;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 我的操作日志控制器
 * 提供操作日志的查询、导出等功能
 *
 * @author ErgouTree
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/zhiyan/activelog")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "我的活动的操作日志管理", description = "提供对我的操作日志查询、导出等相关接口")
public class OperationLogMyActiveController {

    private final OperationLogMyselfActionplusService myselfActionService;

    /**
     * 查询用户在所有项目的所有操作日志（我的活动展示用）
     */
    @GetMapping("/myself/all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "查询我的所有操作日志", description = "聚合查询当前用户在所有项目中的操作日志（包括登录日志）")
    public R<Page<UnifiedOperationLogVO>> getMyAllLogs(
            @RequestParam(defaultValue = "0") @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") int size) {

        Long userId = SecurityUtils.getUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "time"));
        Page<UnifiedOperationLogVO> result = myselfActionService.getProjectAllLogsByMyself(userId, pageable);
        return R.ok(result);
    }


    /**
     * 查询我对项目的相关操作日志
     */
    @GetMapping("/myself/project-logs")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "查询我的项目操作日志", description = "查询当前用户在所有项目中的项目操作日志")
    public R<Page<ProjectOperationLog>> getMyProjectLogs(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Parameter(description = "开始时间") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Parameter(description = "结束时间") LocalDateTime endTime,
            @RequestParam(defaultValue = "0") @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") int size
    ) {
        Long userId = SecurityUtils.getUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "operationTime"));
        Page<ProjectOperationLog> result = myselfActionService.getUserProjectLogs(
                userId, startTime, endTime, pageable);
        return R.ok(result);
    }


    /**
     * 查询我对任务的相关操作日志
     */
    @GetMapping("/myself/task-logs")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "查询我的任务操作日志", description = "查询当前用户在所有项目中的任务操作日志")
    public R<Page<TaskOperationLog>> getMyTaskLogs(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Parameter(description = "开始时间") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Parameter(description = "结束时间") LocalDateTime endTime,
            @RequestParam(defaultValue = "0") @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") int size
    ) {
        Long userId = SecurityUtils.getUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "operationTime"));
        Page<TaskOperationLog> result = myselfActionService.getUserTaskLogs(
                userId, startTime, endTime, pageable);
        return R.ok(result);
    }


    /**
     * 查询我对Wiki相关的操作日志
     */
    @GetMapping("/myself/wiki-logs")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "查询我的Wiki操作日志", description = "查询当前用户在所有项目中的Wiki操作日志")
    public R<Page<WikiOperationLog>> getMyWikiLogs(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Parameter(description = "开始时间") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Parameter(description = "结束时间") LocalDateTime endTime,
            @RequestParam(defaultValue = "0") @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") int size
    ) {
        Long userId = SecurityUtils.getUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "operationTime"));
        Page<WikiOperationLog> result = myselfActionService.getUserWikiLogs(
                userId, startTime, endTime, pageable);
        return R.ok(result);
    }


    /**
     * 查询我对成果的相关操作日志
     */
    @GetMapping("/myself/achievement-logs")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "查询我的成果操作日志", description = "查询当前用户在所有项目中的成果操作日志")
    public R<Page<AchievementOperationLog>> getMyAchievementLogs(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            @Parameter(description = "开始时间") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            @Parameter(description = "结束时间") LocalDateTime endTime,
            @RequestParam(defaultValue = "0") @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") int size) {

        Long userId = SecurityUtils.getUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "operationTime"));
        Page<AchievementOperationLog> result = myselfActionService.getUserAchievementLogs(
                userId, startTime, endTime, pageable);
        return R.ok(result);
    }


    /**
     * 查询我的登录相关日志
     */
    @GetMapping("/myself/login-logs")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "查询我的登录日志", description = "查询当前用户的登录日志")
    public R<Page<LoginLog>> getMyLoginLogs(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            @Parameter(description = "开始时间") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            @Parameter(description = "结束时间") LocalDateTime endTime,
            @RequestParam(defaultValue = "0") @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") int size) {

        Long userId = SecurityUtils.getUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "loginTime"));
        Page<LoginLog> result = myselfActionService.getUserLoginLogs(
                userId, startTime, endTime, pageable);
        return R.ok(result);
    }
}
