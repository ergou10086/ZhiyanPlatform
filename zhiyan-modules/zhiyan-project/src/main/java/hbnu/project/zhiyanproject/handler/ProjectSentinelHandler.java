package hbnu.project.zhiyanproject.handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyanproject.model.dto.TaskBoardDTO;
import hbnu.project.zhiyanproject.model.dto.TaskDetailDTO;
import hbnu.project.zhiyanproject.model.entity.Project;
import hbnu.project.zhiyanproject.model.entity.Tasks;
import hbnu.project.zhiyanproject.model.form.CreateProjectRequest;
import hbnu.project.zhiyanproject.model.form.CreateTaskRequest;
import hbnu.project.zhiyanproject.model.form.UpdateTaskRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;

/**
 * 项目模块 Sentinel 统一处理器
 * <p>
 * 提供流控、降级的统一处理方法
 * </p>
 *
 * @author ErgouTree
 */
@Slf4j
@Component
public class ProjectSentinelHandler {

    // ==================== 项目相关限流处理 ====================

    /**
     * 创建项目 - 限流处理
     */
    public static R<Project> handleCreateProjectBlock(CreateProjectRequest request, BlockException ex) {
        log.warn("[Sentinel] 创建项目接口被限流: {}", ex.getClass().getSimpleName());
        return R.fail("系统繁忙，请稍后再试创建项目");
    }

    /**
     * 创建项目 - 降级处理
     */
    public static R<Project> handleCreateProjectFallback(CreateProjectRequest request, Throwable ex) {
        log.error("[Sentinel] 创建项目接口降级: {}", ex != null ? ex.getMessage() : "未知错误", ex);
        return R.fail("服务暂时不可用，请稍后重试");
    }

    /**
     * 查询项目列表 - 限流处理
     */
    public static R<Page<Project>> handleGetProjectsBlock(
            String searchTerm, String visibility, String status,
            int page, int size, String sortBy, String direction, BlockException ex) {
        log.warn("[Sentinel] 查询项目列表接口被限流");
        return R.fail("访问过于频繁，请稍后再试");
    }

    /**
     * 查询项目详情 - 限流处理
     */
    public static R<Project> handleGetProjectBlock(Long projectId, BlockException ex) {
        log.warn("[Sentinel] 查询项目详情接口被限流, projectId: {}", projectId);
        return R.fail("访问过于频繁，请稍后再试");
    }

    /**
     * 更新项目 - 限流处理
     */
    public static R<Project> handleUpdateProjectBlock(Long projectId, Object request, BlockException ex) {
        log.warn("[Sentinel] 更新项目接口被限流, projectId: {}", projectId);
        return R.fail("操作过于频繁，请稍后再试");
    }

    /**
     * 删除项目 - 限流处理
     */
    public static R<Void> handleDeleteProjectBlock(Long projectId, BlockException ex) {
        log.warn("[Sentinel] 删除项目接口被限流, projectId: {}", projectId);
        return R.fail("操作过于频繁，请稍后再试");
    }

    /**
     * 上传图片 - 限流处理
     */
    public static R<?> handleUploadImageBlock(MultipartFile file, Long projectId, BlockException ex) {
        log.warn("[Sentinel] 上传图片接口被限流");
        return R.fail("上传过于频繁，请稍后再试");
    }

    // ==================== 任务相关限流处理 ====================

    /**
     * 创建任务 - 限流处理
     */
    public static R<Tasks> handleCreateTaskBlock(CreateTaskRequest request, BlockException ex) {
        log.warn("[Sentinel] 创建任务接口被限流");
        return R.fail("操作过于频繁，请稍后再试");
    }

