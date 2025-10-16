package hbnu.project.zhiyanproject.controller;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import hbnu.project.zhiyanproject.model.entity.Tasks;
import hbnu.project.zhiyanproject.model.enums.TaskPriority;
import hbnu.project.zhiyanproject.model.enums.TaskStatus;
import hbnu.project.zhiyanproject.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

import java.time.LocalDate;

/**
 * 任务控制器
 * 基于角色的权限控制
 *
 * @author Tokito
 */
@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "任务管理", description = "任务管理相关接口")
public class TaskController {

    private final TaskService taskService;

    /**
     * 创建任务
     * 权限要求：项目成员
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'DEVELOPER')")
    @Operation(summary = "创建任务", description = "在项目中创建新任务")
    public R<Tasks> createTask(
            @Parameter(description = "项目ID") @RequestParam Long projectId,
            @Parameter(description = "任务标题") @RequestParam String title,
            @Parameter(description = "任务描述") @RequestParam(required = false) String description,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) String assigneeIds,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate) {
        
        Long creatorId = SecurityUtils.getUserId();
        log.info("用户[{}]在项目[{}]中创建任务: {}", creatorId, projectId, title);
        
        return taskService.createTask(projectId, title, description, priority, assigneeIds, dueDate, creatorId);
    }

    /**
     * 更新任务
     * 权限要求：项目成员
     */
    @PutMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'DEVELOPER')")
    @Operation(summary = "更新任务", description = "更新任务信息")
    public R<Tasks> updateTask(
            @Parameter(description = "任务ID") @PathVariable Long taskId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) String assigneeIds,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate) {
        
        Long userId = SecurityUtils.getUserId();
        log.info("用户[{}]更新任务[{}]", userId, taskId);
        
        return taskService.updateTask(taskId, title, description, status, priority, assigneeIds, dueDate);
    }

    /**
     * 删除任务（软删除）
     * 权限要求：任务创建者或项目拥有者
     */
    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'DEVELOPER')")
    @Operation(summary = "删除任务", description = "软删除任务")
    public R<Void> deleteTask(@PathVariable Long taskId) {
        Long userId = SecurityUtils.getUserId();
        log.info("用户[{}]删除任务[{}]", userId, taskId);
        
        return taskService.deleteTask(taskId, userId);
    }

    /**
     * 根据ID获取任务
     * 权限要求：已登录用户
     */
    @GetMapping("/{taskId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取任务详情", description = "根据ID获取任务详细信息")
    public R<Tasks> getTaskById(@PathVariable Long taskId) {
        Long userId = SecurityUtils.getUserId();
        log.debug("用户[{}]查询任务[{}]", userId, taskId);
        
        return taskService.getTaskById(taskId);
    }

    /**
     * 获取项目的所有任务
     * 权限要求：已登录用户
     */
    @GetMapping("/project/{projectId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取项目任务", description = "获取指定项目的所有任务")
    public R<Page<Tasks>> getProjectTasks(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        Sort.Direction sortDirection = "ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return taskService.getProjectTasks(projectId, pageable);
    }

    /**
     * 根据状态获取项目任务
     * 权限要求：已登录用户
     */
    @GetMapping("/project/{projectId}/status/{status}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "按状态获取项目任务", description = "根据任务状态筛选项目任务")
    public R<Page<Tasks>> getTasksByStatus(
            @PathVariable Long projectId,
            @PathVariable TaskStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return taskService.getTasksByStatus(projectId, status, pageable);
    }

    /**
     * 根据优先级获取项目任务
     * 权限要求：已登录用户
     */
    @GetMapping("/project/{projectId}/priority/{priority}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "按优先级获取项目任务", description = "根据任务优先级筛选项目任务")
    public R<Page<Tasks>> getTasksByPriority(
            @PathVariable Long projectId,
            @PathVariable TaskPriority priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return taskService.getTasksByPriority(projectId, priority, pageable);
    }

    /**
     * 获取当前用户创建的任务
     * 权限要求：已登录用户
     */
    @GetMapping("/my-created")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取我创建的任务", description = "获取当前用户创建的所有任务")
    public R<Page<Tasks>> getMyCreatedTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Long userId = SecurityUtils.getUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        return taskService.getUserCreatedTasks(userId, pageable);
    }

    /**
     * 获取用户创建的任务
     * 权限要求：已登录用户
     */
    @GetMapping("/user/{userId}/created")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取用户创建的任务", description = "获取指定用户创建的任务")
    public R<Page<Tasks>> getUserCreatedTasks(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return taskService.getUserCreatedTasks(userId, pageable);
    }

    /**
     * 获取分配给当前用户的任务
     * 权限要求：已登录用户
     */
    @GetMapping("/my-assigned")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取我的任务", description = "获取分配给当前用户的所有任务")
    public R<Page<Tasks>> getMyAssignedTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Long userId = SecurityUtils.getUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        return taskService.getUserAssignedTasks(userId, pageable);
    }

    /**
     * 获取分配给用户的任务
     * 权限要求：已登录用户
     */
    @GetMapping("/user/{userId}/assigned")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取用户任务", description = "获取分配给指定用户的任务")
    public R<Page<Tasks>> getUserAssignedTasks(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return taskService.getUserAssignedTasks(userId, pageable);
    }

    /**
     * 更新任务状态
     * 权限要求：项目成员
     */
    @PatchMapping("/{taskId}/status")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'DEVELOPER')")
    @Operation(summary = "更新任务状态", description = "更新任务的状态")
    public R<Tasks> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestParam TaskStatus status) {
        
        Long userId = SecurityUtils.getUserId();
        log.info("用户[{}]更新任务[{}]状态为: {}", userId, taskId, status);
        
        return taskService.updateTaskStatus(taskId, status);
    }

    /**
     * 分配任务
     * 权限要求：项目成员
     */
    @PostMapping("/{taskId}/assign")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'DEVELOPER')")
    @Operation(summary = "分配任务", description = "将任务分配给指定用户")
    public R<Tasks> assignTask(
            @PathVariable Long taskId,
            @RequestParam String assigneeIds) {
        
        Long userId = SecurityUtils.getUserId();
        log.info("用户[{}]分配任务[{}]给: {}", userId, taskId, assigneeIds);
        
        return taskService.assignTask(taskId, assigneeIds, userId);
    }

    /**
     * 搜索任务
     * 权限要求：已登录用户
     */
    @GetMapping("/project/{projectId}/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "搜索任务", description = "根据关键字搜索项目任务")
    public R<Page<Tasks>> searchTasks(
            @PathVariable Long projectId,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return taskService.searchTasks(projectId, keyword, pageable);
    }

    /**
     * 统计项目任务数量
     * 权限要求：已登录用户
     */
    @GetMapping("/project/{projectId}/count")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "统计项目任务", description = "统计项目中的任务数量")
    public R<Long> countProjectTasks(@PathVariable Long projectId) {
        return taskService.countProjectTasks(projectId);
    }

    /**
     * 统计项目中指定状态的任务数量
     * 权限要求：已登录用户
     */
    @GetMapping("/project/{projectId}/count/status/{status}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "按状态统计任务", description = "统计项目中指定状态的任务数量")
    public R<Long> countTasksByStatus(
            @PathVariable Long projectId,
            @PathVariable TaskStatus status) {
        return taskService.countTasksByStatus(projectId, status);
    }
}
