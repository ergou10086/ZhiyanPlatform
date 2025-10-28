package hbnu.project.zhiyanproject.controller;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import hbnu.project.zhiyanproject.model.dto.TaskBoardDTO;
import hbnu.project.zhiyanproject.model.dto.TaskDetailDTO;
import hbnu.project.zhiyanproject.model.entity.Tasks;
import hbnu.project.zhiyanproject.model.enums.TaskPriority;
import hbnu.project.zhiyanproject.model.enums.TaskStatus;
import hbnu.project.zhiyanproject.model.form.CreateTaskRequest;
import hbnu.project.zhiyanproject.model.form.UpdateTaskRequest;
import hbnu.project.zhiyanproject.service.TaskService;
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
 * 任务控制器
 * 根据产品设计文档完整实现任务管理功能
 *
 * @author ErgouTree
 */
@Slf4j
@RestController
@RequestMapping("/api/projects/tasks")
@RequiredArgsConstructor
@Tag(name = "任务管理", description = "任务管理相关接口，包括任务创建、分配、状态更新、看板视图等")
@SecurityRequirement(name = "Bearer Authentication")
public class TaskController {

    private final TaskService taskService;

    // ==================== 任务创建与管理接口 ====================

    /**
     * 创建任务
     * 业务场景：项目成员在任务看板点击"+"创建新任务
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "创建任务", description = "在项目中创建新任务，可指定执行者、优先级、截止日期等")
    public R<Tasks> createTask(@Valid @RequestBody CreateTaskRequest request) {
        Long currentUserId = SecurityUtils.getUserId();
        log.info("用户[{}]在项目[{}]中创建任务: {}", currentUserId, request.getProjectId(), request.getTitle());

        try {
            Tasks task = taskService.createTask(request, currentUserId);
            return R.ok(task, "任务创建成功");
        } catch (IllegalArgumentException e) {
            log.warn("创建任务失败: {}", e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    /**
     * 更新任务
     * 业务场景：编辑任务详情
     */
    @PutMapping("/{taskId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "更新任务", description = "更新任务的标题、描述、状态、优先级等信息")
    public R<Tasks> updateTask(
            @PathVariable @Parameter(description = "任务ID") Long taskId,
            @Valid @RequestBody UpdateTaskRequest request) {

        Long currentUserId = SecurityUtils.getUserId();
        log.info("用户[{}]更新任务[{}]", currentUserId, taskId);

        try {
            Tasks task = taskService.updateTask(taskId, request, currentUserId);
            return R.ok(task, "任务更新成功");
        } catch (IllegalArgumentException e) {
            log.warn("更新任务失败: {}", e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    /**
     * 删除任务（软删除）
     * 业务场景：任务创建者或项目负责人删除任务
     */
    @DeleteMapping("/{taskId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "删除任务", description = "软删除任务，仅任务创建者或项目负责人可操作")
    public R<Void> deleteTask(@PathVariable @Parameter(description = "任务ID") Long taskId) {
        Long currentUserId = SecurityUtils.getUserId();
        log.info("用户[{}]删除任务[{}]", currentUserId, taskId);

        try {
            taskService.deleteTask(taskId, currentUserId);
            return R.ok(null, "任务已删除");
        } catch (IllegalArgumentException e) {
            log.warn("删除任务失败: {}", e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    /**
     * 更新任务状态
     * 业务场景：在任务看板拖拽任务卡片到不同列，或点击状态按钮
     */
    @PatchMapping("/{taskId}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "更新任务状态", description = "更新任务的状态（TODO/IN_PROGRESS/BLOCKED/DONE）")
    public R<Tasks> updateTaskStatus(
            @PathVariable @Parameter(description = "任务ID") Long taskId,
            @Valid @RequestBody hbnu.project.zhiyanproject.model.form.UpdateTaskStatusRequest request) {

        Long currentUserId = SecurityUtils.getUserId();
        log.info("用户[{}]更新任务[{}]状态为: {}", currentUserId, taskId, request.getStatus());

        try {
            Tasks task = taskService.updateTaskStatus(taskId, request.getStatus(), currentUserId);
            return R.ok(task, "状态已更新");
        } catch (IllegalArgumentException e) {
            log.warn("更新任务状态失败: {}", e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    /**
     * 分配任务
     * 业务场景：重新分配任务执行者
     */
    @PutMapping("/{taskId}/assign")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "分配任务", description = "将任务分配给项目成员")
    public R<Tasks> assignTask(
            @PathVariable @Parameter(description = "任务ID") Long taskId,
            @RequestBody @Parameter(description = "执行者ID列表") List<Long> assigneeIds) {

        Long currentUserId = SecurityUtils.getUserId();
        log.info("用户[{}]分配任务[{}]给: {}", currentUserId, taskId, assigneeIds);

        try {
            Tasks task = taskService.assignTask(taskId, assigneeIds, currentUserId);
            return R.ok(task, "任务已分配");
        } catch (IllegalArgumentException e) {
            log.warn("分配任务失败: {}", e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    // ==================== 任务查询接口 ====================

    /**
     * 获取任务详情
     * 业务场景：点击任务卡片查看详情
     */
    @GetMapping("/{taskId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取任务详情", description = "获取任务的详细信息，包括执行者、创建者等")
    public R<TaskDetailDTO> getTaskDetail(@PathVariable @Parameter(description = "任务ID") Long taskId) {
        log.debug("查询任务详情: taskId={}", taskId);

        try {
            TaskDetailDTO task = taskService.getTaskDetail(taskId);
            return R.ok(task);
        } catch (IllegalArgumentException e) {
            log.warn("获取任务详情失败: {}", e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    /**
     * 获取项目任务看板
     * 业务场景：在项目详情页的"任务"标签页展示看板视图
     */
    @GetMapping("/projects/{projectId}/board")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取任务看板", description = "获取项目的任务看板数据，按状态分组")
    public R<TaskBoardDTO> getTaskBoard(@PathVariable @Parameter(description = "项目ID") Long projectId) {
        Long currentUserId = SecurityUtils.getUserId();
        log.info("用户[{}]查看项目[{}]的任务看板", currentUserId, projectId);

        try {
            TaskBoardDTO board = taskService.getProjectTaskBoard(projectId);
            return R.ok(board);
        } catch (Exception e) {
            log.error("获取任务看板失败", e);
            return R.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取项目的所有任务（分页）
     */
    @GetMapping("/projects/{projectId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取项目任务列表", description = "分页获取项目的所有任务")
    public R<Page<TaskDetailDTO>> getProjectTasks(
            @PathVariable @Parameter(description = "项目ID") Long projectId,
            @RequestParam(defaultValue = "0") @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") int size,
            @RequestParam(defaultValue = "createdAt") @Parameter(description = "排序字段") String sortBy,
            @RequestParam(defaultValue = "DESC") @Parameter(description = "排序方向") String direction) {

        Sort.Direction sortDirection = "ASC".equalsIgnoreCase(direction) 
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        try {
            Page<TaskDetailDTO> tasks = taskService.getProjectTasks(projectId, pageable);
            return R.ok(tasks);
        } catch (Exception e) {
            log.error("获取项目任务列表失败", e);
            return R.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 根据状态获取项目任务
     */
    @GetMapping("/projects/{projectId}/status/{status}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "按状态获取任务", description = "根据任务状态筛选项目任务")
    public R<Page<TaskDetailDTO>> getTasksByStatus(
            @PathVariable @Parameter(description = "项目ID") Long projectId,
            @PathVariable @Parameter(description = "任务状态") TaskStatus status,
            @RequestParam(defaultValue = "0") @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        try {
            Page<TaskDetailDTO> tasks = taskService.getTasksByStatus(projectId, status, pageable);
            return R.ok(tasks);
        } catch (Exception e) {
            log.error("按状态获取任务失败", e);
            return R.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 根据优先级获取项目任务
     */
    @GetMapping("/projects/{projectId}/priority/{priority}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "按优先级获取任务", description = "根据任务优先级筛选项目任务")
    public R<Page<TaskDetailDTO>> getTasksByPriority(
            @PathVariable @Parameter(description = "项目ID") Long projectId,
            @PathVariable @Parameter(description = "优先级") TaskPriority priority,
            @RequestParam(defaultValue = "0") @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        try {
            Page<TaskDetailDTO> tasks = taskService.getTasksByPriority(projectId, priority, pageable);
            return R.ok(tasks);
        } catch (Exception e) {
            log.error("按优先级获取任务失败", e);
            return R.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取分配给我的任务
     * 业务场景：查看自己的任务列表
     */
    @GetMapping("/my-assigned")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取我的任务", description = "获取分配给当前用户的所有任务")
    public R<Page<TaskDetailDTO>> getMyAssignedTasks(
            @RequestParam(defaultValue = "0") @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") int size) {

        Long currentUserId = SecurityUtils.getUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        try {
            Page<TaskDetailDTO> tasks = taskService.getMyAssignedTasks(currentUserId, pageable);
            return R.ok(tasks);
        } catch (Exception e) {
            log.error("获取我的任务失败", e);
            return R.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取我创建的任务
     */
    @GetMapping("/my-created")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取我创建的任务", description = "获取当前用户创建的所有任务")
    public R<Page<TaskDetailDTO>> getMyCreatedTasks(
            @RequestParam(defaultValue = "0") @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") int size) {

        Long currentUserId = SecurityUtils.getUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        try {
            Page<TaskDetailDTO> tasks = taskService.getMyCreatedTasks(currentUserId, pageable);
            return R.ok(tasks);
        } catch (Exception e) {
            log.error("获取我创建的任务失败", e);
            return R.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 搜索任务
     * 业务场景：根据关键词搜索项目任务
     */
    @GetMapping("/projects/{projectId}/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "搜索任务", description = "根据关键词搜索项目任务（标题或描述）")
    public R<Page<TaskDetailDTO>> searchTasks(
            @PathVariable @Parameter(description = "项目ID") Long projectId,
            @RequestParam @Parameter(description = "搜索关键词", required = true) String keyword,
            @RequestParam(defaultValue = "0") @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        try {
            Page<TaskDetailDTO> tasks = taskService.searchTasks(projectId, keyword, pageable);
            return R.ok(tasks);
        } catch (Exception e) {
            log.error("搜索任务失败", e);
            return R.fail("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 获取即将到期的任务
     */
    @GetMapping("/projects/{projectId}/upcoming")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取即将到期的任务", description = "获取指定天数内即将到期的任务")
    public R<Page<TaskDetailDTO>> getUpcomingTasks(
            @PathVariable @Parameter(description = "项目ID") Long projectId,
            @RequestParam(defaultValue = "7") @Parameter(description = "未来天数") int days,
            @RequestParam(defaultValue = "0") @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "dueDate"));

        try {
            Page<TaskDetailDTO> tasks = taskService.getUpcomingTasks(projectId, days, pageable);
            return R.ok(tasks);
        } catch (Exception e) {
            log.error("获取即将到期的任务失败", e);
            return R.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取已逾期的任务
     */
    @GetMapping("/projects/{projectId}/overdue")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取已逾期的任务", description = "获取项目中已逾期且未完成的任务")
    public R<Page<TaskDetailDTO>> getOverdueTasks(
            @PathVariable @Parameter(description = "项目ID") Long projectId,
            @RequestParam(defaultValue = "0") @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dueDate"));

        try {
            Page<TaskDetailDTO> tasks = taskService.getOverdueTasks(projectId, pageable);
            return R.ok(tasks);
        } catch (Exception e) {
            log.error("获取逾期任务失败", e);
            return R.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取我的即将到期的任务（所有参与项目）
     * 业务场景：首页提醒用户快要截止的任务
     */
    @GetMapping("/my-upcoming")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取我的即将到期任务", description = "获取当前用户在所有参与项目中即将到期的任务")
    public R<Page<TaskDetailDTO>> getMyUpcomingTasks(
            @RequestParam(defaultValue = "7") @Parameter(description = "未来天数") int days,
            @RequestParam(defaultValue = "0") @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "10") @Parameter(description = "每页大小") int size) {

        Long currentUserId = SecurityUtils.getUserId();
        Pageable pageable = PageRequest.of(page, size);

        try {
            Page<TaskDetailDTO> tasks = taskService.getMyUpcomingTasks(currentUserId, days, pageable);
            return R.ok(tasks);
        } catch (Exception e) {
            log.error("获取我的即将到期任务失败", e);
            return R.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取我的已逾期任务（所有参与项目）
     * 业务场景：首页提醒用户已逾期的任务
     */
    @GetMapping("/my-overdue")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取我的已逾期任务", description = "获取当前用户在所有参与项目中已逾期的任务")
    public R<Page<TaskDetailDTO>> getMyOverdueTasks(
            @RequestParam(defaultValue = "0") @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "10") @Parameter(description = "每页大小") int size) {

        Long currentUserId = SecurityUtils.getUserId();
        Pageable pageable = PageRequest.of(page, size);

        try {
            Page<TaskDetailDTO> tasks = taskService.getMyOverdueTasks(currentUserId, pageable);
            return R.ok(tasks);
        } catch (Exception e) {
            log.error("获取我的已逾期任务失败", e);
            return R.fail("获取失败: " + e.getMessage());
        }
    }

    // ==================== 统计接口 ====================

    /**
     * 统计项目任务数量
     */
    @GetMapping("/projects/{projectId}/count")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "统计项目任务", description = "统计项目中的任务总数")
    public R<Long> countProjectTasks(@PathVariable @Parameter(description = "项目ID") Long projectId) {
        try {
            long count = taskService.countProjectTasks(projectId);
            return R.ok(count);
        } catch (Exception e) {
            log.error("统计项目任务失败", e);
            return R.fail("统计失败: " + e.getMessage());
        }
    }

    /**
     * 统计指定状态的任务数量
     */
    @GetMapping("/projects/{projectId}/count/status/{status}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "按状态统计任务", description = "统计项目中指定状态的任务数量")
    public R<Long> countTasksByStatus(
            @PathVariable @Parameter(description = "项目ID") Long projectId,
            @PathVariable @Parameter(description = "任务状态") TaskStatus status) {

        try {
            long count = taskService.countTasksByStatus(projectId, status);
            return R.ok(count);
        } catch (Exception e) {
            log.error("按状态统计任务失败", e);
            return R.fail("统计失败: " + e.getMessage());
        }
    }

    /**
     * 统计逾期任务数量
     */
    @GetMapping("/projects/{projectId}/count/overdue")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "统计逾期任务", description = "统计项目中已逾期的任务数量")
    public R<Long> countOverdueTasks(@PathVariable @Parameter(description = "项目ID") Long projectId) {
        try {
            long count = taskService.countOverdueTasks(projectId);
            return R.ok(count);
        } catch (Exception e) {
            log.error("统计逾期任务失败", e);
            return R.fail("统计失败: " + e.getMessage());
        }
    }
}
