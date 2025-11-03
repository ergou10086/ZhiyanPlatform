package hbnu.project.zhiyanproject.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyanproject.client.AuthServiceClient;
import hbnu.project.zhiyanproject.service.UserCacheService;
import hbnu.project.zhiyanproject.model.dto.TaskBoardDTO;
import hbnu.project.zhiyanproject.model.dto.TaskDetailDTO;
import hbnu.project.zhiyanproject.model.dto.UserDTO;
import hbnu.project.zhiyanproject.model.entity.Project;
import hbnu.project.zhiyanproject.model.entity.Tasks;
import hbnu.project.zhiyanproject.model.enums.TaskPriority;
import hbnu.project.zhiyanproject.model.enums.TaskStatus;
import hbnu.project.zhiyanproject.model.form.CreateTaskRequest;
import hbnu.project.zhiyanproject.model.form.UpdateTaskRequest;
import hbnu.project.zhiyanproject.repository.ProjectRepository;
import hbnu.project.zhiyanproject.repository.TaskRepository;
import hbnu.project.zhiyanproject.service.ProjectMemberService;
import hbnu.project.zhiyanproject.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务服务实现类
 * 根据产品设计文档完整实现任务管理功能
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberService projectMemberService;
    private final AuthServiceClient authServiceClient;
    private final UserCacheService userCacheService;
    private final ObjectMapper objectMapper;

    // ==================== 任务创建与管理 ====================

    @Override
    @Transactional
    public Tasks createTask(CreateTaskRequest request, Long creatorId) {
        Long projectId = request.getProjectId();

        // 1. 检查项目是否存在
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("项目不存在"));

        // 2. 检查创建人是否为项目成员
        if (!projectMemberService.isMember(projectId, creatorId)) {
            throw new IllegalArgumentException("只有项目成员才能创建任务");
        }

        // 3. 验证执行者都是项目成员
        List<Long> assigneeIds = request.getAssigneeIds();
        if (assigneeIds != null && !assigneeIds.isEmpty()) {
            for (Long assigneeId : assigneeIds) {
                if (!projectMemberService.isMember(projectId, assigneeId)) {
                    throw new IllegalArgumentException("执行者必须是项目成员");
                }
            }
        }

        // 4. 将执行者列表转换为JSON
        String assigneeIdsJson = convertListToJson(assigneeIds);

        // 5. 创建任务
        Tasks task = Tasks.builder()
                .projectId(projectId)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(TaskStatus.TODO) // 新创建的任务默认为待办
                .priority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM)
                .assigneeId(assigneeIdsJson)
                .dueDate(request.getDueDate())
                .worktime(request.getWorktime())
                .createdBy(creatorId)
                .isDeleted(false)
                .build();

        Tasks saved = taskRepository.save(task);
        log.info("创建任务成功: taskId={}, projectId={}, title={}, creator={}", 
                saved.getId(), projectId, request.getTitle(), creatorId);

        // TODO: 发布"任务已创建"事件到消息队列，通知执行者

        return saved;
    }

    @Override
    @Transactional
    public Tasks updateTask(Long taskId, UpdateTaskRequest request, Long operatorId) {
        // 1. 查询任务
        Tasks task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在"));

        // 2. 检查操作人是否为项目成员
        if (!projectMemberService.isMember(task.getProjectId(), operatorId)) {
            throw new IllegalArgumentException("只有项目成员才能更新任务");
        }

        // 3. 更新任务信息
        boolean updated = false;

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
            updated = true;
        }

        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
            updated = true;
        }

        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
            updated = true;
        }

        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
            updated = true;
        }

        if (request.getAssigneeIds() != null) {
            // 验证新的执行者都是项目成员
            for (Long assigneeId : request.getAssigneeIds()) {
                if (!projectMemberService.isMember(task.getProjectId(), assigneeId)) {
                    throw new IllegalArgumentException("执行者必须是项目成员");
                }
            }
            task.setAssigneeId(convertListToJson(request.getAssigneeIds()));
            updated = true;
        }

        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
            updated = true;
        }

        if (request.getWorktime() != null) {
            task.setWorktime(request.getWorktime());
            updated = true;
        }

        if (!updated) {
            return task;
        }

        Tasks saved = taskRepository.save(task);
        log.info("更新任务成功: taskId={}, operator={}", taskId, operatorId);

        // TODO: 根据更新内容发布相应事件（如状态变更、重新分配等）

        return saved;
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId, Long operatorId) {
        // 1. 查询任务
        Tasks task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在"));

        // 2. 检查权限：任务创建者或项目负责人可以删除
        boolean isCreator = task.getCreatedBy().equals(operatorId);
        boolean isOwner = projectMemberService.isOwner(task.getProjectId(), operatorId);

        if (!isCreator && !isOwner) {
            throw new IllegalArgumentException("只有任务创建者或项目负责人才能删除任务");
        }

        // 3. 软删除
        task.setIsDeleted(true);
        taskRepository.save(task);

        log.info("删除任务成功: taskId={}, operator={}", taskId, operatorId);

        // TODO: 发布"任务已删除"事件
    }

    @Override
    @Transactional
    public Tasks updateTaskStatus(Long taskId, TaskStatus newStatus, Long operatorId) {
        // 1. 查询任务
        Tasks task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在"));

        // 2. 检查操作人是否为项目成员
        if (!projectMemberService.isMember(task.getProjectId(), operatorId)) {
            throw new IllegalArgumentException("只有项目成员才能更新任务状态");
        }

        // 3. 更新状态
        TaskStatus oldStatus = task.getStatus();
        task.setStatus(newStatus);
        Tasks saved = taskRepository.save(task);

        log.info("更新任务状态: taskId={}, {} -> {}, operator={}", 
                taskId, oldStatus, newStatus, operatorId);

        // TODO: 发布"任务状态变更"事件，通知相关人员

        return saved;
    }

    @Override
    @Transactional
    public Tasks assignTask(Long taskId, List<Long> assigneeIds, Long operatorId) {
        // 1. 查询任务
        Tasks task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在"));

        // 2. 检查操作人是否为项目成员
        if (!projectMemberService.isMember(task.getProjectId(), operatorId)) {
            throw new IllegalArgumentException("只有项目成员才能分配任务");
        }

        // 3. 验证新的执行者都是项目成员
        if (assigneeIds != null && !assigneeIds.isEmpty()) {
            for (Long assigneeId : assigneeIds) {
                if (!projectMemberService.isMember(task.getProjectId(), assigneeId)) {
                    throw new IllegalArgumentException("执行者必须是项目成员");
                }
            }
        }

        // 4. 更新执行者
        task.setAssigneeId(convertListToJson(assigneeIds));
        
        // 5. ✅ 如果任务状态是TODO，自动改为IN_PROGRESS
        if (task.getStatus() == TaskStatus.TODO) {
            task.setStatus(TaskStatus.IN_PROGRESS);
            log.info("任务状态自动从TODO更新为IN_PROGRESS");
        }
        
        Tasks saved = taskRepository.save(task);

        log.info("重新分配任务: taskId={}, assigneeIds={}, newStatus={}, operator={}", 
                taskId, assigneeIds, saved.getStatus(), operatorId);

        // TODO: 发布"任务已重新分配"事件，通知新的执行者

        return saved;
    }

    @Override
    @Transactional
    public Tasks claimTask(Long taskId, Long userId) {
        // 1. 查询任务
        Tasks task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在"));

        // 2. 检查用户是否为项目成员
        if (!projectMemberService.isMember(task.getProjectId(), userId)) {
            throw new IllegalArgumentException("只有项目成员才能接取任务");
        }

        // 3. 获取当前执行者列表
        List<Long> currentAssignees = convertJsonToList(task.getAssigneeId());
        // ✅ 创建一个新的可修改列表，避免UnsupportedOperationException
        List<Long> assigneeIds = new ArrayList<>(currentAssignees != null ? currentAssignees : Collections.emptyList());

        // 4. 检查是否已经是执行者
        if (assigneeIds.contains(userId)) {
            throw new IllegalArgumentException("您已经是该任务的执行者");
        }

        // 5. 将当前用户添加到执行者列表
        assigneeIds.add(userId);
        task.setAssigneeId(convertListToJson(assigneeIds));
        
        // 6. 如果任务状态是TODO，自动改为IN_PROGRESS
        if (task.getStatus() == TaskStatus.TODO) {
            task.setStatus(TaskStatus.IN_PROGRESS);
        }
        
        Tasks saved = taskRepository.save(task);

        log.info("用户接取任务: taskId={}, userId={}, currentAssignees={}", 
                taskId, userId, assigneeIds);

        // TODO: 发布"任务已接取"事件，通知项目成员

        return saved;
    }

    // ==================== 任务查询 ====================

    @Override
    public TaskDetailDTO getTaskDetail(Long taskId) {
        Tasks task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在"));

        return convertToDetailDTO(task);
    }

    @Override
    public TaskBoardDTO getProjectTaskBoard(Long projectId) {
        // 1. 检查项目是否存在
        projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("项目不存在"));

        // 2. 查询所有未删除的任务（一次查询）
        List<Tasks> allTasks = taskRepository.findByProjectIdAndIsDeleted(projectId, false);

        // 3. 批量转换所有任务为DTO（只查询一次用户信息，避免重复查询）
        List<TaskDetailDTO> allTaskDTOs = convertListToDetailDTO(allTasks);

        // 4. 按状态分组（在内存中分组，无需额外数据库查询）
        Map<TaskStatus, List<TaskDetailDTO>> tasksByStatus = allTaskDTOs.stream()
                .collect(Collectors.groupingBy(TaskDetailDTO::getStatus));

        List<TaskDetailDTO> todoTasks = tasksByStatus.getOrDefault(TaskStatus.TODO, Collections.emptyList());
        List<TaskDetailDTO> inProgressTasks = tasksByStatus.getOrDefault(TaskStatus.IN_PROGRESS, Collections.emptyList());
        List<TaskDetailDTO> blockedTasks = tasksByStatus.getOrDefault(TaskStatus.BLOCKED, Collections.emptyList());
        List<TaskDetailDTO> doneTasks = tasksByStatus.getOrDefault(TaskStatus.DONE, Collections.emptyList());

        // 5. 统计信息（基于已转换的DTO，避免重复计算）
        long overdueCount = allTaskDTOs.stream()
                .filter(TaskDetailDTO::getIsOverdue)
                .count();

        TaskBoardDTO.TaskStatistics statistics = TaskBoardDTO.TaskStatistics.builder()
                .todoCount((long) todoTasks.size())
                .inProgressCount((long) inProgressTasks.size())
                .blockedCount((long) blockedTasks.size())
                .doneCount((long) doneTasks.size())
                .totalCount((long) allTaskDTOs.size())
                .overdueCount(overdueCount)
                .build();

        return TaskBoardDTO.builder()
                .todoTasks(todoTasks)
                .inProgressTasks(inProgressTasks)
                .blockedTasks(blockedTasks)
                .doneTasks(doneTasks)
                .statistics(statistics)
                .build();
    }

    @Override
    public Page<TaskDetailDTO> getProjectTasks(Long projectId, Pageable pageable) {
        // 只查询未删除的任务
        Page<Tasks> tasks = taskRepository.findByProjectIdAndIsDeleted(projectId, false, pageable);
        // 使用优化后的批量转换，避免N+1查询
        List<TaskDetailDTO> dtoList = convertListToDetailDTO(tasks.getContent());
        return new PageImpl<>(dtoList, pageable, tasks.getTotalElements());
    }

    @Override
    public Page<TaskDetailDTO> getTasksByStatus(Long projectId, TaskStatus status, Pageable pageable) {
        // 只查询未删除的任务
        Page<Tasks> tasks = taskRepository.findByProjectIdAndStatusAndIsDeleted(projectId, status, false, pageable);
        // 使用优化后的批量转换，避免N+1查询
        List<TaskDetailDTO> dtoList = convertListToDetailDTO(tasks.getContent());
        return new PageImpl<>(dtoList, pageable, tasks.getTotalElements());
    }

    @Override
    public Page<TaskDetailDTO> getTasksByPriority(Long projectId, TaskPriority priority, Pageable pageable) {
        // 只查询未删除的任务
        Page<Tasks> tasks = taskRepository.findByProjectIdAndPriorityAndIsDeleted(projectId, priority, false, pageable);
        // 使用优化后的批量转换，避免N+1查询
        List<TaskDetailDTO> dtoList = convertListToDetailDTO(tasks.getContent());
        return new PageImpl<>(dtoList, pageable, tasks.getTotalElements());
    }

    @Override
    public Page<TaskDetailDTO> getMyAssignedTasks(Long userId, Pageable pageable) {
        // findByAssigneeId 已经在 Repository 层过滤了已删除的任务
        Page<Tasks> tasks = taskRepository.findByAssigneeId(String.valueOf(userId), pageable);
        // 使用优化后的批量转换，避免N+1查询
        List<TaskDetailDTO> dtoList = convertListToDetailDTO(tasks.getContent());
        return new PageImpl<>(dtoList, pageable, tasks.getTotalElements());
    }

    @Override
    public Page<TaskDetailDTO> getMyCreatedTasks(Long userId, Pageable pageable) {
        // 只查询未删除的任务
        Page<Tasks> tasks = taskRepository.findByCreatedByAndIsDeleted(userId, false, pageable);
        // 使用优化后的批量转换，避免N+1查询
        List<TaskDetailDTO> dtoList = convertListToDetailDTO(tasks.getContent());
        return new PageImpl<>(dtoList, pageable, tasks.getTotalElements());
    }

    @Override
    public Page<TaskDetailDTO> searchTasks(Long projectId, String keyword, Pageable pageable) {
        Page<Tasks> tasks = taskRepository.searchByKeyword(projectId, keyword, pageable);
        // 使用优化后的批量转换，避免N+1查询
        List<TaskDetailDTO> dtoList = convertListToDetailDTO(tasks.getContent());
        return new PageImpl<>(dtoList, pageable, tasks.getTotalElements());
    }

    @Override
    public Page<TaskDetailDTO> getUpcomingTasks(Long projectId, int days, Pageable pageable) {
        LocalDate targetDate = LocalDate.now().plusDays(days);
        Page<Tasks> tasks = taskRepository.findUpcomingTasks(projectId, targetDate, pageable);
        // 使用优化后的批量转换，避免N+1查询
        List<TaskDetailDTO> dtoList = convertListToDetailDTO(tasks.getContent());
        return new PageImpl<>(dtoList, pageable, tasks.getTotalElements());
    }

    @Override
    public Page<TaskDetailDTO> getOverdueTasks(Long projectId, Pageable pageable) {
        Page<Tasks> tasks = taskRepository.findOverdueTasks(projectId, LocalDate.now(), pageable);
        // 使用优化后的批量转换，避免N+1查询
        List<TaskDetailDTO> dtoList = convertListToDetailDTO(tasks.getContent());
        return new PageImpl<>(dtoList, pageable, tasks.getTotalElements());
    }

    @Override
    public Page<TaskDetailDTO> getMyUpcomingTasks(Long userId, int days, Pageable pageable) {
        LocalDate targetDate = LocalDate.now().plusDays(days);
        Page<Tasks> tasks = taskRepository.findMyUpcomingTasks(String.valueOf(userId), targetDate, pageable);
        // 使用优化后的批量转换，避免N+1查询
        List<TaskDetailDTO> dtoList = convertListToDetailDTO(tasks.getContent());
        return new PageImpl<>(dtoList, pageable, tasks.getTotalElements());
    }

    @Override
    public Page<TaskDetailDTO> getMyOverdueTasks(Long userId, Pageable pageable) {
        Page<Tasks> tasks = taskRepository.findMyOverdueTasks(String.valueOf(userId), LocalDate.now(), pageable);
        // 使用优化后的批量转换，避免N+1查询
        List<TaskDetailDTO> dtoList = convertListToDetailDTO(tasks.getContent());
        return new PageImpl<>(dtoList, pageable, tasks.getTotalElements());
    }

    // ==================== 统计相关 ====================

    @Override
    public long countProjectTasks(Long projectId) {
        return taskRepository.countByProjectId(projectId);
    }

    @Override
    public long countTasksByStatus(Long projectId, TaskStatus status) {
        return taskRepository.countByProjectIdAndStatus(projectId, status);
    }

    @Override
    public long countOverdueTasks(Long projectId) {
        Page<Tasks> overdueTasks = taskRepository.findOverdueTasks(
                projectId, LocalDate.now(), PageRequest.of(0, 1));
        return overdueTasks.getTotalElements();
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 将任务实体转换为详细DTO
     */
    private TaskDetailDTO convertToDetailDTO(Tasks task) {
        // 查询项目信息
        String projectName = projectRepository.findById(task.getProjectId())
                .map(Project::getName)
                .orElse("未知项目");

        // 解析执行者ID列表
        List<Long> assigneeIds = convertJsonToList(task.getAssigneeId());

        // 批量查询执行者信息
        List<TaskDetailDTO.TaskAssigneeDTO> assignees = new ArrayList<>();
        if (!assigneeIds.isEmpty()) {
            try {
                R<List<UserDTO>> response = authServiceClient.getUsersByIds(assigneeIds);
                if (R.isSuccess(response) && response.getData() != null) {
                    // 将List转换为Map
                    Map<Long, UserDTO> userMap = response.getData().stream()
                            .collect(Collectors.toMap(UserDTO::getId, user -> user));
                    assignees = assigneeIds.stream()
                            .map(userMap::get)
                            .filter(Objects::nonNull)
                            .map(user -> TaskDetailDTO.TaskAssigneeDTO.builder()
                                    .userId(String.valueOf(user.getId()))
                                    .userName(user.getName())
                                    .email(user.getEmail())
                                    .avatarUrl(user.getAvatarUrl())
                                    .build())
                            .collect(Collectors.toList());
                }
            } catch (Exception e) {
                log.error("查询执行者信息失败", e);
            }
        }

        // 查询创建人信息（使用缓存服务）
        String creatorName = "未知用户";
        try {
            R<UserDTO> response = userCacheService.getUserById(task.getCreatedBy());
            if (R.isSuccess(response) && response.getData() != null) {
                creatorName = response.getData().getName();
            }
        } catch (Exception e) {
            log.error("查询创建人信息失败: userId={}", task.getCreatedBy(), e);
        }

        // 判断是否逾期
        boolean isOverdue = task.getDueDate() != null 
                && task.getDueDate().isBefore(LocalDate.now())
                && task.getStatus() != TaskStatus.DONE;

        return TaskDetailDTO.builder()
                .id(String.valueOf(task.getId()))
                .projectId(String.valueOf(task.getProjectId()))
                .projectName(projectName)
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .statusName(task.getStatus().getStatusName())
                .priority(task.getPriority())
                .priorityName(task.getPriority().getPriorityName())
                .assignees(assignees)
                .dueDate(task.getDueDate())
                .worktime(task.getWorktime())
                .isOverdue(isOverdue)
                .createdBy(String.valueOf(task.getCreatedBy()))
                .creatorName(creatorName)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    /**
     * 批量转换任务列表为DTO（优化版本 - 解决N+1查询问题）
     */
    private List<TaskDetailDTO> convertListToDetailDTO(List<Tasks> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 收集所有需要查询的ID
        Set<Long> projectIds = new HashSet<>();
        Set<Long> creatorIds = new HashSet<>();
        Set<Long> assigneeIds = new HashSet<>();

        for (Tasks task : tasks) {
            projectIds.add(task.getProjectId());
            creatorIds.add(task.getCreatedBy());

            // 解析执行者ID
            List<Long> taskAssigneeIds = convertJsonToList(task.getAssigneeId());
            assigneeIds.addAll(taskAssigneeIds);
        }

        // 2. 批量查询项目信息
        Map<Long, String> projectNameMap = new HashMap<>();
        if (!projectIds.isEmpty()) {
            List<Project> projects = projectRepository.findAllById(projectIds);
            projectNameMap = projects.stream()
                    .collect(Collectors.toMap(Project::getId, Project::getName));
        }

        // 3. 批量查询所有用户信息（创建人 + 执行者）使用缓存服务
        Set<Long> allUserIds = new HashSet<>();
        allUserIds.addAll(creatorIds);
        allUserIds.addAll(assigneeIds);

        Map<Long, UserDTO> userMap = new HashMap<>();
        if (!allUserIds.isEmpty()) {
            try {
                R<List<UserDTO>> response = userCacheService.getUsersByIds(new ArrayList<>(allUserIds));
                if (R.isSuccess(response) && response.getData() != null) {
                    userMap = response.getData().stream()
                            .collect(Collectors.toMap(UserDTO::getId, user -> user));
                }
            } catch (Exception e) {
                log.error("批量查询用户信息失败", e);
            }
        }

        // 4. 转换为DTO（使用预加载的数据）
        final Map<Long, String> finalProjectNameMap = projectNameMap;
        final Map<Long, UserDTO> finalUserMap = userMap;

        return tasks.stream()
                .map(task -> convertToDetailDTOWithCache(task, finalProjectNameMap, finalUserMap))
                .collect(Collectors.toList());
    }

    /**
     * 使用缓存的Map转换单个任务（避免重复查询）
     */
    private TaskDetailDTO convertToDetailDTOWithCache(
            Tasks task,
            Map<Long, String> projectNameMap,
            Map<Long, UserDTO> userMap) {

        // 从缓存Map中获取项目名称
        String projectName = projectNameMap.getOrDefault(task.getProjectId(), "未知项目");

        // 解析执行者ID列表
        List<Long> assigneeIdList = convertJsonToList(task.getAssigneeId());

        // 从缓存Map中获取执行者信息
        List<TaskDetailDTO.TaskAssigneeDTO> assignees = assigneeIdList.stream()
                .map(userMap::get)
                .filter(Objects::nonNull)
                .map(user -> TaskDetailDTO.TaskAssigneeDTO.builder()
                        .userId(String.valueOf(user.getId()))
                        .userName(user.getName())
                        .email(user.getEmail())
                        .avatarUrl(user.getAvatarUrl())
                        .build())
                .collect(Collectors.toList());

        // 从缓存Map中获取创建人信息
        UserDTO creator = userMap.get(task.getCreatedBy());
        String creatorName = creator != null ? creator.getName() : "未知用户";

        // 判断是否逾期
        boolean isOverdue = task.getDueDate() != null
                && task.getDueDate().isBefore(LocalDate.now())
                && task.getStatus() != TaskStatus.DONE;

        return TaskDetailDTO.builder()
                .id(String.valueOf(task.getId()))
                .projectId(String.valueOf(task.getProjectId()))
                .projectName(projectName)
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .statusName(task.getStatus().getStatusName())
                .priority(task.getPriority())
                .priorityName(task.getPriority().getPriorityName())
                .assignees(assignees)
                .dueDate(task.getDueDate())
                .worktime(task.getWorktime())
                .isOverdue(isOverdue)
                .createdBy(String.valueOf(task.getCreatedBy()))
                .creatorName(creatorName)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    /**
     * 将ID列表转换为JSON字符串
     */
    private String convertListToJson(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(ids);
        } catch (JsonProcessingException e) {
            log.error("转换ID列表为JSON失败", e);
            return "[]";
        }
    }

    /**
     * 将JSON字符串转换为ID列表
     */
    private List<Long> convertJsonToList(String json) {
        if (json == null || json.trim().isEmpty() || "[]".equals(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Long.class));
        } catch (JsonProcessingException e) {
            log.error("解析JSON为ID列表失败: json={}", json, e);
            return Collections.emptyList();
        }
    }
}
