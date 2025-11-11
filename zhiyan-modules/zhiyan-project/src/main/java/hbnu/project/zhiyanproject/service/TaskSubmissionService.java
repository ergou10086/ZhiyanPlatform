package hbnu.project.zhiyanproject.service;

import hbnu.project.zhiyanproject.model.dto.TaskSubmissionDTO;
import hbnu.project.zhiyanproject.model.form.ReviewSubmissionRequest;
import hbnu.project.zhiyanproject.model.form.SubmitTaskRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 任务提交服务接口
 *
 * @author Tokito
 */
public interface TaskSubmissionService {

    /**
     * 提交任务
     *
     * @param taskId  任务ID
     * @param request 提交请求
     * @param userId  提交人ID
     * @return 提交记录DTO
     */
    TaskSubmissionDTO submitTask(Long taskId, SubmitTaskRequest request, Long userId);

    /**
     * 审核任务提交
     *
     * @param submissionId 提交记录ID
     * @param request      审核请求
     * @param reviewerId   审核人ID
     * @return 提交记录DTO
     */
    TaskSubmissionDTO reviewSubmission(Long submissionId, ReviewSubmissionRequest request, Long reviewerId);

    /**
     * 撤回提交（仅提交人可撤回）
     *
     * @param submissionId 提交记录ID
     * @param userId       操作人ID
     * @return 提交记录DTO
     */
    TaskSubmissionDTO revokeSubmission(Long submissionId, Long userId);

    /**
     * 获取提交记录详情
     *
     * @param submissionId 提交记录ID
     * @return 提交记录DTO
     */
    TaskSubmissionDTO getSubmissionDetail(Long submissionId);

    /**
     * 获取任务的所有提交记录
     *
     * @param taskId 任务ID
     * @return 提交记录列表
     */
    List<TaskSubmissionDTO> getTaskSubmissions(Long taskId);

    /**
     * 获取任务的最新提交
     *
     * @param taskId 任务ID
     * @return 提交记录DTO
     */
    TaskSubmissionDTO getLatestSubmission(Long taskId);

    /**
     * 获取待审核的提交记录（分页）
     * 返回用户相关的待审核提交：包括用户提交的 + 需要用户审核的（任务创建者是该用户）
     *
     * @param userId   用户ID
     * @param pageable 分页参数
     * @return 提交记录分页
     */
    Page<TaskSubmissionDTO> getPendingSubmissions(Long userId, Pageable pageable);

    /**
     * 获取项目的待审核提交记录（分页）
     *
     * @param projectId 项目ID
     * @param pageable  分页参数
     * @return 提交记录分页
     */
    Page<TaskSubmissionDTO> getProjectPendingSubmissions(Long projectId, Pageable pageable);

    /**
     * 获取用户的提交历史（分页）
     *
     * @param userId   用户ID
     * @param pageable 分页参数
     * @return 提交记录分页
     */
    Page<TaskSubmissionDTO> getUserSubmissions(Long userId, Pageable pageable);

    /**
     * 统计待审核的提交数量
     * 统计用户相关的待审核提交：包括用户提交的 + 需要用户审核的（任务创建者是该用户）
     *
     * @param userId 用户ID
     * @return 数量
     */
    long countPendingSubmissions(Long userId);

    /**
     * 统计项目的待审核提交数量
     *
     * @param projectId 项目ID
     * @return 数量
     */
    long countProjectPendingSubmissions(Long projectId);

    /**
<<<<<<< HEAD
     * 获取我创建的任务中的待审核提交（分页）
=======
     * 获取我提交的待审核任务（我提交的，等待别人审核）
>>>>>>> 5861726dbe32635b99f7a52c69684df8f9edc1d4
     *
     * @param userId   用户ID
     * @param pageable 分页参数
     * @return 提交记录分页
     */
<<<<<<< HEAD
    Page<TaskSubmissionDTO> getMyCreatedTasksPendingSubmissions(Long userId, Pageable pageable);

    /**
     * 统计我创建的任务中的待审核提交数量
=======
    Page<TaskSubmissionDTO> getMyPendingSubmissions(Long userId, Pageable pageable);

    /**
     * 获取待我审核的提交（别人提交的，需要我审核的，因为我是任务创建者）
     *
     * @param userId   用户ID（任务创建者ID）
     * @param pageable 分页参数
     * @return 提交记录分页
     */
    Page<TaskSubmissionDTO> getPendingSubmissionsForReview(Long userId, Pageable pageable);

    /**
     * 统计我提交的待审核任务数量
>>>>>>> 5861726dbe32635b99f7a52c69684df8f9edc1d4
     *
     * @param userId 用户ID
     * @return 数量
     */
<<<<<<< HEAD
    long countMyCreatedTasksPendingSubmissions(Long userId);
=======
    long countMyPendingSubmissions(Long userId);

    /**
     * 统计待我审核的提交数量（任务创建者是我）
     *
     * @param userId 用户ID（任务创建者ID）
     * @return 数量
     */
    long countPendingSubmissionsForReview(Long userId);
>>>>>>> 5861726dbe32635b99f7a52c69684df8f9edc1d4
}