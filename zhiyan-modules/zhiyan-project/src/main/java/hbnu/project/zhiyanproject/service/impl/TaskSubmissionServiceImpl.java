package hbnu.project.zhiyanproject.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonoss.service.MinioService;
import hbnu.project.zhiyanproject.model.dto.TaskSubmissionDTO;
import hbnu.project.zhiyanproject.model.dto.UserDTO;
import hbnu.project.zhiyanproject.model.entity.TaskSubmission;
import hbnu.project.zhiyanproject.model.entity.TaskUser;
import hbnu.project.zhiyanproject.model.entity.Tasks;
import hbnu.project.zhiyanproject.model.enums.ReviewStatus;
import hbnu.project.zhiyanproject.model.enums.TaskStatus;
import hbnu.project.zhiyanproject.model.form.ReviewSubmissionRequest;
import hbnu.project.zhiyanproject.model.form.SubmitTaskRequest;
import hbnu.project.zhiyanproject.model.entity.Project;
import hbnu.project.zhiyanproject.repository.ProjectMemberRepository;
import hbnu.project.zhiyanproject.repository.ProjectRepository;
import hbnu.project.zhiyanproject.repository.TaskRepository;
import hbnu.project.zhiyanproject.repository.TaskSubmissionRepository;
import hbnu.project.zhiyanproject.repository.TaskUserRepository;
import hbnu.project.zhiyanproject.service.TaskSubmissionService;
import hbnu.project.zhiyanproject.service.UserCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务提交服务实现类
 *
 * @author Tokito
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskSubmissionServiceImpl implements TaskSubmissionService {

    private final TaskSubmissionRepository submissionRepository;
    private final TaskRepository taskRepository;
    private final TaskUserRepository taskUserRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final UserCacheService userCacheService;
    private final ObjectMapper objectMapper;

    @Autowired
    private MinioService minioService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskSubmissionDTO submitTask(Long taskId, SubmitTaskRequest request, Long userId) {
        log.info("用户[{}]提交任务[{}]", userId, taskId);

        // 1. 验证任务是否存在且未删除
        Tasks task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在"));

        if (task.getIsDeleted()) {
            throw new IllegalArgumentException("任务已删除，无法提交");
        }

        // 2. 验证用户是否为任务执行者
        boolean isAssignee = taskUserRepository.isUserActiveExecutor(taskId, userId);
        if (!isAssignee) {
            throw new IllegalArgumentException("只有任务执行者才能提交任务");
        }

        // 3. 如果是最终提交，检查是否已有其他执行者的最终提交被批准
        if (Boolean.TRUE.equals(request.getIsFinal())) {
            boolean hasApprovedSubmission = checkIfTaskHasApprovedSubmission(taskId);
            if (hasApprovedSubmission) {
                throw new IllegalArgumentException("任务已有执行者提交通过审核，无法再次提交最终版本");
            }

            // 检查任务状态，避免重复提交
            if (task.getStatus() == TaskStatus.DONE) {
                throw new IllegalArgumentException("任务已完成，无法重复提交");
            }
        }

        // 4. 获取下一个版本号
        Integer nextVersion = submissionRepository.getNextVersionNumber(taskId);

        // 5. 转换附件URL列表为JSON
        String attachmentUrlsJson = null;
        if (request.getAttachmentUrls() != null && !request.getAttachmentUrls().isEmpty()) {
            try {
                attachmentUrlsJson = objectMapper.writeValueAsString(request.getAttachmentUrls());
            } catch (JsonProcessingException e) {
                log.error("附件URL序列化失败", e);
                throw new IllegalArgumentException("附件URL格式错误");
            }
        }

        // 6. 创建提交记录
        TaskSubmission submission = TaskSubmission.builder()
                .taskId(taskId)
                .projectId(task.getProjectId())
                .submitterId(userId)
                .submissionType(request.getSubmissionType())
                .submissionContent(request.getSubmissionContent())
                .attachmentUrls(attachmentUrlsJson)
                .actualWorktime(request.getActualWorktime())
                .version(nextVersion)
                .isFinal(request.getIsFinal())
                .reviewStatus(ReviewStatus.PENDING)
                .isDeleted(false)
                .build();

        submission = submissionRepository.save(submission);
        log.info("任务提交成功: submissionId={}, version={}, isFinal={}",
                submission.getId(), nextVersion, request.getIsFinal());

        // 7. 如果是最终提交，更新任务状态为待审核（无论是否已有其他执行者提交）
        if (Boolean.TRUE.equals(request.getIsFinal())) {
            task.setStatus(TaskStatus.PENDING_REVIEW);
            taskRepository.save(task);
            log.info("任务状态已更新为待审核: taskId={}", taskId);
        }

        // 8. 转换为DTO返回
        return convertToDTO(submission, task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskSubmissionDTO reviewSubmission(Long submissionId, ReviewSubmissionRequest request, Long reviewerId) {
        log.info("用户[{}]审核提交记录[{}]，结果: {}", reviewerId, submissionId, request.getReviewStatus());

        // 1. 验证提交记录是否存在
        TaskSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("提交记录不存在"));

        if (submission.getIsDeleted()) {
            throw new IllegalArgumentException("提交记录已删除");
        }

        // 2. 验证审核状态
        if (submission.getReviewStatus() != ReviewStatus.PENDING) {
            throw new IllegalArgumentException("该提交已审核，无法重复操作");
        }

        // 3. 验证审核权限（必须是任务创建者）
        Tasks task = taskRepository.findById(submission.getTaskId())
                .orElseThrow(() -> new IllegalArgumentException("关联任务不存在"));

        // 检查是否为任务创建者
        boolean isCreator = task.getCreatedBy().equals(reviewerId);

        if (!isCreator) {
            throw new IllegalArgumentException("只有任务创建者才能审核提交");
        }

        // 4. 验证审核结果
        if (request.getReviewStatus() != ReviewStatus.APPROVED
                && request.getReviewStatus() != ReviewStatus.REJECTED) {
            throw new IllegalArgumentException("审核结果只能是APPROVED或REJECTED");
        }

        // 5. 更新提交记录
        submission.setReviewStatus(request.getReviewStatus());
        submission.setReviewerId(reviewerId);
        submission.setReviewComment(request.getReviewComment());
        submission.setReviewTime(Instant.now());

        submission = submissionRepository.save(submission);
        log.info("提交记录审核完成: submissionId={}, status={}", submissionId, request.getReviewStatus());

        // 6. 如果审核通过且为最终提交，更新任务状态为已完成
        if (request.getReviewStatus() == ReviewStatus.APPROVED && Boolean.TRUE.equals(submission.getIsFinal())) {
            // 检查是否已有其他执行者的提交被批准
            boolean hasApprovedSubmission = checkIfTaskHasApprovedSubmission(task.getId());

            if (!hasApprovedSubmission) {
                task.setStatus(TaskStatus.DONE);
                taskRepository.save(task);
                log.info("✅ 任务已完成: taskId={}, 首个执行者提交通过审核", task.getId());
            } else {
                log.info("✅ 任务已完成: taskId={}, 已有其他执行者提交通过审核", task.getId());
            }
        }

        // 7. 转换为DTO返回
        return convertToDTO(submission, task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskSubmissionDTO revokeSubmission(Long submissionId, Long userId) {
        log.info("用户[{}]撤回提交记录[{}]", userId, submissionId);

        // 1. 验证提交记录是否存在
        TaskSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("提交记录不存在"));

        if (submission.getIsDeleted()) {
            throw new IllegalArgumentException("提交记录已删除");
        }

        // 2. 验证操作权限（只有提交人可以撤回）
        if (!submission.getSubmitterId().equals(userId)) {
            throw new IllegalArgumentException("只有提交人才能撤回提交");
        }

        // 3. 验证审核状态（只能撤回待审核的提交）
        if (submission.getReviewStatus() != ReviewStatus.PENDING) {
            throw new IllegalArgumentException("只能撤回待审核的提交");
        }

        // 4. 更新状态为已撤回
        submission.setReviewStatus(ReviewStatus.REVOKED);
        submission = submissionRepository.save(submission);

        log.info("提交记录撤回成功: submissionId={}", submissionId);

        // 5. 获取任务信息
        Tasks task = taskRepository.findById(submission.getTaskId())
                .orElseThrow(() -> new IllegalArgumentException("关联任务不存在"));

        // 6. 转换为DTO返回
        return convertToDTO(submission, task);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskSubmissionDTO getSubmissionDetail(Long submissionId) {
        TaskSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("提交记录不存在"));

        if (submission.getIsDeleted()) {
            throw new IllegalArgumentException("提交记录已删除");
        }

        Tasks task = taskRepository.findById(submission.getTaskId())
                .orElseThrow(() -> new IllegalArgumentException("关联任务不存在"));

        return convertToDTO(submission, task);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskSubmissionDTO> getTaskSubmissions(Long taskId) {
        List<TaskSubmission> submissions = submissionRepository
                .findByTaskIdAndIsDeletedFalseOrderByVersionDesc(taskId);

        Tasks task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在"));

        return submissions.stream()
                .map(s -> convertToDTO(s, task))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TaskSubmissionDTO getLatestSubmission(Long taskId) {
        TaskSubmission submission = submissionRepository
                .findFirstByTaskIdAndIsDeletedFalseOrderByVersionDesc(taskId)
                .orElseThrow(() -> new IllegalArgumentException("该任务暂无提交记录"));

        Tasks task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在"));

        return convertToDTO(submission, task);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskSubmissionDTO> getPendingSubmissions(Long userId, Pageable pageable) {
        // 返回用户相关的待审核提交：包括用户提交的 + 需要用户审核的（任务创建者是该用户）
        return submissionRepository
                .findPendingSubmissionsForUser(userId, ReviewStatus.PENDING, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskSubmissionDTO> getProjectPendingSubmissions(Long projectId, Pageable pageable) {
        return submissionRepository
                .findByProjectIdAndReviewStatusAndIsDeletedFalseOrderBySubmissionTimeDesc(
                        projectId, ReviewStatus.PENDING, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskSubmissionDTO> getUserSubmissions(Long userId, Pageable pageable) {
        return submissionRepository
                .findBySubmitterIdAndIsDeletedFalseOrderBySubmissionTimeDesc(userId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public long countPendingSubmissions(Long userId) {
        // 统计用户相关的待审核提交：包括用户提交的 + 需要用户审核的（任务创建者是该用户）
        return submissionRepository.countPendingSubmissionsForUser(userId, ReviewStatus.PENDING);
    }

    @Override
    public long countProjectPendingSubmissions(Long projectId) {
        return submissionRepository.countByProjectIdAndReviewStatusAndIsDeletedFalse(
                projectId, ReviewStatus.PENDING);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskSubmissionDTO> getMyPendingSubmissions(Long userId, Pageable pageable) {
        // 查询我提交的待审核任务（我提交的，等待别人审核）
        return submissionRepository
                .findMyPendingSubmissions(userId, ReviewStatus.PENDING, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskSubmissionDTO> getPendingSubmissionsForReview(Long userId, Pageable pageable) {
        // 查询待我审核的提交（别人提交的，需要我审核的，因为我是任务创建者）
        return submissionRepository
                .findPendingSubmissionsForReviewer(userId, ReviewStatus.PENDING, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public long countMyPendingSubmissions(Long userId) {
        // 统计我提交的待审核任务数量
        return submissionRepository.countMyPendingSubmissions(userId, ReviewStatus.PENDING);
    }

    @Override
    public long countPendingSubmissionsForReview(Long userId) {
        // 统计待我审核的提交数量（任务创建者是我）
        return submissionRepository.countPendingSubmissionsForReviewer(userId, ReviewStatus.PENDING);
    }

    @Override
    public Page<TaskSubmissionDTO> getMyCreatedTasksPendingSubmissions(Long userId, Pageable pageable) {
        log.debug("查询用户[{}]创建的任务中的待审核提交", userId);
        return submissionRepository
                .findPendingSubmissionsForMyCreatedTasks(userId, ReviewStatus.PENDING, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public long countMyCreatedTasksPendingSubmissions(Long userId) {
        log.debug("统计用户[{}]创建的任务中的待审核提交数量", userId);
        return submissionRepository.countPendingSubmissionsForMyCreatedTasks(userId, ReviewStatus.PENDING);
    }

    /**
     * 转换实体为DTO（带任务信息）
     */
    private TaskSubmissionDTO convertToDTO(TaskSubmission submission, Tasks task) {
        // 解析附件URL
        List<String> attachmentUrls = new ArrayList<>();
        if (submission.getAttachmentUrls() != null) {
            try {
                attachmentUrls = objectMapper.readValue(
                        submission.getAttachmentUrls(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            } catch (JsonProcessingException e) {
                log.warn("附件URL反序列化失败", e);
            }
        }

        // 获取用户信息
        UserDTO submitter = null;
        UserDTO reviewer = null;

        try {
            R<UserDTO> submitterResult = userCacheService.getUserById(submission.getSubmitterId());
            if (R.isSuccess(submitterResult)) {
                submitter = submitterResult.getData();
            }

            if (submission.getReviewerId() != null) {
                R<UserDTO> reviewerResult = userCacheService.getUserById(submission.getReviewerId());
                if (R.isSuccess(reviewerResult)) {
                    reviewer = reviewerResult.getData();
                }
            }
        } catch (Exception e) {
            log.warn("获取用户信息失败", e);
        }

        // 安全地获取项目名称，通过 projectId 直接查询，避免懒加载问题
        String projectName = null;
        try {
            if (submission.getProjectId() != null) {
                Optional<Project> projectOpt = projectRepository.findById(submission.getProjectId());
                if (projectOpt.isPresent()) {
                    projectName = projectOpt.get().getName();
                }
            }
        } catch (Exception e) {
            log.warn("获取项目名称失败，projectId: {}", submission.getProjectId(), e);
        }

        return TaskSubmissionDTO.builder()
                .id(String.valueOf(submission.getId()))
                .taskId(String.valueOf(submission.getTaskId()))
                .taskTitle(task != null ? task.getTitle() : null)
                .taskCreatorId(task != null && task.getCreatedBy() != null ? String.valueOf(task.getCreatedBy()) : null)
                .projectId(String.valueOf(submission.getProjectId()))
                .projectName(projectName)
                .submitterId(String.valueOf(submission.getSubmitterId()))
                .submitter(submitter)
                .submissionType(submission.getSubmissionType())
                .submissionContent(submission.getSubmissionContent())
                .attachmentUrls(attachmentUrls)
                .submissionTime(instantToLocalDateTime(submission.getSubmissionTime()))
                .reviewStatus(submission.getReviewStatus())
                .reviewerId(submission.getReviewerId() != null ? String.valueOf(submission.getReviewerId()) : null)
                .reviewer(reviewer)
                .reviewComment(submission.getReviewComment())
                .reviewTime(instantToLocalDateTime(submission.getReviewTime()))
                .actualWorktime(submission.getActualWorktime())
                .version(submission.getVersion())
                .isFinal(submission.getIsFinal())
                .createdAt(instantToLocalDateTime(submission.getCreatedAt()))
                .updatedAt(instantToLocalDateTime(submission.getUpdatedAt()))
                .build();
    }

    /**
     * 转换实体为DTO（不带任务信息）
     */
    private TaskSubmissionDTO convertToDTO(TaskSubmission submission) {
        Tasks task = taskRepository.findById(submission.getTaskId())
                .orElse(null);
        return convertToDTO(submission, task);
    }

    /**
     * Instant转LocalDateTime
     */
    private LocalDateTime instantToLocalDateTime(Instant instant) {
        return instant != null ? LocalDateTime.ofInstant(instant, ZoneId.systemDefault()) : null;
    }

    /**
     * 检查任务是否已有通过审核的最终提交
     * 用于避免重复更新任务状态
     */
    private boolean checkIfTaskHasApprovedSubmission(Long taskId) {
        // 查询任务的所有最终提交记录
        List<TaskSubmission> finalSubmissions = submissionRepository.findByTaskIdAndIsDeletedFalseOrderByVersionDesc(taskId)
                .stream()
                .filter(submission -> Boolean.TRUE.equals(submission.getIsFinal()))
                .toList();

        // 检查是否有已批准的最终提交
        return finalSubmissions.stream()
                .anyMatch(submission -> submission.getReviewStatus() == ReviewStatus.APPROVED);
    }


    /**
     * 检查任务是否可以标记为已完成
     * 当有执行者提交最终版本并通过审核时，任务即可完成
     */
    private boolean canMarkTaskAsDone(Long taskId) {
        // 查询任务的所有执行者
        List<TaskUser> executors = taskUserRepository.findActiveExecutorsByTaskId(taskId);

        if (executors.isEmpty()) {
            return false;
        }

        // 检查是否有至少一个执行者的最终提交被批准
        List<TaskSubmission> approvedSubmissions = submissionRepository.findApprovedFinalSubmissionsByTaskId(taskId);

        return !approvedSubmissions.isEmpty();
    }


    /**
     * 获取任务的提交统计信息
     */
    public Map<String, Object> getTaskSubmissionStats(Long taskId) {
        Map<String, Object> stats = new HashMap<>();

        // 获取所有执行者
        List<TaskUser> executors = taskUserRepository.findActiveExecutorsByTaskId(taskId);
        stats.put("totalExecutors", executors.size());

        // 获取所有最终提交
        List<TaskSubmission> finalSubmissions = submissionRepository.findFinalSubmissionsByTaskId(taskId);
        stats.put("totalFinalSubmissions", finalSubmissions.size());

        // 获取已批准的最终提交
        List<TaskSubmission> approvedSubmissions = submissionRepository.findApprovedFinalSubmissionsByTaskId(taskId);
        stats.put("approvedFinalSubmissions", approvedSubmissions.size());

        // 按执行者统计提交情况
        Map<Long, List<TaskSubmission>> submissionsByExecutor = finalSubmissions.stream()
                .collect(Collectors.groupingBy(TaskSubmission::getSubmitterId));

        stats.put("submissionsByExecutor", submissionsByExecutor);
        stats.put("canBeMarkedAsDone", !approvedSubmissions.isEmpty());

        return stats;
    }
}