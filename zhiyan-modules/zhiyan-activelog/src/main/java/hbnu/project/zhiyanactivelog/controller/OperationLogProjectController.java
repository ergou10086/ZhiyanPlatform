package hbnu.project.zhiyanactivelog.controller;

import hbnu.project.zhiyanactivelog.model.entity.*;
import hbnu.project.zhiyanactivelog.model.vo.UnifiedOperationLogVO;
import hbnu.project.zhiyanactivelog.service.OperationLogExportService;
import hbnu.project.zhiyanactivelog.service.OperationLogMyselfActionService;
import hbnu.project.zhiyanactivelog.service.OperationLogService;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
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
 * 项目内的操作日志控制器
 * 提供操作日志的查询、导出等功能
 *
 * @author ErgouTree
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/zhiyan/activelog")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "项目内操作日志管理", description = "项目内部的操作日志查询、导出等相关接口")
public class OperationLogProjectController {

    private final OperationLogService operationLogService;

    /**
     * 查询项目内所有类型的操作日志（聚合查询）
     */
    @GetMapping("/projects/{projectId}/all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "查询项目内所有操作日志", description = "聚合查询项目内的项目、任务、Wiki、成果操作日志")
    public R<Page<UnifiedOperationLogVO>> getProjectAllLogs(
            @PathVariable @Parameter(description = "项目ID") Long projectId,
            @RequestParam(defaultValue = "0") @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "operationTime"));
        Page<Object> result = operationLogService.getProjectAllLogs(projectId, pageable);

        // 转换为VO
        Page<UnifiedOperationLogVO> voPage = result.map(obj -> (UnifiedOperationLogVO) obj);
        return R.ok(voPage);
    }


    /**
     * 查询项目内项目操作日志（带筛选）
     */
    @GetMapping("/projects/{projectId}/project-logs")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "查询项目内项目操作日志", description = "查询项目内的项目操作日志，支持按操作类型、用户名、时间范围筛选")
    public R<Page<ProjectOperationLog>> getProjectLogs(
            @PathVariable @Parameter(description = "项目ID") Long projectId,
            @RequestParam(required = false) @Parameter(description = "操作类型") String operationType,
            @RequestParam(required = false) @Parameter(description = "用户名") String username,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Parameter(description = "开始时间") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Parameter(description = "结束时间") LocalDateTime endTime,
            @RequestParam(defaultValue = "0") @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") int size
    ){
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "operationTime"));
        Page<ProjectOperationLog> result = operationLogService.getProjectLogs(projectId, operationType, username, startTime, endTime, pageable);

        return R.ok(result);
    }


    /**
     * 查询项目内Wiki操作日志（带筛选）
     */
    @GetMapping("/projects/{projectId}/wiki-logs")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "查询项目内Wiki操作日志", description = "查询项目内的Wiki操作日志，支持按Wiki页面ID、操作类型、用户名、时间范围筛选")
    public R<Page<WikiOperationLog>> getWikiLogs(
            @PathVariable @Parameter(description = "项目ID") Long projectId,
            @RequestParam(required = false) @Parameter(description = "Wiki页面ID") Long wikiPageId,
            @RequestParam(required = false) @Parameter(description = "操作类型") String operationType,
            @RequestParam(required = false) @Parameter(description = "用户名") String username,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Parameter(description = "开始时间") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Parameter(description = "结束时间") LocalDateTime endTime,
            @RequestParam(defaultValue = "0") @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "operationTime"));
        Page<WikiOperationLog> result = operationLogService.getWikiLogs(
                projectId, wikiPageId, operationType, username, startTime, endTime, pageable);
        return R.ok(result);
    }


    /**
     * 查询项目内成果操作日志（带筛选）
     */
    @GetMapping("/projects/{projectId}/achievement-logs")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "查询项目内成果操作日志", description = "查询项目内的成果操作日志，支持按成果ID、操作类型、用户名、时间范围筛选")
    public R<Page<AchievementOperationLog>> getAchievementLogs(
            @PathVariable @Parameter(description = "项目ID") Long projectId,
            @RequestParam(required = false) @Parameter(description = "成果ID") Long achievementId,
            @RequestParam(required = false) @Parameter(description = "操作类型") String operationType,
            @RequestParam(required = false) @Parameter(description = "用户名") String username,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Parameter(description = "开始时间") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Parameter(description = "结束时间") LocalDateTime endTime,
            @RequestParam(defaultValue = "0") @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "operationTime"));
        Page<AchievementOperationLog> result = operationLogService.getAchievementLogs(
                projectId, achievementId, operationType, username, startTime, endTime, pageable);
        return R.ok(result);
    }
}
