package hbnu.project.zhiyanactivelog.controller;

import hbnu.project.zhiyanactivelog.service.OperationLogExportService;
import hbnu.project.zhiyanactivelog.service.OperationLogService;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 日志导出控制器
 *
 * @author ErgouTree
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/zhiyan/activelog/export")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "日志导出管理", description = "提供对操作日志导出的相关接口")
public class OperationLogExportController {

    private final OperationLogExportService exportService;


    /**
     * 导出项目操作日志
     */
    @GetMapping("/projects/{projectId}/project-logs")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "导出项目操作日志", description = "导出指定项目的操作日志，支持按操作类型、用户名、时间范围筛选，可指定导出条数")
    public void exportProjectLogs(
            @PathVariable @Parameter(description = "项目ID") Long projectId,
            @RequestParam(required = false) @Parameter(description = "操作类型") String operationType,
            @RequestParam(required = false) @Parameter(description = "用户名") String username,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Parameter(description = "开始时间") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Parameter(description = "结束时间") LocalDateTime endTime,
            @RequestParam(required = false) @Parameter(description = "导出条数限制，不传则导出全部") Integer limit,
            HttpServletResponse response
    ) {
        exportService.exportProjectLogs(projectId, operationType, username, startTime, endTime, limit, response);
    }


    /**
     * 导出我的操作日志
     */
    @GetMapping("/my-logs")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "导出我的操作日志", description = "导出当前用户的所有操作日志，支持按时间范围筛选，可指定导出条数")
    public void exportMyLogs(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Parameter(description = "开始时间") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Parameter(description = "结束时间") LocalDateTime endTime,
            @RequestParam(required = false) @Parameter(description = "导出条数限制，不传则导出全部") Integer limit,
            HttpServletResponse response
    ) {
        Long userId = SecurityUtils.getUserId();
        exportService.exportMyLogs(userId, startTime, endTime, limit, response);
    }



    /**
     * 导出我在指定项目内的操作日志
     */
    @GetMapping("/projects/{projectId}/my-logs")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "导出项目内我的操作日志", description = "导出当前用户在指定项目内的所有操作日志，可指定导出条数")
    public void exportMyProjectLogs(
            @PathVariable @Parameter(description = "项目ID") Long projectId,
            @RequestParam(required = false) @Parameter(description = "导出条数限制，不传则导出全部") Integer limit,
            HttpServletResponse response
    ) {
        Long userId = SecurityUtils.getUserId();
        exportService.exportMyProjectLogs(userId, projectId, limit, response);
    }
}