    /**
     * 创建任务 - 降级处理
     */
    public static R<Tasks> handleCreateTaskFallback(CreateTaskRequest request, Throwable ex) {
        String errorMsg = ex != null ? ex.getMessage() : "未知错误";
        String exClass = ex != null ? ex.getClass().getSimpleName() : "Unknown";
        String exType = ex != null ? ex.getClass().getName() : "Unknown";
        
        log.error("[Sentinel] 创建任务接口降级 - 异常类型: {} ({}), 错误信息: {}, 项目ID: {}, 任务标题: {}", 
                exClass, exType, errorMsg, 
                request != null ? request.getProjectId() : "null", 
                request != null ? request.getTitle() : "null", ex);
        
        // 如果是认证异常，返回认证失败的错误信息
        if (ex instanceof AccessDeniedException || 
            ex instanceof AuthenticationException ||
            ex instanceof AuthenticationCredentialsNotFoundException ||
            (exType != null && (exType.contains("AccessDenied") || exType.contains("Authentication")))) {
            log.warn("[Sentinel] 创建任务失败: 用户未认证或Token无效");
            return R.fail(401, "请先登录，Token可能已过期");
        }
        
        // 如果是业务异常，返回具体错误信息
        if (ex instanceof IllegalArgumentException) {
            return R.fail(errorMsg != null && !errorMsg.isEmpty() ? errorMsg : "参数错误，请检查输入");
        }
        
        // 如果异常为null，可能是认证拦截导致的
        if (ex == null) {
            log.warn("[Sentinel] 创建任务失败: 异常为null，可能是认证拦截");
            return R.fail(401, "请先登录，Token可能已过期");
        }
        
        return R.fail("服务暂时不可用，请稍后重试");
    }

    /**
     * 更新任务 - 限流处理
     */
    public static R<Tasks> handleUpdateTaskBlock(Long taskId, UpdateTaskRequest request, BlockException ex) {
        log.warn("[Sentinel] 更新任务接口被限流, taskId: {}", taskId);
        return R.fail("操作过于频繁，请稍后再试");
    }

    /**
     * 删除任务 - 限流处理
     */
    public static R<Void> handleDeleteTaskBlock(Long taskId, BlockException ex) {
        log.warn("[Sentinel] 删除任务接口被限流, taskId: {}", taskId);
        return R.fail("操作过于频繁，请稍后再试");
    }

    /**
     * 查询任务列表 - 限流处理
     */
    public static R<Page<Tasks>> handleGetTasksBlock(
            Long projectId, Object status, Object priority,
            Long assigneeId, Object completed, Object overdue,
            int page, int size, String sortBy, String direction, BlockException ex) {
        log.warn("[Sentinel] 查询任务列表接口被限流");
        return R.fail("访问过于频繁，请稍后再试");
    }

    /**
     * 查询任务详情 - 限流处理
     */
    public static R<TaskDetailDTO> handleGetTaskDetailBlock(Long taskId, BlockException ex) {
        log.warn("[Sentinel] 查询任务详情接口被限流, taskId: {}", taskId);
        return R.fail("访问过于频繁，请稍后再试");
    }

    /**
     * 查询任务看板 - 限流处理
     */
    public static R<TaskBoardDTO> handleGetTaskBoardBlock(Long projectId, BlockException ex) {
        log.warn("[Sentinel] 查询任务看板接口被限流, projectId: {}", projectId);
        // 返回空数据而不是错误，提升用户体验
        TaskBoardDTO emptyBoard = new TaskBoardDTO();
        emptyBoard.setTodoTasks(Collections.emptyList());
        emptyBoard.setInProgressTasks(Collections.emptyList());
        emptyBoard.setBlockedTasks(Collections.emptyList());
        emptyBoard.setDoneTasks(Collections.emptyList());
        return R.ok(emptyBoard, "系统繁忙，请刷新重试");
    }

    /**
     * 查询任务看板 - 降级处理
     */
    public static R<TaskBoardDTO> handleGetTaskBoardFallback(Long projectId, Throwable ex) {
        log.error("[Sentinel] 查询任务看板接口降级, projectId: {}", projectId, ex);
        TaskBoardDTO emptyBoard = new TaskBoardDTO();
        emptyBoard.setTodoTasks(Collections.emptyList());
        emptyBoard.setInProgressTasks(Collections.emptyList());
        emptyBoard.setBlockedTasks(Collections.emptyList());
        emptyBoard.setDoneTasks(Collections.emptyList());
        return R.ok(emptyBoard, "服务暂时不可用，请稍后刷新");
    }

    // ==================== 通用降级处理 ====================

    /**
     * 通用查询列表降级处理
     */
    public static <T> R<Page<T>> handleQueryListFallback(Throwable ex) {
        log.error("[Sentinel] 查询列表接口降级: {}", ex.getMessage());
        Page<T> emptyPage = new PageImpl<>(Collections.emptyList());
        return R.ok(emptyPage, "服务暂时不可用，请稍后刷新");
    }

    /**
     * 通用查询单个对象降级处理
     */
    public static <T> R<T> handleQuerySingleFallback(Throwable ex) {
        log.error("[Sentinel] 查询接口降级: {}", ex.getMessage());
        return R.fail("服务暂时不可用，请稍后重试");
    }
}

