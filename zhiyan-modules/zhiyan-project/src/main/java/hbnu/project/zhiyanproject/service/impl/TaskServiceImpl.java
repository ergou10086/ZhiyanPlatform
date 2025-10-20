package hbnu.project.zhiyanproject.service.impl;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyanproject.model.entity.Project;
import hbnu.project.zhiyanproject.model.entity.Tasks;
import hbnu.project.zhiyanproject.model.enums.TaskPriority;
import hbnu.project.zhiyanproject.model.enums.TaskStatus;
import hbnu.project.zhiyanproject.repository.ProjectRepository;
import hbnu.project.zhiyanproject.repository.TaskRepository;
import hbnu.project.zhiyanproject.service.ProjectMemberService;
import hbnu.project.zhiyanproject.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

/**
 * 任务服务实现类
 *
 * @author Tokito
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberService projectMemberService;

    @Override
    @Transactional
    public R<Tasks> createTask(Long projectId, String title, String description,
                               TaskPriority priority, String assigneeIds, LocalDate dueDate, Long creatorId) {
        try {
            // 验证项目是否存在
            Project project = projectRepository.findById(projectId).orElse(null);
            if (project == null) {
                return R.fail("项目不存在");
            }

            // 检查用户是否有创建任务的权限
            if (!projectMemberService.isMember(projectId, creatorId)) {
                return R.fail("只有项目成员才能创建任务");
            }

            // 创建任务
            Tasks task = Tasks.builder()
                    .projectId(projectId)
                    .title(title)
                    .description(description)
                    .status(TaskStatus.TODO)
                    .priority(priority != null ? priority : TaskPriority.MEDIUM)
                    .assigneeId(assigneeIds)
                    .dueDate(dueDate)
                    .createdBy(creatorId)
                    .build();

            task = taskRepository.save(task);
            log.info("成功创建任务: id={}, title={}, projectId={}, creator={}",
                    task.getId(), title, projectId, creatorId);
            return R.ok(task, "任务创建成功");
        } catch (Exception e) {
            log.error("创建任务失败: projectId={}, title={}, creatorId={}", projectId, title, creatorId, e);
            return R.fail("任务创建失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public R<Tasks> updateTask(Long taskId, String title, String description,
                               TaskStatus status, TaskPriority priority, String assigneeIds, LocalDate dueDate) {
        try {
            Tasks task = taskRepository.findById(taskId).orElse(null);
            if (task == null) {
                return R.fail("任务不存在");
            }

            // 更新任务信息
            if (StringUtils.hasText(title)) {
                task.setTitle(title);
            }

            if (StringUtils.hasText(description)) {
                task.setDescription(description);
            }

            if (status != null) {
                task.setStatus(status);
            }

            if (priority != null) {
                task.setPriority(priority);
            }

            if (StringUtils.hasText(assigneeIds)) {
                task.setAssigneeId(assigneeIds);
            }

            if (dueDate != null) {
                task.setDueDate(dueDate);
            }

            task = taskRepository.save(task);
            log.info("成功更新任务: id={}, title={}", taskId, task.getTitle());
            return R.ok(task, "任务更新成功");
        } catch (Exception e) {
            log.error("更新任务失败: taskId={}", taskId, e);
            return R.fail("任务更新失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public R<Void> deleteTask(Long taskId, Long userId) {
        try {
            Tasks task = taskRepository.findById(taskId).orElse(null);
            if (task == null) {
                return R.fail("任务不存在");
            }

            // 检查是否为任务创建者或项目拥有者
            if (!task.getCreatedBy().equals(userId)) {
                Project project = projectRepository.findById(task.getProjectId()).orElse(null);
                if (project == null || !project.isOwner(userId)) {
                    return R.fail("只有任务创建者或项目拥有者才能删除任务");
                }
            }

            // 软删除：只标记为已删除
            task.setIsDeleted(true);
            taskRepository.save(task);

            log.info("成功删除任务（软删除）: id={}, userId={}", taskId, userId);
            return R.ok(null, "任务删除成功");
        } catch (Exception e) {
            log.error("删除任务失败: taskId={}, userId={}", taskId, userId, e);
            return R.fail("任务删除失败: " + e.getMessage());
        }
    }

    @Override
    public R<Tasks> getTaskById(Long taskId) {
        try {
            Tasks task = taskRepository.findById(taskId).orElse(null);
            if (task == null) {
                return R.fail("任务不存在");
            }
            return R.ok(task);
        } catch (Exception e) {
            log.error("获取任务失败: taskId={}", taskId, e);
            return R.fail("获取任务失败: " + e.getMessage());
        }
    }

    @Override
    public R<Page<Tasks>> getProjectTasks(Long projectId, Pageable pageable) {
        try {
            Page<Tasks> tasks = taskRepository.findByProjectId(projectId, pageable);
            return R.ok(tasks);
        } catch (Exception e) {
            log.error("获取项目任务列表失败: projectId={}", projectId, e);
            return R.fail("获取任务列表失败: " + e.getMessage());
        }
    }

    @Override
    public R<Page<Tasks>> getTasksByStatus(Long projectId, TaskStatus status, Pageable pageable) {
        try {
            Page<Tasks> tasks = taskRepository.findByProjectIdAndStatus(projectId, status, pageable);
            return R.ok(tasks);
        } catch (Exception e) {
            log.error("根据状态获取任务列表失败: projectId={}, status={}", projectId, status, e);
            return R.fail("获取任务列表失败: " + e.getMessage());
        }
    }

    @Override
    public R<Page<Tasks>> getTasksByPriority(Long projectId, TaskPriority priority, Pageable pageable) {
        try {
            Page<Tasks> tasks = taskRepository.findByProjectIdAndPriority(projectId, priority, pageable);
            return R.ok(tasks);
        } catch (Exception e) {
            log.error("根据优先级获取任务列表失败: projectId={}, priority={}", projectId, priority, e);
            return R.fail("获取任务列表失败: " + e.getMessage());
        }
    }

    @Override
    public R<Page<Tasks>> getUserCreatedTasks(Long userId, Pageable pageable) {
        try {
            Page<Tasks> tasks = taskRepository.findByCreatedBy(userId, pageable);
            return R.ok(tasks);
        } catch (Exception e) {
            log.error("获取用户创建的任务列表失败: userId={}", userId, e);
            return R.fail("获取任务列表失败: " + e.getMessage());
        }
    }



    @Override
    @Transactional
    public R<Tasks> updateTaskStatus(Long taskId, TaskStatus status) {
        try {
            Tasks task = taskRepository.findById(taskId).orElse(null);
            if (task == null) {
                return R.fail("任务不存在");
            }

            task.setStatus(status);
            task = taskRepository.save(task);

            log.info("成功更新任务状态: id={}, status={}", taskId, status);
            return R.ok(task, "任务状态更新成功");
        } catch (Exception e) {
            log.error("更新任务状态失败: taskId={}, status={}", taskId, status, e);
            return R.fail("更新任务状态失败: " + e.getMessage());
        }
    }

    @Override
    public R<Page<Tasks>> searchTasks(Long projectId, String keyword, Pageable pageable) {
        try {
            Page<Tasks> tasks = taskRepository.searchByKeyword(projectId, keyword, pageable);
            return R.ok(tasks);
        } catch (Exception e) {
            log.error("搜索任务失败: projectId={}, keyword={}", projectId, keyword, e);
            return R.fail("搜索任务失败: " + e.getMessage());
        }
    }

    /**
     * @Override
    public R<Long> countProjectTasks(Long projectId) {
        try {
            long count = taskRepository.countByProjectId(projectId);
            return R.ok(count);
        } catch (Exception e) {
            log.error("统计项目任务数量失败: projectId={}", projectId, e);
            return R.fail("统计失败: " + e.getMessage());
        }
    }

    @Override
    public R<Long> countTasksByStatus(Long projectId, TaskStatus status) {
        try {
            long count = taskRepository.countByProjectIdAndStatus(projectId, status);
            return R.ok(count);
        } catch (Exception e) {
            log.error("统计项目任务数量失败: projectId={}, status={}", projectId, status, e);
            return R.fail("统计失败: " + e.getMessage());
        }
    }*/
}
