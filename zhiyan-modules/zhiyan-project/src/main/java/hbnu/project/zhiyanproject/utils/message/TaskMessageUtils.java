package hbnu.project.zhiyanproject.utils.message;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyanmessgae.model.dto.SendMessageRequestDTO;
import hbnu.project.zhiyanproject.client.MessageServiceClient;
import hbnu.project.zhiyanproject.model.entity.Project;
import hbnu.project.zhiyanproject.model.entity.Tasks;
import hbnu.project.zhiyanproject.model.entity.TaskSubmission;
import hbnu.project.zhiyanproject.model.enums.ReviewStatus;
import hbnu.project.zhiyanproject.model.enums.TaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

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

    /**
     * 发送任务创建通知
     */
    public void sendTaskCreatedNotification(Tasks task, Long creatorId, List<Long> assigneeIds) {
        try {
            if (assigneeIds == null || assigneeIds.isEmpty()) {
                return;
            }

            SendMessageRequestDTO request = SendMessageRequestDTO.builder()
                    .scene("TASK_ASSIGN")
                    .senderId(creatorId)
                    .receiverIds(assigneeIds)
                    .title("新任务分配")
                    .content(String.format("您被分配到新任务「%s」，请及时处理", task.getTitle()))
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
     * 发送给任务的分配
     */
    public void sendTaskAssignedNotification(Tasks task, List<Long> newAssigneeIds, Long operatorId) {
        try{
            if(newAssigneeIds == null || newAssigneeIds.isEmpty()){
                log.warn("发送给空气，666");
                return;
            }

            SendMessageRequestDTO request = SendMessageRequestDTO.builder()
                    .scene("TASK_ASSIGN")
                    .senderId(operatorId)
                    .receiverIds(newAssigneeIds)
                    .title("任务分配")
                    .content(String.format("您被分配到任务「%s」，请及时处理", task.getTitle()))
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
        }
    }


    /**
     * 构建任务扩展数据
     */
    private String buildTaskExtendData(Tasks task) {
        try {
            return String.format(
                    "{\"taskId\":%d,\"taskTitle\":\"%s\",\"projectId\":%d,\"status\":\"%s\",\"priority\":\"%s\",\"jumpUrl\":\"/tasks/%d\"}",
                    task.getId(),
                    escapeJsonString(task.getTitle()),
                    task.getProjectId(),
                    task.getStatus().name(),
                    task.getPriority().name(),
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
                    "{\"submissionId\":%d,\"taskId\":%d,\"taskTitle\":\"%s\",\"projectId\":%d,\"version\":%d,\"reviewStatus\":\"%s\",\"jumpUrl\":\"/tasks/%d/submissions/%d\"}",
                    submission.getId(),
                    task.getId(),
                    escapeJsonString(task.getTitle()),
                    task.getProjectId(),
                    submission.getVersion(),
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
}
