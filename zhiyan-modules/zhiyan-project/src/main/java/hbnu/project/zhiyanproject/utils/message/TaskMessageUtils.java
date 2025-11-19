package hbnu.project.zhiyanproject.utils.message;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyanmessgae.model.dto.SendMessageRequestDTO;
import hbnu.project.zhiyanproject.client.AuthServiceClient;
import hbnu.project.zhiyanproject.client.MessageServiceClient;
import hbnu.project.zhiyanproject.model.dto.UserDTO;
import hbnu.project.zhiyanproject.model.entity.TaskUser;
import hbnu.project.zhiyanproject.model.entity.Tasks;
import hbnu.project.zhiyanproject.model.entity.TaskSubmission;
import hbnu.project.zhiyanproject.model.enums.ReviewStatus;
import hbnu.project.zhiyanproject.model.enums.TaskStatus;
import hbnu.project.zhiyanproject.repository.TaskUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务消息工具类
 * 集中处理任务相关的消息发送逻辑
 *
 * @author ErgouTree
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskMessageUtils {

    private final MessageServiceClient messageServiceClient;

    private final TaskUserRepository taskUserRepository;

    private final AuthServiceClient authServiceClient;

    /**
     * 根据用户ID获取用户名
     *
     * @param userId 用户ID
     * @return 用户名，如果获取失败返回"未知用户"
     */
    private String getUserNameById(Long userId) {
        if (userId == null) {
            return "未知用户";
        }

        try {
            R<UserDTO> result = authServiceClient.getUserById(userId);
            if (R.isSuccess(result) && result.getData() != null && result.getData().getName() != null) {
                return result.getData().getName();
            } else {
                log.warn("获取用户[{}]信息失败: {}", userId, result.getMsg());
                return "未知用户";
            }
        } catch (Exception e) {
            log.error("获取用户[{}]信息异常", userId, e);
            return "未知用户";
        }
    }


    /**
     * 发送任务创建通知
     */
    public void sendTaskCreatedNotification(Tasks task, Long creatorId, List<Long> assigneeIds) {
        try {
            if (assigneeIds == null || assigneeIds.isEmpty()) {
                return;
            }

            // 获取创建者姓名
            String creatorName = getUserNameById(creatorId);

            SendMessageRequestDTO request = SendMessageRequestDTO.builder()
                    .scene("TASK_ASSIGN")
                    .senderId(creatorId)
                    .receiverIds(assigneeIds)
                    .title("新任务分配")
                    .content(String.format("您被「%s」分配到新任务「%s」，请及时处理", creatorName, task.getTitle()))
                    .businessId(task.getId())
                    .businessType("TASK")
                    .extendData(buildTaskExtendData(task))
                    .build();

            R<Void> result = messageServiceClient.sendBatchPersonalMessage(request);
            if (!R.isSuccess(result)) {
                log.warn("发送任务创建通知失败: taskId={}, error={}", task.getId(), result.getMsg());
            }
        } catch (Exception e) {
            log.error("发送任务创建通知异常: taskId={}", task.getId(), e);
        }
    }

    /**
     * 发送任务分配通知
     */
    public void sendTaskAssignedNotification(Tasks task, List<Long> newAssigneeIds, Long operatorId) {
        try{
            if(newAssigneeIds == null || newAssigneeIds.isEmpty()){
                log.warn("发送给空气，666");
                return;
            }

            // 获取操作者姓名
            String operatorName = getUserNameById(operatorId);

            SendMessageRequestDTO request = SendMessageRequestDTO.builder()
                    .scene("TASK_ASSIGN")
                    .senderId(operatorId)
                    .receiverIds(newAssigneeIds)
                    .title("任务分配")
                    .content(String.format("您被「%s」分配到任务「%s」，请及时处理", operatorName, task.getTitle()))
                    .businessId(task.getId())
                    .businessType("TASK")
                    .extendData(buildTaskExtendData(task))
                    .build();

            R<Void> result = messageServiceClient.sendBatchPersonalMessage(request);
            if (!R.isSuccess(result)) {
                log.warn("发送任务分配通知失败: taskId={}, error={}", task.getId(), result.getMsg());
            }
        }catch (Exception e){
            log.error("发送任务分配通知异常: taskId={}", task.getId(), e);
        }
    }

    /**
     * 发送任务状态变更通知
     */
    public void sendTaskStatusChangedNotification(Tasks task, TaskStatus oldStatus, TaskStatus newStatus, Long operatorId) {
        try{
            // 通知任务执行者
            List<Long> assigneeIds = getTaskAssigneeIds(task.getId());
            if (assigneeIds.isEmpty()) {
                return;
            }

            // 获取操作者姓名
            String operatorName = getUserNameById(operatorId);

            SendMessageRequestDTO request = SendMessageRequestDTO.builder()
                    .scene("TASK_STATUS_CHANGED")
                    .senderId(operatorId)
                    .receiverIds(assigneeIds)
                    .title("任务状态更新")
                    .content(String.format("任务「%s」状态已从「%s」变更为「%s」，操作者「%s」",
                            task.getTitle(),
                            oldStatus.getStatusName(),
                            newStatus.getStatusName(),
                            operatorName))
                    .businessId(task.getId())
                    .businessType("TASK")
                    .extendData(buildTaskExtendData(task))
                    .build();

            R<Void> result = messageServiceClient.sendBatchPersonalMessage(request);
            if (!R.isSuccess(result)) {
                log.warn("发送任务状态变更通知失败: taskId={}, error={}", task.getId(), result.getMsg());
            }
        } catch (Exception e) {
            log.error("发送任务状态变更通知异常: taskId={}", task.getId(), e);
        }
    }

    /**
     * 发送任务接取通知
     */
    public void sendTaskClaimedNotification(Tasks task, Long claimerId) {
        try{
            // 通知任务创建者
            Long creatorId = task.getCreatedBy();
            // 自己创建的任务不需要通知自己
            if (creatorId.equals(claimerId)) {
                return;
            }

            // 获取接取者姓名
            String claimerName = getUserNameById(claimerId);

            SendMessageRequestDTO request = SendMessageRequestDTO.builder()
                    .scene("TASK_ASSIGN")
                    .senderId(claimerId)
                    .receiverId(creatorId)
                    .title("任务接取")
                    .content(String.format("「%s」已主动接取您创建的任务「%s」", claimerName, task.getTitle()))
                    .businessId(task.getId())
                    .businessType("TASK")
                    .extendData(buildTaskExtendData(task))
                    .build();

            R<Void> result = messageServiceClient.sendPersonalMessage(request);
            if (!R.isSuccess(result)) {
                log.warn("发送任务接取通知失败: taskId={}, error={}", task.getId(), result.getMsg());
            }
        }catch (Exception e) {
            log.error("发送任务接取通知异常: taskId={}", task.getId(), e);
        }
    }

    /**
     * 发送任务提交通知
     */
    public void sendTaskSubmittedNotification(TaskSubmission submission, Tasks task) {
        try {
            // 通知任务创建者（审核人）
            Long creatorId = task.getCreatedBy();
            if (creatorId.equals(submission.getSubmitterId())) {
                return; // 自己提交给自己的任务不需要通知
            }

            String submissionType = Boolean.TRUE.equals(submission.getIsFinal()) ? "最终提交" : "进度提交";
            // 获取提交者姓名
            String submitterName = getUserNameById(submission.getSubmitterId());

            SendMessageRequestDTO request = SendMessageRequestDTO.builder()
                    .scene("TASK_REVIEW_REQUEST")
                    .senderId(submission.getSubmitterId())
                    .receiverId(creatorId)
                    .title("待审核任务")
                    .content(String.format("任务「%s」有新的%s（版本%d），提交者「%s」，请及时审核",
                            task.getTitle(), submissionType, submission.getVersion(), submitterName))
                    .businessId(task.getId())
                    .businessType("TASK")
                    .extendData(buildTaskSubmissionExtendData(submission, task))
                    .build();

            R<Void> result = messageServiceClient.sendPersonalMessage(request);
            if (!R.isSuccess(result)) {
                log.warn("发送任务提交通知失败: submissionId={}, error={}", submission.getId(), result.getMsg());
            }
        } catch (Exception e) {
            log.error("发送任务提交通知异常: submissionId={}", submission.getId(), e);
        }
    }

    /**
     * 发送任务审核结果通知
     */
    public void sendTaskReviewResultNotification(TaskSubmission submission, Tasks task, ReviewStatus reviewStatus, Long reviewerId) {
        try{
            String resultText = reviewStatus == ReviewStatus.APPROVED ? "通过" : "驳回";
            String comment = submission.getReviewComment() != null ?
                    String.format("，审核意见：%s", submission.getReviewComment()) : "";

            // 获取审核者姓名
            String reviewerName = getUserNameById(reviewerId);

            SendMessageRequestDTO request = SendMessageRequestDTO.builder()
                    .scene("TASK_REVIEW_RESULT")
                    .senderId(reviewerId)
                    .receiverId(submission.getSubmitterId())
                    .title("任务审核结果")
                    .content(String.format("任务「%s」的提交已%s，审核者「%s」%s",
                            task.getTitle(), resultText, reviewerName, comment))
                    .businessId(task.getId())
                    .businessType("TASK")
                    .extendData(buildTaskSubmissionExtendData(submission, task))
                    .build();

            R<Void> result = messageServiceClient.sendPersonalMessage(request);
            if (!R.isSuccess(result)) {
                log.warn("发送任务审核结果通知失败: submissionId={}, error={}", submission.getId(), result.getMsg());
            }

            // 如果最终提交被通过，通知项目内所有人
            if (reviewStatus == ReviewStatus.APPROVED && Boolean.TRUE.equals(submission.getIsFinal())) {
                sendTaskCompletedNotification(task, submission.getSubmitterId());
            }
        }catch (Exception e) {
            log.error("发送任务审核结果通知异常: submissionId={}", submission.getId(), e);
        }
    }

    /**
     * 发送任务完成通知
     */
    public void sendTaskCompletedNotification(Tasks task, Long completedBy) {
        try {
            List<Long> assigneeIds = getTaskAssigneeIds(task.getId());
            if (assigneeIds.isEmpty()) {
                return;
            }

            // 获取完成者姓名
            String completedByName = getUserNameById(completedBy);

            SendMessageRequestDTO request = SendMessageRequestDTO.builder()
                    .scene("TASK_STATUS_CHANGED")
                    .senderId(completedBy)
                    .receiverIds(assigneeIds)
                    .title("任务已完成")
                    .content(String.format("任务「%s」已完成，完成者「%s」，感谢大家的协作",
                            task.getTitle(), completedByName))
                    .businessId(task.getId())
                    .businessType("TASK")
                    .extendData(buildTaskExtendData(task))
                    .build();

            R<Void> result = messageServiceClient.sendBatchPersonalMessage(request);
            if (!R.isSuccess(result)) {
                log.warn("发送任务完成通知失败: taskId={}, error={}", task.getId(), result.getMsg());
            }
        } catch (Exception e) {
            log.error("发送任务完成通知异常: taskId={}", task.getId(), e);
        }
    }


    /**
     * 发送任务撤回通知
     */
    public void sendTaskSubmissionRevokedNotification(TaskSubmission submission, Tasks task) {
        try {
            // 通知任务创建者（审核人）
            Long creatorId = task.getCreatedBy();
            // 自己撤回自己的提交不需要通知
            if (creatorId.equals(submission.getSubmitterId())) {
                return;
            }

            SendMessageRequestDTO request = SendMessageRequestDTO.builder()
                    .scene("TASK_STATUS_CHANGED")
                    .senderId(submission.getSubmitterId())
                    .receiverId(creatorId)
                    .title("任务提交撤回")
                    .content(String.format("任务「%s」的提交已被撤回", task.getTitle()))
                    .businessId(task.getId())
                    .businessType("TASK")
                    .extendData(buildTaskSubmissionExtendData(submission, task))
                    .build();

            R<Void> result = messageServiceClient.sendPersonalMessage(request);
            if (!R.isSuccess(result)) {
                log.warn("发送任务撤回通知失败: submissionId={}, error={}", submission.getId(), result.getMsg());
            }
        } catch (Exception e) {
            log.error("发送任务撤回通知异常: submissionId={}", submission.getId(), e);
        }
    }


    /**
     * 发送任务人数已满通知
     */
    public void sendTaskFullNotification(Tasks task, List<Long> assigneeIds) {
        try {
            if (assigneeIds == null || assigneeIds.isEmpty()) {
                return;
            }

            SendMessageRequestDTO request = SendMessageRequestDTO.builder()
                    .scene("TASK_STATUS_CHANGED")
                    .senderId(null) // 系统消息
                    .receiverIds(assigneeIds)
                    .title("任务人数已满")
                    .content(String.format("任务「%s」已达到最大委托人数(%d)，可以开始协作完成",
                            task.getTitle(), task.getRequiredPeople()))
                    .businessId(task.getId())
                    .businessType("TASK")
                    .extendData(buildTaskExtendData(task))
                    .build();

            R<Void> result = messageServiceClient.sendBatchPersonalMessage(request);
            if (!R.isSuccess(result)) {
                log.warn("发送任务人数已满通知失败: taskId={}, error={}", task.getId(), result.getMsg());
            }
        } catch (Exception e) {
            log.error("发送任务人数已满通知异常: taskId={}", task.getId(), e);
        }
    }


    /**
     * 发送任务到期提醒
     */
    public void sendTaskDeadlineReminder(Tasks task, List<Long> assigneeIds) {
        try{
            if(assigneeIds == null || assigneeIds.isEmpty()) {
                return;
            }

            SendMessageRequestDTO requestDTO = SendMessageRequestDTO.builder()
                    .scene("TASK_DEADLINE_REMIND")
                    .senderId(null) // 系统消息
                    .receiverIds(assigneeIds)
                    .title("任务到期提醒")
                    .content(String.format("任务「%s」即将到期，请及时处理", task.getTitle()))
                    .businessId(task.getId())
                    .businessType("TASK")
                    .extendData(buildTaskExtendData(task))
                    .build();

            R<Void> result = messageServiceClient.sendBatchPersonalMessage(requestDTO);
            if (!R.isSuccess(result)) {
                log.warn("发送任务到期提醒失败: taskId={}, error={}", task.getId(), result.getMsg());
            }
        }catch (Exception e) {
            log.error("发送任务到期提醒异常: taskId={}", task.getId(), e);
        }
    }


    /**
     * 发送任务逾期警告
     */
    public void sendTaskOverdueWarning(Tasks task, List<Long> assigneeIds) {
        try {
            if (assigneeIds == null || assigneeIds.isEmpty()) {
                return;
            }

            SendMessageRequestDTO request = SendMessageRequestDTO.builder()
                    .scene("TASK_OVERDUE")
                    .senderId(null) // 系统消息
                    .receiverIds(assigneeIds)
                    .title("任务逾期警告")
                    .content(String.format("任务「%s」已逾期，请尽快处理", task.getTitle()))
                    .businessId(task.getId())
                    .businessType("TASK")
                    .extendData(buildTaskExtendData(task))
                    .build();

            R<Void> result = messageServiceClient.sendBatchPersonalMessage(request);
            if (!R.isSuccess(result)) {
                log.warn("发送任务逾期警告失败: taskId={}, error={}", task.getId(), result.getMsg());
            }
        } catch (Exception e) {
            log.error("发送任务逾期警告异常: taskId={}", task.getId(), e);
        }
    }


    /**
     * 构建任务扩展数据
     */
    private String buildTaskExtendData(Tasks task) {
        try {
            return String.format(
                    "{\"taskId\":%d,\"taskTitle\":\"%s\",\"projectId\":%d,\"status\":\"%s\",\"priority\":\"%s\",\"requiredPeople\":%d,\"jumpUrl\":\"/tasks/%d\"}",
                    task.getId(),
                    escapeJsonString(task.getTitle()),
                    task.getProjectId(),
                    task.getStatus().name(),
                    task.getPriority().name(),
                    task.getRequiredPeople(),
                    task.getId());
        } catch (Exception e) {
            log.error("构建任务扩展数据异常", e);
            return "{}";
        }
    }

    /**
     * 构建任务提交扩展数据
     */
    private String buildTaskSubmissionExtendData(TaskSubmission submission, Tasks task) {
        try {
            return String.format(
                    "{\"submissionId\":%d,\"taskId\":%d,\"taskTitle\":\"%s\",\"projectId\":%d,\"version\":%d,\"isFinal\":%s,\"reviewStatus\":\"%s\",\"jumpUrl\":\"/tasks/%d/submissions/%d\"}",
                    submission.getId(),
                    task.getId(),
                    escapeJsonString(task.getTitle()),
                    task.getProjectId(),
                    submission.getVersion(),
                    submission.getIsFinal(),
                    submission.getReviewStatus().name(),
                    task.getId(),
                    submission.getId());
        } catch (Exception e) {
            log.error("构建任务提交扩展数据异常", e);
            return "{}";
        }
    }

    /**
     * 转义JSON字符串中的特殊字符
     */
    private String escapeJsonString(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 获取任务执行者ID列表
     */
    public List<Long> getTaskAssigneeIds(Long taskId) {
        return taskUserRepository.findActiveExecutorsByTaskId(taskId)
                .stream()
                .map(TaskUser::getUserId)
                .collect(Collectors.toList());
    }
}
