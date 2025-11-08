package hbnu.project.zhiyanproject.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonoss.service.MinioService;
import hbnu.project.zhiyanproject.model.dto.TaskSubmissionDTO;
import hbnu.project.zhiyanproject.model.dto.UserDTO;
import hbnu.project.zhiyanproject.model.entity.TaskSubmission;
import hbnu.project.zhiyanproject.model.entity.Tasks;
import hbnu.project.zhiyanproject.model.enums.ProjectMemberRole;
import hbnu.project.zhiyanproject.model.enums.ReviewStatus;
import hbnu.project.zhiyanproject.model.enums.TaskStatus;
import hbnu.project.zhiyanproject.model.form.ReviewSubmissionRequest;
import hbnu.project.zhiyanproject.model.form.SubmitTaskRequest;
import hbnu.project.zhiyanproject.repository.ProjectMemberRepository;
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
import java.util.ArrayList;
import java.util.List;
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

        // 3. 如果是最终提交，检查任务状态
        if (Boolean.TRUE.equals(request.getIsFinal())) {
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
        log.info("任务提交成功: submissionId={}, version={}", submission.getId(), nextVersion);

        // 7. 如果是最终提交，更新任务状态为已提交（可选，取决于是否扩展了TaskStatus枚举）
        // 如果没有扩展TaskStatus，可以保持IN_PROGRESS状态
        if (Boolean.TRUE.equals(request.getIsFinal())) {
            // task.setStatus(TaskStatus.SUBMITTED); // 如果扩展了枚举则启用
            // taskRepository.save(task);
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

        // 3. 验证审核权限（必须是项目负责人或任务创建者）
        Tasks task = taskRepository.findById(submission.getTaskId())
                .orElseThrow(() -> new IllegalArgumentException("关联任务不存在"));

        // 检查是否为项目负责人
        boolean isOwner = projectMemberRepository.findUserRoleInProject(reviewerId, task.getProjectId())
                .map(role -> role == ProjectMemberRole.OWNER)
                .orElse(false);
        boolean isCreator = task.getCreatedBy().equals(reviewerId);

        if (!isOwner && !isCreator) {
            throw new IllegalArgumentException("只有项目负责人或任务创建者才能审核提交");
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
            task.setStatus(TaskStatus.DONE);
            taskRepository.save(task);
            log.info("任务已完成: taskId={}", task.getId());
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
    public TaskSubmissionDTO getLatestSubmission(Long taskId) {
        TaskSubmission submission = submissionRepository
                .findFirstByTaskIdAndIsDeletedFalseOrderByVersionDesc(taskId)
                .orElseThrow(() -> new IllegalArgumentException("该任务暂无提交记录"));

        Tasks task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在"));

        return convertToDTO(submission, task);
    }

    @Override
    public Page<TaskSubmissionDTO> getPendingSubmissions(Pageable pageable) {
        return submissionRepository
                .findByReviewStatusAndIsDeletedFalseOrderBySubmissionTimeDesc(ReviewStatus.PENDING, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<TaskSubmissionDTO> getProjectPendingSubmissions(Long projectId, Pageable pageable) {
        return submissionRepository
                .findByProjectIdAndReviewStatusAndIsDeletedFalseOrderBySubmissionTimeDesc(
                        projectId, ReviewStatus.PENDING, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<TaskSubmissionDTO> getUserSubmissions(Long userId, Pageable pageable) {
        return submissionRepository
                .findBySubmitterIdAndIsDeletedFalseOrderBySubmissionTimeDesc(userId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public long countPendingSubmissions() {
        return submissionRepository.countByReviewStatusAndIsDeletedFalse(ReviewStatus.PENDING);
    }

    @Override
    public long countProjectPendingSubmissions(Long projectId) {
        return submissionRepository.countByProjectIdAndReviewStatusAndIsDeletedFalse(
                projectId, ReviewStatus.PENDING);
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

        return TaskSubmissionDTO.builder()
                .id(String.valueOf(submission.getId()))
                .taskId(String.valueOf(submission.getTaskId()))
                .taskTitle(task.getTitle())
                .projectId(String.valueOf(submission.getProjectId()))
                .projectName(task.getProject() != null ? task.getProject().getName() : null)
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
}