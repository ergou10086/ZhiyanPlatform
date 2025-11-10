package hbnu.project.zhiyanproject.repository;

import hbnu.project.zhiyanproject.model.entity.TaskSubmission;
import hbnu.project.zhiyanproject.model.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 任务提交记录Repository
 *
 * @author Tokito
 */
@Repository
public interface TaskSubmissionRepository extends JpaRepository<TaskSubmission, Long> {

    /**
     * 查询任务的所有提交记录（按版本倒序）
     */
    List<TaskSubmission> findByTaskIdAndIsDeletedFalseOrderByVersionDesc(Long taskId);

    /**
     * 查询任务的最新提交
     */
    Optional<TaskSubmission> findFirstByTaskIdAndIsDeletedFalseOrderByVersionDesc(Long taskId);

    /**
     * 查询任务的最终提交
     */
    Optional<TaskSubmission> findByTaskIdAndIsFinalTrueAndIsDeletedFalse(Long taskId);

    /**
     * 查询待审核的提交记录（分页）
     */
    Page<TaskSubmission> findByReviewStatusAndIsDeletedFalseOrderBySubmissionTimeDesc(
            ReviewStatus reviewStatus, Pageable pageable);

    /**
     * 查询项目的待审核提交记录（分页）
     */
    Page<TaskSubmission> findByProjectIdAndReviewStatusAndIsDeletedFalseOrderBySubmissionTimeDesc(
            Long projectId, ReviewStatus reviewStatus, Pageable pageable);

    /**
     * 查询用户的提交历史（分页）
     */
    Page<TaskSubmission> findBySubmitterIdAndIsDeletedFalseOrderBySubmissionTimeDesc(
            Long submitterId, Pageable pageable);

    /**
     * 查询用户在特定项目的提交历史（分页）
     */
    Page<TaskSubmission> findBySubmitterIdAndProjectIdAndIsDeletedFalseOrderBySubmissionTimeDesc(
            Long submitterId, Long projectId, Pageable pageable);

    /**
     * 统计任务的提交次数
     */
    long countByTaskIdAndIsDeletedFalse(Long taskId);

    /**
     * 统计待审核的提交数量
     */
    long countByReviewStatusAndIsDeletedFalse(ReviewStatus reviewStatus);

    /**
     * 统计项目的待审核提交数量
     */
    long countByProjectIdAndReviewStatusAndIsDeletedFalse(Long projectId, ReviewStatus reviewStatus);

    /**
     * 查询任务的下一个版本号
     */
    @Query("SELECT COALESCE(MAX(s.version), 0) + 1 FROM TaskSubmission s " +
            "WHERE s.taskId = :taskId AND s.isDeleted = false")
    Integer getNextVersionNumber(@Param("taskId") Long taskId);

    /**
     * 查询我创建的任务中的待审核提交（分页）
     * 只查询我创建的任务，且任务所属项目未删除
     */
    @Query("SELECT ts FROM TaskSubmission ts " +
           "WHERE ts.reviewStatus = :reviewStatus " +
           "AND ts.isDeleted = false " +
           "AND ts.taskId IN (" +
           "  SELECT t.id FROM Tasks t " +
           "  WHERE t.createdBy = :creatorId " +
           "  AND t.isDeleted = false " +
           "  AND t.projectId IN (SELECT p.id FROM Project p WHERE p.isDeleted = false)" +
           ") " +
           "ORDER BY ts.submissionTime DESC")
    Page<TaskSubmission> findPendingSubmissionsForMyCreatedTasks(
            @Param("creatorId") Long creatorId,
            @Param("reviewStatus") ReviewStatus reviewStatus,
            Pageable pageable);

    /**
     * 统计我创建的任务中的待审核提交数量
     */
    @Query("SELECT COUNT(ts) FROM TaskSubmission ts " +
           "WHERE ts.reviewStatus = :reviewStatus " +
           "AND ts.isDeleted = false " +
           "AND ts.taskId IN (" +
           "  SELECT t.id FROM Tasks t " +
           "  WHERE t.createdBy = :creatorId " +
           "  AND t.isDeleted = false " +
           "  AND t.projectId IN (SELECT p.id FROM Project p WHERE p.isDeleted = false)" +
           ")")
    long countPendingSubmissionsForMyCreatedTasks(
            @Param("creatorId") Long creatorId,
            @Param("reviewStatus") ReviewStatus reviewStatus);
}